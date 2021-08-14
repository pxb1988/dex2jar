package com.googlecode.d2j.dex;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.converter.Dex2IRConverter;
import com.googlecode.d2j.converter.IR2JConverter;
import com.googlecode.d2j.node.DexAnnotationNode;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFieldNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.ts.AggTransformer;
import com.googlecode.dex2jar.ir.ts.CleanLabel;
import com.googlecode.dex2jar.ir.ts.DeadCodeTransformer;
import com.googlecode.dex2jar.ir.ts.EndRemover;
import com.googlecode.dex2jar.ir.ts.ExceptionHandlerTrim;
import com.googlecode.dex2jar.ir.ts.Ir2JRegAssignTransformer;
import com.googlecode.dex2jar.ir.ts.MultiArrayTransformer;
import com.googlecode.dex2jar.ir.ts.NewTransformer;
import com.googlecode.dex2jar.ir.ts.NpeTransformer;
import com.googlecode.dex2jar.ir.ts.RemoveConstantFromSSA;
import com.googlecode.dex2jar.ir.ts.RemoveLocalFromSSA;
import com.googlecode.dex2jar.ir.ts.TypeTransformer;
import com.googlecode.dex2jar.ir.ts.UnSSATransformer;
import com.googlecode.dex2jar.ir.ts.VoidInvokeTransformer;
import com.googlecode.dex2jar.ir.ts.ZeroTransformer;
import com.googlecode.dex2jar.ir.ts.array.FillArrayTransformer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InnerClassNode;

public class Dex2Asm {

    protected static class Clz {

        public int access;

        public Clz enclosingClass;

        public Method enclosingMethod;

        public String innerName;

        public Set<Clz> inners = null;

        public final String name;

        public Clz(String name) {
            super();
            this.name = name;
        }

