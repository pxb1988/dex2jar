package com.googlecode.d2j.tools.jar;

import com.googlecode.dex2jar.tools.BaseCmd;
import com.googlecode.dex2jar.tools.Constants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * 1. Replace class A by another class B, include superclass, new for
 * <p>
 * <pre>
 *     class Test1 extends A ...
 *     class Test2 implements A ...
 *     void amethod(A a) ...
 * </pre>
 * <p>
 * after
 * <p>
 * <pre>
 *     class Test1 extends B ...
 *     class Test2 extends B ...
 *     void amethod(B a) ...
 * </pre>
 * <p>
 * 2. Replace method A to another method B, method B must be public static, and in either 'public static RET b(ARGs)' or
 * 'public RET b(Invocation inv)' RET: same return type with method A or Object ARGs: if method A is static, ARGs is
 * same with method A, if method A is non-static the ARGs is 'thiz, arguments in methodA'
 * <p>
 * <pre>
 * public int a() {
 *     Test t = new Test();
 *     return t.test(1, 2);
 * }
 * </pre>
 * <p>
 * after
 * <p>
 * <pre>
 *     // direct replace
 *     public int a(){
 *         Test t=new Test();
 *         return B(t,1,2);
 *     }
 *     // or by MethodInvocation
 *     public int a(){
 *         Test t=new Test();
 *         return test_$$$_A_(t,1,2)
 *     }
 *     // the replaced invoke method
 *     public static int test$$$_A_(Test t, int a, int b){
 *         MethodInvocation i=new MethodInvocation(t, new Object[]{a.b})
 *         return B(i).intValue();
 *     }
 *     // the callback if MethodInvocation.proceed() is invoked
 *     public static Object test$$$$_callback(Test t, Object[]args) {
 *        return box(t.test(args[0].intValue(),args[1].intValue()));
 *     }
 * </pre>
 * <p>
 * 3. Replace Methods Implementations
 * <p>
 * <pre>
 * public int test() {
 *         ...
 * }
 * </pre>
 * <p>
 * after
 * <p>
 * <pre>
 *     public int test(){
 *          MethodInvocation i=new MethodInvocation(t, new Object[]{a.b})
 *          return B(i).intValue();
 *     }
 *     public int org_test(){
 *         ...
 *     }
 * </pre>
 */
public class InvocationWeaver extends BaseWeaver implements Opcodes {

    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    private final Remapper remapper = new Remapper() {

        @Override
        public String mapDesc(String desc) {
            if (desc.length() == 1) {
                return desc;
            }
            String nDesc = clzDescMap.get(desc);
            return nDesc == null ? desc : nDesc;
        }
    };

    private static void box(Type arg, MethodVisitor mv) {
        switch (arg.getSort()) {
        case Type.OBJECT:
        case Type.ARRAY:
            return;
        case Type.INT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            break;
        case Type.LONG:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            break;
        case Type.FLOAT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Floag", "valueOf", "(F)Ljava/lang/Floag;", false);
            break;
        case Type.DOUBLE:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            break;
        case Type.SHORT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
            break;
        case Type.CHAR:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
            break;
        case Type.BOOLEAN:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            break;
        case Type.BYTE:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
            break;
        case Type.VOID:
            mv.visitInsn(ACONST_NULL);
            break;
        default:
            break;
        }
    }

