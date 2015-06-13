package com.googlecode.d2j.dex;

import java.util.*;

import com.googlecode.d2j.converter.Dex2IRConverter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.InnerClassNode;

import com.googlecode.d2j.*;
import com.googlecode.d2j.converter.IR2JConverter;
import com.googlecode.d2j.node.*;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.ts.*;
import com.googlecode.dex2jar.ir.ts.array.FillArrayTransformer;

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
                inners = new HashSet<Clz>();
            }
            inners.add(clz);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Clz other = (Clz) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
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

    protected static final CleanLabel T_cleanLabel = new CleanLabel();
    protected static final EndRemover T_endRemove = new EndRemover();
    protected static final Ir2JRegAssignTransformer T_ir2jRegAssign = new Ir2JRegAssignTransformer();
    protected static final NewTransformer T_new = new NewTransformer();
    protected static final RemoveConstantFromSSA T_removeConst = new RemoveConstantFromSSA();
    protected static final RemoveLocalFromSSA T_removeLocal = new RemoveLocalFromSSA();
    protected static final ExceptionHandlerTrim T_trimEx = new ExceptionHandlerTrim();
    protected static final TypeTransformer T_type = new TypeTransformer();
    // protected static final TopologicalSort T_topologicalSort = new TopologicalSort();
    protected static final DeadCodeTransformer T_deadCode = new DeadCodeTransformer();
    protected static final FillArrayTransformer T_fillArray = new FillArrayTransformer();
    protected static final AggTransformer T_agg = new AggTransformer();
    protected static final UnSSATransformer T_unssa = new UnSSATransformer();
    protected static final ZeroTransformer T_zero = new ZeroTransformer();
    protected static final VoidInvokeTransformer T_voidInvoke = new VoidInvokeTransformer();
    protected static final NpeTransformer T_npe = new NpeTransformer();

    static private int clearClassAccess(boolean isInner, int access) {
        if ((access & Opcodes.ACC_INTERFACE) == 0) { // issue 55
            access |= Opcodes.ACC_SUPER;// 解决生成的class文件使用dx重新转换时使用的指令与原始指令不同的问题
        }
        // access in class has no acc_static or acc_private
        access &= ~(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE);
        if (isInner && (access & Opcodes.ACC_PROTECTED) != 0) {// protected inner class are public
            access &= ~Opcodes.ACC_PROTECTED;
            access |= Opcodes.ACC_PUBLIC;
        }
        access &= ~DexConstants.ACC_DECLARED_SYNCHRONIZED; // clean ACC_DECLARED_SYNCHRONIZED
        return access;
    }

    static private int clearInnerAccess(int access) {
        access &= (~Opcodes.ACC_SUPER);// inner class attr has no acc_super
        if (0 != (access & Opcodes.ACC_PRIVATE)) {// clear public/protected if it is private
            access &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
        } else if (0 != (access & Opcodes.ACC_PROTECTED)) {// clear public if it is protected
            access &= ~(Opcodes.ACC_PUBLIC);
        }
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
        String xthrows[] = null;
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
                    }
                }
            }
        }
        int access = methodNode.access;
        // clear ACC_DECLARED_SYNCHRONIZED and ACC_CONSTRUCTOR from method flags
        final int cleanFlag = ~((DexConstants.ACC_DECLARED_SYNCHRONIZED | DexConstants.ACC_CONSTRUCTOR));
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
                            Object ts[] = (Object[]) findAnnotationAttribute(ann, "value");
                            for (Object v : ts) {
                                DexType type = (DexType) v;
                                Clz inner = get(classes, type.desc);
                                clz.addInner(inner);
                                inner.enclosingClass = clz;
                            }
                        }
                            break;
                        }
                    }
                }
            }
        }
        return classes;
    }

    public void convertClass(DexClassNode classNode, ClassVisitorFactory cvf, DexFileNode fileNode) {
        convertClass(classNode, cvf, collectClzInfo(fileNode));
    }

    public void convertClass(DexClassNode classNode, ClassVisitorFactory cvf) {
        convertClass(classNode, cvf, new HashMap<String, Clz>());
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
                    }
                }
            }
        }
        String interfaceInterNames[] = null;
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
            isInnerClass = clzInfo.enclosingClass != null || clzInfo.enclosingMethod != null;
            access = clearClassAccess(isInnerClass, access);

        }
        cv.visit(Opcodes.V1_6, access, toInternalName(classNode.className), signature,
                classNode.superClass == null ? null : toInternalName(classNode.superClass), interfaceInterNames);

        List<InnerClassNode> innerClassNodes = new ArrayList<InnerClassNode>(5);
        if (clzInfo != null) {
            searchInnerClass(clzInfo, innerClassNodes, classNode.className);
        }
        if (isInnerClass) {
            // build Outer Clz
            if (clzInfo.innerName == null) {// anonymous Innerclass
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
        Collections.sort(innerClassNodes, INNER_CLASS_NODE_COMPARATOR);
        for (InnerClassNode icn : innerClassNodes) {
            if (icn.innerName != null && !isJavaIdentifier(icn.innerName)) {
                System.err.println("WARN: ignored invalid inner class name " + ", treat as anonymous inner class.");
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
                convertClass(classNode, cvf, classes);
            }
        }
    }

    public void convertField(DexClassNode classNode, DexFieldNode fieldNode, ClassVisitor cv) {
        String signature = null;
        if (fieldNode.anns != null) {
            for (DexAnnotationNode ann : fieldNode.anns) {
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
                    }
                }
            }
        }
        Object value = fieldNode.cst;
        if (value instanceof DexType) {
            value = Type.getType(((DexType) value).desc);
        }
        final int FieldCleanFlag = ~DexConstants.ACC_DECLARED_SYNCHRONIZED;
        FieldVisitor fv = cv.visitField(fieldNode.access & FieldCleanFlag, fieldNode.field.getName(),
                fieldNode.field.getType(), signature, value);
        if (fv == null) {
            return;
        }
        accept(fieldNode.anns, fv);
        fv.visitEnd();
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
        T_cleanLabel.transform(irMethod);
        T_deadCode.transform(irMethod);
        T_removeLocal.transform(irMethod);
        T_removeConst.transform(irMethod);
        T_zero.transform(irMethod);
        if (T_npe.transformReportChanged(irMethod)) {
            T_deadCode.transform(irMethod);
            T_removeLocal.transform(irMethod);
            T_removeConst.transform(irMethod);
        }
        T_new.transform(irMethod);
        T_fillArray.transform(irMethod);
        T_agg.transform(irMethod);
        T_voidInvoke.transform(irMethod);
        T_type.transform(irMethod);
        T_unssa.transform(irMethod);
        T_trimEx.transform(irMethod);
        T_ir2jRegAssign.transform(irMethod);
    }

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
     * 
     * this method will add
     * 
     * <pre>
     * InnerClass  Outter
     * A$B$WeAreHere A$B
     * A$B           A
     * </pre>
     * 
     * to WeAreHere.class
     * 
     */
    private static void searchEnclosing(Clz clz, List<InnerClassNode> innerClassNodes) {
        for (Clz p = clz; p != null; p = p.enclosingClass) {
            Clz enclosingClass = p.enclosingClass;
            if (enclosingClass == null) {
                break;
            }
            int accessInInner = clearInnerAccess(p.access);
            if (p.innerName != null) {// non-anonymous Innerclass
                innerClassNodes.add(new InnerClassNode(toInternalName(p.name),
                        toInternalName(enclosingClass.name), p.innerName, accessInInner));
            } else {// anonymous Innerclass
                innerClassNodes.add(new InnerClassNode(toInternalName(p.name), null, null, accessInInner));
            }
        }
    }

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
     * 
     * this method will add
     * 
     * <pre>
     * InnerClass      Outter
     * WeAreHere$A$B   WeAreHere$A
     * WeAreHere$A     WeAreHere
     * </pre>
     * 
     * to WeAreHere.class
     * 
     * @param clz
     */
    private static void searchInnerClass(Clz clz, List<InnerClassNode> innerClassNodes, String className) {
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
                    if (inner.innerName == null) {// anonymous Innerclass
                        innerClassNodes.add(new InnerClassNode(toInternalName(inner.name), null, null,
                                clearInnerAccess(inner.access)));
                    } else {// non-anonymous Innerclass
                        innerClassNodes.add(new InnerClassNode(toInternalName(inner.name), toInternalName(className),
                                inner.innerName, clearInnerAccess(inner.access)));
                    }
                    stack.push(inner);
                }
            }
        }
    }

    private static final Comparator<InnerClassNode> INNER_CLASS_NODE_COMPARATOR = new Comparator<InnerClassNode>() {
        @Override
        public int compare(InnerClassNode o1, InnerClassNode o2) {
            return o1.name.compareTo(o2.name);
        }
    };

}