        void addInner(Clz clz) {
            if (inners == null) {
                inners = new HashSet<>();
            }
            inners.add(clz);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Clz other = (Clz) obj;
            if (name == null) {
                return other.name == null;
            } else {
                return name.equals(other.name);
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        public String toString() {
            return "" + name;
        }
    }

    protected static final int ACC_INTERFACE_ABSTRACT = (Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT);

    private static final int NO_CODE_MASK = DexConstants.ACC_ABSTRACT | DexConstants.ACC_NATIVE
            | DexConstants.ACC_ANNOTATION;

    protected static final CleanLabel T_CLEAN_LABEL = new CleanLabel();

    protected static final EndRemover T_END_REMOVE = new EndRemover();

    protected static final Ir2JRegAssignTransformer T_IR_2_J_REG_ASSIGN = new Ir2JRegAssignTransformer();

    protected static final NewTransformer T_NEW = new NewTransformer();

    protected static final RemoveConstantFromSSA T_REMOVE_CONST = new RemoveConstantFromSSA();

    protected static final RemoveLocalFromSSA T_REMOVE_LOCAL = new RemoveLocalFromSSA();

    protected static final ExceptionHandlerTrim T_TRIM_EX = new ExceptionHandlerTrim();

    protected static final TypeTransformer T_TYPE = new TypeTransformer();

    // protected static final TopologicalSort T_topologicalSort = new TopologicalSort();

    protected static final DeadCodeTransformer T_DEAD_CODE = new DeadCodeTransformer();

    protected static final FillArrayTransformer T_FILL_ARRAY = new FillArrayTransformer();

    protected static final AggTransformer T_AGG = new AggTransformer();

    protected static final UnSSATransformer T_UNSSA = new UnSSATransformer();

    protected static final ZeroTransformer T_ZERO = new ZeroTransformer();

    protected static final VoidInvokeTransformer T_VOID_INVOKE = new VoidInvokeTransformer();

    protected static final NpeTransformer T_NPE = new NpeTransformer();

    protected static final MultiArrayTransformer T_MULTI_ARRAY = new MultiArrayTransformer();

    private static int clearClassAccess(boolean isInner, int access) {
        if ((access & Opcodes.ACC_INTERFACE) == 0) { // issue 55
            access |= Opcodes.ACC_SUPER; // 解决生成的class文件使用dx重新转换时使用的指令与原始指令不同的问题
        }
        // access in classes have no acc_static or acc_private
        access &= ~(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE);
        if (isInner && (access & Opcodes.ACC_PROTECTED) != 0) {
            // protected inner classes are public
            access &= ~Opcodes.ACC_PROTECTED;
            access |= Opcodes.ACC_PUBLIC;
        }
        access &= ~DexConstants.ACC_DECLARED_SYNCHRONIZED; // clean ACC_DECLARED_SYNCHRONIZED
        access &= ~Opcodes.ACC_SYNTHETIC; // clean ACC_SYNTHETIC
        return access;
    }

    private static int clearInnerAccess(int access) {
        access &= (~Opcodes.ACC_SUPER); // inner class attr has no acc_super
        if (0 != (access & Opcodes.ACC_PRIVATE)) { // clear public/protected if it is private
            access &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
        } else if (0 != (access & Opcodes.ACC_PROTECTED)) { // clear public if it is protected
            access &= ~(Opcodes.ACC_PUBLIC);
        }
        access &= ~Opcodes.ACC_SYNTHETIC; // clean ACC_SYNTHETIC
        return access;
    }

    protected static String toInternalName(DexType type) {
        return toInternalName(type.desc);
    }

    protected static String toInternalName(String desc) {
        // TODO without creating object
        return Type.getType(desc).getInternalName();
    }

    public static void accept(DexAnnotationNode ann, ClassVisitor v) {
        AnnotationVisitor av = v.visitAnnotation(ann.type, ann.visibility != Visibility.BUILD);
        if (av != null) {
            accept(ann.items, av);
            av.visitEnd();
        }
    }

    public static void accept(List<DexAnnotationNode> anns, ClassVisitor cv) {
        if (anns != null) {
            for (DexAnnotationNode ann : anns) {
                if (ann.visibility != Visibility.SYSTEM) {
                    accept(ann, cv);
                }
            }
        }
    }

    public static void accept(List<DexAnnotationNode> anns, FieldVisitor fv) {
        if (anns != null) {
            for (DexAnnotationNode ann : anns) {
                if (ann.visibility != Visibility.SYSTEM) {
                    accept(ann, fv);
                }
            }
        }
    }

    public static void accept(List<DexAnnotationNode> anns, MethodVisitor mv) {
        if (anns != null) {
            for (DexAnnotationNode ann : anns) {
                if (ann.visibility != Visibility.SYSTEM) {
                    accept(ann, mv);
                }
            }
        }
    }

    public static void accept(DexAnnotationNode ann, MethodVisitor v) {
        AnnotationVisitor av = v.visitAnnotation(ann.type, ann.visibility != Visibility.BUILD);
        if (av != null) {
            accept(ann.items, av);
            av.visitEnd();
        }
    }

    public static void acceptParameter(DexAnnotationNode ann, int index, MethodVisitor v) {
        AnnotationVisitor av = v.visitParameterAnnotation(index, ann.type, ann.visibility != Visibility.BUILD);
        if (av != null) {
            accept(ann.items, av);
            av.visitEnd();
        }
    }

    public static void accept(DexAnnotationNode ann, FieldVisitor v) {
        AnnotationVisitor av = v.visitAnnotation(ann.type, ann.visibility != Visibility.BUILD);
        if (av != null) {
            accept(ann.items, av);
            av.visitEnd();
        }
    }

    public static void accept(List<DexAnnotationNode.Item> items, AnnotationVisitor av) {
        for (DexAnnotationNode.Item item : items) {
            accept(av, item.name, item.value);
        }
    }

    private static void accept(AnnotationVisitor dav, String name, Object o) {
        if (o instanceof Object[]) {
            AnnotationVisitor arrayVisitor = dav.visitArray(name);
            if (arrayVisitor != null) {
                Object[] array = (Object[]) o;
                for (Object e : array) {
                    accept(arrayVisitor, null, e);
                }
                arrayVisitor.visitEnd();
            }
        } else if (o instanceof DexAnnotationNode) {
            DexAnnotationNode ann = (DexAnnotationNode) o;
            AnnotationVisitor av = dav.visitAnnotation(name, ann.type);
            if (av != null) {
                for (DexAnnotationNode.Item item : ann.items) {
                    accept(av, item.name, item.value);
                }
                av.visitEnd();
            }
        } else if (o instanceof Field) {
            Field f = (Field) o;
            dav.visitEnum(name, f.getType(), f.getName());
        } else if (o instanceof DexType) {
            dav.visit(name, Type.getType(((DexType) o).desc));
        } else if (o instanceof Method) {
            System.err.println("WARN: ignored method annotation value");
        } else {
            if (o == null) {
                System.err.println("WARN: ignored null annotation value");
            } else {
                dav.visit(name, o);
            }
        }
    }

    private static MethodVisitor collectBasicMethodInfo(DexMethodNode methodNode, ClassVisitor cv) {
        String[] xthrows = null;
        String signature = null;
        if (methodNode.anns != null) {
            for (DexAnnotationNode ann : methodNode.anns) {
                if (ann.visibility == Visibility.SYSTEM) {
                    switch (ann.type) {
                    case DexConstants.ANNOTATION_THROWS_TYPE: {
                        Object[] strs = (Object[]) findAnnotationAttribute(ann, "value");
                        if (strs != null) {
                            xthrows = new String[strs.length];
                            for (int i = 0; i < strs.length; i++) {
                                DexType type = (DexType) strs[i];
                                xthrows[i] = toInternalName(type);
                            }
                        }
                    }
                    break;
                    case DexConstants.ANNOTATION_SIGNATURE_TYPE: {
                        Object[] strs = (Object[]) findAnnotationAttribute(ann, "value");
                        if (strs != null) {
                            StringBuilder sb = new StringBuilder();
                            for (Object str : strs) {
                                sb.append(str);
                            }
                            signature = sb.toString();
                        }
                    }
                    break;
                    default:
                        break;
                    }
                }
            }
        }
        int access = methodNode.access;
        // clear ACC_DECLARED_SYNCHRONIZED, ACC_CONSTRUCTOR and ACC_SYNTHETIC from method flags
        final int cleanFlag = ~((DexConstants.ACC_DECLARED_SYNCHRONIZED | DexConstants.ACC_CONSTRUCTOR | Opcodes.ACC_SYNTHETIC));
        access &= cleanFlag;
        return cv.visitMethod(access, methodNode.method.getName(), methodNode.method.getDesc(), signature, xthrows);
    }

    protected static Map<String, Clz> collectClzInfo(DexFileNode fileNode) {
        Map<String, Clz> classes = new HashMap<>();
        for (DexClassNode classNode : fileNode.clzs) {
            Clz clz = get(classes, classNode.className);
            clz.access = (clz.access & ~ACC_INTERFACE_ABSTRACT) | classNode.access;
            if (classNode.anns != null) {
                for (DexAnnotationNode ann : classNode.anns) {
                    if (ann.visibility == Visibility.SYSTEM) {
                        switch (ann.type) {
                        case DexConstants.ANNOTATION_ENCLOSING_CLASS_TYPE: {
                            DexType type = (DexType) findAnnotationAttribute(ann, "value");
                            Clz enclosingClass = get(classes, type.desc);
                            clz.enclosingClass = enclosingClass;

                            // apply patch from ChaeHoon Lim,
                            // obfuscated code may declare itself as enclosing class
                            // which cause dex2jar to endless loop
                            //if(!clz.name.equals(clz.enclosingClass.name)) {
                            //    enclosingClass.addInner(clz);
                            //}
                            enclosingClass.addInner(clz);

                        }
                        break;
                        case DexConstants.ANNOTATION_ENCLOSING_METHOD_TYPE: {
                            Method m = (Method) findAnnotationAttribute(ann, "value");
                            Clz enclosingClass = get(classes, m.getOwner());
                            clz.enclosingClass = enclosingClass;
                            clz.enclosingMethod = m;
                            enclosingClass.addInner(clz);
                        }
                        break;
                        case DexConstants.ANNOTATION_INNER_CLASS_TYPE: {
                            for (DexAnnotationNode.Item it : ann.items) {
                                if ("accessFlags".equals(it.name)) {
                                    clz.access |= (Integer) it.value & ~ACC_INTERFACE_ABSTRACT;
                                } else if ("name".equals(it.name)) {
                                    clz.innerName = (String) it.value;
                                }
                            }
                        }
                        break;
                        case DexConstants.ANNOTATION_MEMBER_CLASSES_TYPE: {
                            Object[] ts = (Object[]) findAnnotationAttribute(ann, "value");
                            for (Object v : ts) {
                                DexType type = (DexType) v;
                                Clz inner = get(classes, type.desc);
                                clz.addInner(inner);
                                inner.enclosingClass = clz;
                            }
                        }
                        break;
                        default:
                            break;
                        }
                    }
                }
            }
        }
        return classes;
    }

    public void convertClass(DexClassNode classNode, ClassVisitorFactory cvf, DexFileNode fileNode) {
        convertClass(fileNode.dexVersion, classNode, cvf, collectClzInfo(fileNode));
    }

    public void convertClass(DexClassNode classNode, ClassVisitorFactory cvf) {
        convertClass(DexConstants.DEX_035, classNode, cvf);
    }

    public void convertClass(int dexVersion, DexClassNode classNode, ClassVisitorFactory cvf) {
        convertClass(dexVersion, classNode, cvf, new HashMap<>());
    }

    private static boolean isJavaIdentifier(String str) {
        if (str.length() < 1) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < str.length(); i++) {
            if (!Character.isJavaIdentifierPart(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public void convertClass(DexClassNode classNode, ClassVisitorFactory cvf, Map<String, Clz> classes) {
        convertClass(DexConstants.DEX_035, classNode, cvf, classes);
    }

    public void convertClass(DexFileNode dfn, DexClassNode classNode, ClassVisitorFactory cvf,
                             Map<String, Clz> classes) {
        convertClass(dfn.dexVersion, classNode, cvf, classes);
    }

    public void convertClass(int dexVersion, DexClassNode classNode, ClassVisitorFactory cvf,
                             Map<String, Clz> classes) {
        ClassVisitor cv = cvf.create(toInternalName(classNode.className));
        if (cv == null) {
            return;
        }
        // the default value of static-final field are omitted by dex, fix it
        DexFix.fixStaticFinalFieldValue(classNode);

        String signature = null;
        if (classNode.anns != null) {
            for (DexAnnotationNode ann : classNode.anns) {
                if (ann.visibility == Visibility.SYSTEM) {
                    switch (ann.type) {
                    case DexConstants.ANNOTATION_SIGNATURE_TYPE: {
                        Object[] strs = (Object[]) findAnnotationAttribute(ann, "value");
                        if (strs != null) {
                            StringBuilder sb = new StringBuilder();
                            for (Object str : strs) {
                                sb.append(str);
                            }
                            signature = sb.toString();
                        }
                    }
                    break;
                    default:
                        break;
                    }
                }
            }
        }
        String[] interfaceInterNames = null;
        if (classNode.interfaceNames != null) {
            interfaceInterNames = new String[classNode.interfaceNames.length];
            for (int i = 0; i < classNode.interfaceNames.length; i++) {
                interfaceInterNames[i] = toInternalName(classNode.interfaceNames[i]);
            }
        }

        Clz clzInfo = classes.get(classNode.className);
        int access = classNode.access;
        boolean isInnerClass = false;
        if (clzInfo != null) {
            if (clzInfo.enclosingClass != null || clzInfo.enclosingMethod != null) {
                if (classNode.anns.stream().noneMatch(x -> x.type.equals("Ldalvik/annotation/EnclosingMethod;"))) {
                    isInnerClass = true;
                } else if (clzInfo.enclosingClass != null) {
                    clzInfo.enclosingClass.inners.remove(clzInfo);
                }
            }
        }
        access = clearClassAccess(isInnerClass, access);

        int version = dexVersion >= DexConstants.DEX_037 ? Opcodes.V1_8 : Opcodes.V1_6;
        cv.visit(version, access, toInternalName(classNode.className), signature,
                classNode.superClass == null ? null : toInternalName(classNode.superClass), interfaceInterNames);

        List<InnerClassNode> innerClassNodes = new ArrayList<>(5);
        if (clzInfo != null) {
            searchInnerClass(clzInfo, innerClassNodes, classNode.className);
        }
        if (isInnerClass) {
            // build Outer Clz
            if (clzInfo.innerName == null) { // anonymous Innerclass
                Method enclosingMethod = clzInfo.enclosingMethod;
                if (enclosingMethod != null) {
                    cv.visitOuterClass(toInternalName(enclosingMethod.getOwner()), enclosingMethod.getName(),
                            enclosingMethod.getDesc());
                } else {
                    Clz enclosingClass = clzInfo.enclosingClass;
                    cv.visitOuterClass(toInternalName(enclosingClass.name), null, null);
                }
            }
            searchEnclosing(clzInfo, innerClassNodes);
        }
        innerClassNodes.sort(INNER_CLASS_NODE_COMPARATOR);
        for (InnerClassNode icn : innerClassNodes) {
            if (icn.innerName != null && !isJavaIdentifier(icn.innerName)) {
                System.err.println("WARN: Ignored invalid inner-class name, "
                        + "treat as anonymous inner class. (" + icn.innerName + ")");
                icn.innerName = null;
                icn.outerName = null;
            }
            icn.accept(cv);
        }

        accept(classNode.anns, cv);

        if (classNode.fields != null) {
            for (DexFieldNode fieldNode : classNode.fields) {
                convertField(classNode, fieldNode, cv);
            }
        }
        if (classNode.methods != null) {
            for (DexMethodNode methodNode : classNode.methods) {
                convertMethod(classNode, methodNode, cv);
            }
        }
        cv.visitEnd();
    }

    public void convertCode(DexMethodNode methodNode, MethodVisitor mv) {
        IrMethod irMethod = dex2ir(methodNode);
        optimize(irMethod);
        ir2j(irMethod, mv);
    }

    public void convertDex(DexFileNode fileNode, ClassVisitorFactory cvf) {
        if (fileNode.clzs != null) {
            Map<String, Clz> classes = collectClzInfo(fileNode);
            for (DexClassNode classNode : fileNode.clzs) {
                convertClass(fileNode, classNode, cvf, classes);
            }
        }
    }

    public void convertField(DexClassNode classNode, DexFieldNode fieldNode, ClassVisitor cv) {
        String signature = null;
        if (fieldNode.anns != null) {
            for (DexAnnotationNode ann : fieldNode.anns) {
                if (ann.visibility == Visibility.SYSTEM) {
                    switch (ann.type) {
                    case DexConstants.ANNOTATION_SIGNATURE_TYPE:
                        Object[] strs = (Object[]) findAnnotationAttribute(ann, "value");
                        if (strs != null) {
                            StringBuilder sb = new StringBuilder();
                            for (Object str : strs) {
                                sb.append(str);
                            }
                            signature = sb.toString();
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        Object value = convertConstantValue(fieldNode.cst);
        final int fieldCleanFlag = ~((DexConstants.ACC_DECLARED_SYNCHRONIZED | Opcodes.ACC_SYNTHETIC));
        FieldVisitor fv = cv.visitField(fieldNode.access & fieldCleanFlag, fieldNode.field.getName(),
                fieldNode.field.getType(), signature == null || !signature.contains(";") ? null : signature, value);
        if (fv == null) {
            return;
        }
        accept(fieldNode.anns, fv);
        fv.visitEnd();
    }

    public static Object[] convertConstantValues(Object[] v) {
        Object[] copy = Arrays.copyOf(v, v.length);
        for (int i = 0; i < copy.length; i++) {
            Object ele = copy[i];
            ele = convertConstantValue(ele);
            copy[i] = ele;
        }
        return copy;
    }

    public static Object convertConstantValue(Object ele) {
        if (ele instanceof DexType) {
            ele = Type.getType(((DexType) ele).desc);
        } else if (ele instanceof MethodHandle) {
            Handle h = null;
            MethodHandle mh = (MethodHandle) ele;
            switch (mh.getType()) {
            case MethodHandle.INSTANCE_GET:
            case MethodHandle.STATIC_GET:
                h = new Handle(Opcodes.H_GETFIELD, toInternalName(mh.getField().getOwner()), mh.getField().getName(),
                        mh.getField().getType(), false);
                break;
            case MethodHandle.INSTANCE_PUT:
            case MethodHandle.STATIC_PUT:
                h = new Handle(Opcodes.H_PUTFIELD, toInternalName(mh.getField().getOwner()), mh.getField().getName(),
                        mh.getField().getType(), false);
                break;
            case MethodHandle.INVOKE_INSTANCE:
                h = new Handle(Opcodes.H_INVOKEVIRTUAL, toInternalName(mh.getMethod().getOwner()),
                        mh.getMethod().getName(), mh.getMethod().getDesc(), false);
                break;
            case MethodHandle.INVOKE_STATIC:
                h = new Handle(Opcodes.H_INVOKESTATIC, toInternalName(mh.getMethod().getOwner()),
                        mh.getMethod().getName(), mh.getMethod().getDesc(), false);
                break;
            case MethodHandle.INVOKE_CONSTRUCTOR:
                h = new Handle(Opcodes.H_NEWINVOKESPECIAL, toInternalName(mh.getMethod().getOwner()),
                        mh.getMethod().getName(), mh.getMethod().getDesc(), false);
                break;
            case MethodHandle.INVOKE_DIRECT:
                h = new Handle(Opcodes.H_INVOKESPECIAL, toInternalName(mh.getMethod().getOwner()),
                        mh.getMethod().getName(), mh.getMethod().getDesc(), false);
                break;
            case MethodHandle.INVOKE_INTERFACE:
                h = new Handle(Opcodes.H_INVOKEINTERFACE, toInternalName(mh.getMethod().getOwner()),
                        mh.getMethod().getName(), mh.getMethod().getDesc(), true);
                break;
            default:
                break;
            }
            ele = h;
        } else if (ele instanceof Proto) {
            ele = Type.getMethodType(((Proto) ele).getDesc());
        }
        return ele;
    }

    public void convertMethod(DexClassNode classNode, DexMethodNode methodNode, ClassVisitor cv) {

        MethodVisitor mv = collectBasicMethodInfo(methodNode, cv);

        if (mv == null) {
            return;
        }
        if (0 != (classNode.access & DexConstants.ACC_ANNOTATION)) { // its inside an annotation
            Object defaultValue = null;
            if (classNode.anns != null) {
                for (DexAnnotationNode ann : classNode.anns) {
                    if (ann.visibility == Visibility.SYSTEM && ann.type.equals(DexConstants.ANNOTATION_DEFAULT_TYPE)) {
                        DexAnnotationNode node = (DexAnnotationNode) findAnnotationAttribute(ann, "value");
                        if (node != null) {
                            defaultValue = findAnnotationAttribute(node, methodNode.method.getName());
                        }
                        break;
                    }
                }
            }
            if (defaultValue != null) {
                AnnotationVisitor av = mv.visitAnnotationDefault();
                if (av != null) {
                    accept(av, null, defaultValue);
                    av.visitEnd();
                }
            }
        }

        accept(methodNode.anns, mv);

        if (methodNode.parameterAnns != null) {
            for (int i = 0; i < methodNode.parameterAnns.length; i++) {
                List<DexAnnotationNode> anns = methodNode.parameterAnns[i];
                if (anns != null) {
                    for (DexAnnotationNode ann : anns) {
                        if (ann.visibility != Visibility.SYSTEM) {
                            acceptParameter(ann, i, mv);
                        }
                    }
                }
            }
        }

        if ((NO_CODE_MASK & methodNode.access) == 0) { // has code
            if (methodNode.codeNode != null) {
                mv.visitCode();
                convertCode(methodNode, mv);
            }
        }

        mv.visitEnd();

    }

    public IrMethod dex2ir(DexMethodNode methodNode) {
        return new Dex2IRConverter()
                .convert(0 != (methodNode.access & DexConstants.ACC_STATIC), methodNode.method, methodNode.codeNode);
    }

    protected static Object findAnnotationAttribute(DexAnnotationNode ann, String name) {
        for (DexAnnotationNode.Item item : ann.items) {
            if (item.name.equals(name)) {
                return item.value;
            }
        }
        return null;
    }

    private static Clz get(Map<String, Clz> classes, String name) {
        Clz clz = classes.get(name);
        if (clz == null) {
            clz = new Clz(name);
            classes.put(name, clz);
        }
        return clz;
    }

    public void ir2j(IrMethod irMethod, MethodVisitor mv) {
        new IR2JConverter(false).convert(irMethod, mv);
        mv.visitMaxs(-1, -1);
    }

    public void optimize(IrMethod irMethod) {
        T_CLEAN_LABEL.transform(irMethod);
        T_DEAD_CODE.transform(irMethod);
        T_REMOVE_LOCAL.transform(irMethod);
        T_REMOVE_CONST.transform(irMethod);
        T_ZERO.transform(irMethod);
        if (T_NPE.transformReportChanged(irMethod)) {
            T_DEAD_CODE.transform(irMethod);
            T_REMOVE_LOCAL.transform(irMethod);
            T_REMOVE_CONST.transform(irMethod);
        }
        T_NEW.transform(irMethod);
        T_FILL_ARRAY.transform(irMethod);
        T_AGG.transform(irMethod);
        T_MULTI_ARRAY.transform(irMethod);
        T_VOID_INVOKE.transform(irMethod);
        T_TYPE.transform(irMethod);
        T_UNSSA.transform(irMethod);
        T_TRIM_EX.transform(irMethod);
        T_IR_2_J_REG_ASSIGN.transform(irMethod);
    }

    // CHECKSTYLE:OFF

    /**
     * For structure
     *
     * <pre>
     * class A {
     *     class B {
     *         class WeAreHere {
     *         }
     *     }
     * }
     * </pre>
     * <p>
     * this method will add
     *
     * <pre>
     * InnerClass  Outter
     * A$B$WeAreHere A$B
     * A$B           A
     * </pre>
     * <p>
     * to WeAreHere.class
     */

    // CHECKSTYLE:ON

    private static void searchEnclosing(Clz clz, List<InnerClassNode> innerClassNodes) {
        Set<Clz> visitedClz = new HashSet<>();
        for (Clz p = clz; p != null; p = p.enclosingClass) {
            if (!visitedClz.add(p)) { // prevent endless loop
                break;
            }
            Clz enclosingClass = p.enclosingClass;
            if (enclosingClass == null) {
                break;
            }
            if (enclosingClass == clz) {
                // enclosing itself, that is impossible
                break;
            }
            int accessInInner = clearInnerAccess(p.access);
            if (p.innerName != null) { // non-anonymous inner class
                innerClassNodes.add(new InnerClassNode(toInternalName(p.name),
                        toInternalName(enclosingClass.name), p.innerName, accessInInner));
            } else { // anonymous inner class
                innerClassNodes.add(new InnerClassNode(toInternalName(p.name), null, null, accessInInner));
            }
        }
    }

    // CHECKSTYLE:OFF

    /**
     * For structure
     *
     * <pre>
     * class WeAreHere {
     *     class A {
     *         class B {
     *
     *         }
     *     }
     * }
     * </pre>
     * <p>
     * this method will add
     *
     * <pre>
     * InnerClass      Outter
     * WeAreHere$A$B   WeAreHere$A
     * WeAreHere$A     WeAreHere
     * </pre>
     * <p>
     * to WeAreHere.class
     */

    // CHECKSTYLE:ON

    private static void searchInnerClass(Clz clz, List<InnerClassNode> innerClassNodes,
                                         String className) {
        Set<Clz> visited = new HashSet<>();
        Stack<Clz> stack = new Stack<>();
        stack.push(clz);
        while (!stack.empty()) {
            clz = stack.pop();
            if (visited.contains(clz)) {
                continue;
            } else {
                visited.add(clz);
            }
            if (clz.inners != null) {
                for (Clz inner : clz.inners) {
                    if (inner.innerName == null) { // anonymous Innerclass
                        innerClassNodes.add(new InnerClassNode(toInternalName(inner.name), null, null,
                                clearInnerAccess(inner.access)));
                    } else { // non-anonymous Innerclass
                        innerClassNodes.add(new InnerClassNode(toInternalName(inner.name), toInternalName(clz.name),
                                inner.innerName, clearInnerAccess(inner.access)));
                    }
                    stack.push(inner);
                }
            }
        }
    }

    private static final Comparator<InnerClassNode> INNER_CLASS_NODE_COMPARATOR =
            Comparator.comparing(o -> o.name);

}