    private static void unBox(Type orgRet, Type nRet, MethodVisitor mv) {
        if (orgRet.equals(nRet)) {
            return;
        }
        if (orgRet.getSort() == Type.VOID) {
            mv.visitInsn(nRet.getSize() == 1 ? POP : POP2);
        }
        if (nRet.getSort() != Type.OBJECT) {
            throw new RuntimeException("invalid ret type:" + nRet);
        }
        switch (orgRet.getSort()) {
        case Type.OBJECT:
        case Type.ARRAY:
            mv.visitTypeInsn(CHECKCAST, orgRet.getInternalName());
            break;
        case Type.INT:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
            break;
        case Type.FLOAT:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
            break;
        case Type.LONG:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
            break;
        case Type.DOUBLE:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D", false);
            break;
        case Type.BYTE:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "byteValue", "()B", false);
            break;
        case Type.SHORT:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "shortValue", "()S", false);
            break;
        case Type.CHAR:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
            break;
        case Type.BOOLEAN:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            break;
        default:
            break;
        }
    }

    public byte[] wave0(byte[] data) {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        wave0(data, cw);
        return cw.toByteArray();
    }

    public void wave0(byte[] data, final ClassVisitor cv) {
        new ClassReader(data).accept(wrapper(cv), ClassReader.EXPAND_FRAMES);
    }

    public ClassVisitor wrapper(final ClassVisitor cv) {
        return new ClassRemapper(cv, remapper) {
            final Map<MtdInfo, MtdInfo> toCreate = new HashMap<>();
            String clzName;

            private MtdInfo newMethodA(int opcode, MtdInfo t, MtdInfo mapTo) {
                MtdInfo n = toCreate.get(t);
                if (n != null) {
                    return n;
                }
                n = new MtdInfo();
                n.owner = t.owner;
                n.name = buildMethodAName(t.name);
                boolean hasThis = opcode != INVOKESTATIC;

                if (hasThis) {
                    Type[] args = Type.getArgumentTypes(t.desc);
                    Type ret = Type.getReturnType(t.desc);
                    List<Type> ts = new ArrayList<>(args.length + 1);
                    ts.add(Type.getType(t.owner));
                    ts.addAll(Arrays.asList(args));
                    n.desc = Type.getMethodDescriptor(ret, ts.toArray(new Type[0]));
                } else {
                    n.desc = t.desc;
                }

                toCreate.put(t, n);
                MethodVisitor mv = cv.visitMethod(ACC_SYNTHETIC | ACC_PRIVATE | ACC_STATIC, n.name, n.desc, null, null);
                mv.visitCode();
                genMethodACode(opcode, t, mapTo, mv, t);

                return n;
            }

            private void genMethodACode(int opcode, MtdInfo t, MtdInfo mapTo, MethodVisitor mv, MtdInfo src) {
                boolean hasThis = opcode != INVOKESTATIC;
                Type[] args = Type.getArgumentTypes(t.desc);
                Type ret = Type.getReturnType(t.desc);


                final int start;
                mv.visitTypeInsn(NEW, getCurrentInvocationName());
                mv.visitInsn(DUP);
                if (hasThis) {
                    mv.visitVarInsn(ALOAD, 0);
                    start = 1;
                } else {
                    mv.visitInsn(ACONST_NULL);
                    start = 0;
                }
                if (args.length == 0) {
                    mv.visitInsn(ACONST_NULL);
                } else {
                    mv.visitLdcInsn(args.length);
                    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                    for (int i = 0; i < args.length; i++) {
                        mv.visitInsn(DUP);
                        mv.visitLdcInsn(i);
                        mv.visitVarInsn(args[i].getOpcode(ILOAD), i + start);
                        box(args[i], mv);
                        mv.visitInsn(AASTORE);
                    }
                }
                int nextIdx = callbacks.size();
                mv.visitLdcInsn(nextIdx);
                mv.visitMethodInsn(INVOKESPECIAL, getCurrentInvocationName(), "<init>",
                        "(Ljava/lang/Object;[Ljava/lang/Object;I)V", false);

                mv.visitMethodInsn(INVOKESTATIC, toInternal(mapTo.owner), mapTo.name, mapTo.desc, false);
                unBox(ret, Type.getReturnType(mapTo.desc), mv);
                mv.visitInsn(ret.getOpcode(IRETURN));
                mv.visitMaxs(-1, -1);
                mv.visitEnd();

                Callback cb = new Callback();
                cb.idx = nextIdx;
                cb.callback = newMethodCallback(opcode, t);
                cb.target = src;
                cb.isSpecial = opcode == INVOKESPECIAL;
                cb.isStatic = opcode == INVOKESTATIC;
                callbacks.add(cb);
            }

            private MtdInfo newMethodCallback(int opcode, MtdInfo t) {
                MtdInfo n = new MtdInfo();
                n.owner = "L" + className + ";";
                n.name = buildCallbackMethodName(t.name);
                if (opcode == INVOKESPECIAL || opcode == INVOKESTATIC) {
                    n.desc = "([Ljava/lang/Object;)Ljava/lang/Object;";
                } else {
                    n.desc = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";
                }
                MethodVisitor mv = cv.visitMethod(opcode == INVOKESPECIAL ? ACC_PUBLIC : ACC_PUBLIC
                        | ACC_STATIC, n.name, n.desc, null, null);
                mv.visitCode();
                int start;
                if (opcode != INVOKESTATIC) {
                    mv.visitVarInsn(ALOAD, 0);
                    if (opcode != INVOKESPECIAL) {
                        mv.visitTypeInsn(CHECKCAST, toInternal(t.owner));
                    }
                    start = 1;
                } else {
                    start = 0;
                }
                Type[] args = Type.getArgumentTypes(t.desc);

                for (int i = 0; i < args.length; i++) {
                    mv.visitVarInsn(ALOAD, start);
                    mv.visitLdcInsn(i);
                    mv.visitInsn(AALOAD);
                    unBox(args[i], OBJECT_TYPE, mv);
                }
                mv.visitMethodInsn(opcode, toInternal(t.owner), t.name, t.desc, opcode == INVOKEINTERFACE);
                Type ret = Type.getReturnType(t.desc);
                box(ret, mv);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(-1, -1);
                mv.visitEnd();
                return n;
            }

            @Override
            public void visit(int version, int access, String name, String signature, String superName,
                              String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                clzName = name;
            }

            public MethodVisitor visitMethod(int access, final String name, String desc, String signature,
                                             String[] exceptions) {

                final MethodVisitor superMv = superMethodVisitor(access, name, desc, signature, exceptions);
                final MtdInfo mapTo = findDefinedTargetMethod("L" + clzName + ";", name, desc);
                if (mapTo != null) {
                    final MtdInfo t1 = new MtdInfo();
                    t1.owner = "L" + clzName + ";";
                    t1.name = buildMethodAName(name);
                    t1.desc = desc;
                    final MtdInfo src = new MtdInfo();
                    src.owner = t1.owner;
                    src.name = name;
                    src.desc = desc;
                    return new MethodNode(Constants.ASM_VERSION, access, name, desc, signature, exceptions) {
                        @Override
                        public void visitEnd() {

                            InsnList instructions = this.instructions;
                            List<TryCatchBlockNode> tryCatchBlocks = this.tryCatchBlocks;
                            List<LocalVariableNode> localVariables = this.localVariables;

                            this.instructions = new InsnList();
                            this.tryCatchBlocks = new ArrayList<>();
                            this.localVariables = new ArrayList<>();
                            this.maxLocals = -1;
                            this.maxStack = -1;
                            accept(superMv);
                            int opcode;
                            if (Modifier.isStatic(access)) {
                                opcode = Opcodes.INVOKESTATIC;
                            } else {
                                opcode = Opcodes.INVOKEVIRTUAL;
                            }
                            genMethodACode(opcode, t1, mapTo, superMv, src);

                            int newAccess = (access & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC; // make sure public
                            MethodVisitor rmv = wrap(superMethodVisitor(newAccess, t1.name, desc, null, null));
                            if (rmv != null) {
                                rmv.visitCode();
                                int n;
                                int i;
                                n = tryCatchBlocks == null ? 0 : tryCatchBlocks.size();

                                for (i = 0; i < n; ++i) {
                                    tryCatchBlocks.get(i).accept(rmv);
                                }
                                instructions.accept(rmv);
                                n = localVariables == null ? 0 : localVariables.size();

                                for (i = 0; i < n; ++i) {
                                    localVariables.get(i).accept(rmv);
                                }
                                rmv.visitMaxs(-1, -1);
                                rmv.visitEnd();
                            }
                        }
                    };
                } else {
                    return wrap(superMv);
                }
            }

            private MethodVisitor superMethodVisitor(int access, String name, String desc, String signature,
                                                     String[] exceptions) {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }

            MethodVisitor wrap(MethodVisitor mv) {
                return mv == null ? null : new ReplaceMethodVisitor(mv);
            }

            class ReplaceMethodVisitor extends MethodVisitor {

                ReplaceMethodVisitor(MethodVisitor mv) {
                    super(Constants.ASM_VERSION, mv);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc,
                                            boolean isInterface) {
                    MtdInfo mapTo = findTargetMethod("L" + owner + ";", name, desc);
                    if (mapTo != null) {
                        boolean isStatic = opcode == INVOKESTATIC;
                        Type orgRet = Type.getReturnType(desc);
                        Type[] orgArgs = Type.getArgumentTypes(desc);
                        Type nRet = Type.getReturnType(mapTo.desc);
                        Type[] nArgs = Type.getArgumentTypes(mapTo.desc);
                        if (orgRet.getSort() != Type.VOID && nRet.getSort() == Type.VOID) {
                            throw new RuntimeException("can't cast " + nRet + " to " + orgRet);
                        }

                        if (nArgs.length == 1 && nArgs[0].getDescriptor().equals(invocationInterfaceDesc)) {
                            MtdInfo t = new MtdInfo();
                            t.owner = "L" + owner + ";";
                            t.name = name;
                            t.desc = desc;
                            MtdInfo n = newMethodA(opcode, t, mapTo);
                            super.visitMethodInsn(INVOKESTATIC, clzName, n.name, n.desc, isInterface);
                        } else { // simple replace
                            // checking for invalid replace
                            if (isStatic) {
                                if (!Arrays.deepEquals(orgArgs, nArgs)) {
                                    throw new RuntimeException("arguments not equal: " + owner + "." + name + desc
                                            + " <> " + mapTo.owner + "." + mapTo.name + mapTo.desc);
                                }
                            } else {
                                if (nArgs.length != orgArgs.length + 1) {
                                    throw new RuntimeException("arguments not equal: " + owner + "." + name + desc
                                            + " <> " + mapTo.owner + "." + mapTo.name + mapTo.desc);
                                }
                                if (orgArgs.length > 0) {
                                    for (int i = 0; i < orgArgs.length; i++) {
                                        if (!orgArgs[i].equals(nArgs[i + 1])) {
                                            throw new RuntimeException("arguments not equal: " + owner + "." + name
                                                    + desc + " <> " + mapTo.owner + "." + mapTo.name + mapTo.desc);
                                        }
                                    }
                                }
                            }
                            // replace it!
                            super.visitMethodInsn(INVOKESTATIC, toInternal(mapTo.owner), mapTo.name, mapTo.desc,
                                    isInterface);
                            unBox(orgRet, nRet, this.mv);
                        }

                    } else {
                        super.visitMethodInsn(opcode, owner, name, desc, isInterface);
                    }
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                    visitMethodInsn(opcode, owner, name, desc, opcode == INVOKEINTERFACE);
                }
            }

        };
    }

    public void wave(Path from, final Path to) throws IOException {

        BaseCmd.walkJarOrDir(from, (file, relative) -> {
            Path targetPath = to.resolve(relative);
            BaseCmd.createParentDirectories(targetPath);
            if (relative.endsWith(".class")) {
                String clzName = relative.substring(0, relative.length() - ".class".length());
                if (ignores.contains(clzName)) {
                    Files.copy(file, targetPath);
                } else {
                    byte[] out = wave0(Files.readAllBytes(file));
                    Files.write(targetPath, out);
                }
            } else {
                if (relative.startsWith("META-INF/")) {
                    if (relative.equals(JarFile.MANIFEST_NAME)) {
                        try (InputStream in = Files.newInputStream(file)) {
                            Manifest mf = new Manifest(in);
                            mf.getMainAttributes().put(new Name("X-NOTICE"), "Modified");
                            mf.getEntries().clear();

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            mf.write(baos);
                            baos.flush();
                            Files.write(targetPath, baos.toByteArray());
                        }
                    } else if (!relative.endsWith(".DSA") && !relative.endsWith(".RSA")
                            && !relative.endsWith(".SF") && !relative.endsWith(".ECDSA")) {
                        Files.copy(file, targetPath);
                    } /* else {
                        // ignored
                    }*/
                } else {
                    Files.copy(file, targetPath);
                }
            }
        });

        if (!callbacks.isEmpty()) {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            String type = buildInvocationClz(cw);
            byte[] data = cw.toByteArray();
            Path target = to.resolve(type + ".class");
            BaseCmd.createParentDirectories(target);
            Files.write(target, data);
            nextInvocationName();
        }

    }

    public String buildInvocationClz(ClassVisitor cw) {
        String typeName = getCurrentInvocationName();
        cw.visit(V1_6, ACC_PUBLIC, typeName, null, "java/lang/Object", new String[]{
                toInternal(invocationInterfaceDesc)});
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "thiz", "Ljava/lang/Object;", null, null).visitEnd();
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "args", "[Ljava/lang/Object;", null, null).visitEnd();
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "idx", "I", null, null).visitEnd();
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;[Ljava/lang/Object;I)V", null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, typeName, "thiz", "Ljava/lang/Object;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(PUTFIELD, typeName, "args", "[Ljava/lang/Object;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 3);
            mv.visitFieldInsn(PUTFIELD, typeName, "idx", "I");
            mv.visitInsn(RETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        {
            genSwitchMethod(cw, typeName, "getMethodOwner", mtd -> toInternal(mtd.owner));
            genSwitchMethod(cw, typeName, "getMethodName", mtd -> mtd.name);
            genSwitchMethod(cw, typeName, "getMethodDesc", mtd -> mtd.desc);
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getArguments", "()[Ljava/lang/Object;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, typeName, "args", "[Ljava/lang/Object;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getThis", "()Ljava/lang/Object;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, typeName, "thiz", "Ljava/lang/Object;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "proceed", "()Ljava/lang/Object;", null,
                    new String[]{"java/lang/Throwable"});
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, typeName, "idx", "I");
            Label def = new Label();
            Label[] labels = new Label[callbacks.size()];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }
            mv.visitTableSwitchInsn(0, callbacks.size() - 1, def, labels);

            for (int i = 0; i < labels.length; i++) {
                mv.visitLabel(labels[i]);
                Callback cb = callbacks.get(i);
                MtdInfo m = (MtdInfo) cb.callback;
                if (cb.isStatic) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "args", "[Ljava/lang/Object;");
                    mv.visitMethodInsn(INVOKESTATIC, toInternal(m.owner), m.name, m.desc, false);
                } else if (cb.isSpecial) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "thiz", "Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, toInternal(m.owner));
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "args", "[Ljava/lang/Object;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, toInternal(m.owner), m.name, m.desc, false);
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "thiz", "Ljava/lang/Object;");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "args", "[Ljava/lang/Object;");
                    mv.visitMethodInsn(INVOKESTATIC, toInternal(m.owner), m.name, m.desc, false);
                }
                Type ret = Type.getReturnType(m.desc);
                box(ret, mv);
                mv.visitInsn(ret.getOpcode(IRETURN));
            }
            mv.visitLabel(def);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("invalid idx");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitInsn(ATHROW);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        return typeName;
    }

    interface CB {

        String getKey(MtdInfo mtd);

    }

    private void genSwitchMethod(ClassVisitor cw, String typeName, String methodName, CB callback) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, methodName, "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, typeName, "idx", "I");
        Label def = new Label();
        Label[] labels = new Label[callbacks.size()];

        Map<String, Label> strMap = new TreeMap<>();
        for (int i = 0; i < labels.length; i++) {
            Callback cb = callbacks.get(i);
            String key = callback.getKey((MtdInfo) cb.target);
            Label label = strMap.computeIfAbsent(key, k -> new Label());
            labels[i] = label;
        }

        mv.visitTableSwitchInsn(0, callbacks.size() - 1, def, labels);

        for (Map.Entry<String, Label> e : strMap.entrySet()) {
            mv.visitLabel(e.getValue());
            mv.visitLdcInsn(e.getKey());
            mv.visitInsn(ARETURN);
        }
        mv.visitLabel(def);
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("invalid idx");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }


}
