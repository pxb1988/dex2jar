package com.googlecode.d2j.tools.jar;

import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.dex2jar.tools.BaseCmd;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 1. Replace class A to another class B, include superclass, new for
 * 
 * <pre>
 *     class Test1 extends A ...
 *     class Test2 implements A ...
 *     void amethod(A a) ...
 * </pre>
 * 
 * after
 * 
 * <pre>
 *     class Test1 extends B ...
 *     class Test2 extends B ...
 *     void amethod(B a) ...
 * </pre>
 * 
 * 2. Replace method A to another method B, method B must be public static, and in either 'public static RET b(ARGs)' or
 * 'public RET b(Invocation inv)' RET: same return type with method A or Object ARGs: if method A is static, ARGs is
 * same with method A, if method A is non-static the ARGs is 'thiz, arguments in methodA'
 * 
 * <pre>
 * public int a() {
 *     Test t = new Test();
 *     return t.test(1, 2);
 * }
 * </pre>
 * 
 * after
 * 
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
 * 
 * 3. TODO, Replace Methods Implementations
 * 
 * <pre>
 * public int test() {
 *         ...
 * }
 * </pre>
 * 
 * after
 * 
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
public class InvocationWeaver implements Opcodes {
    private static final String INVOCATION_INTERFACE = "com/googlecode/d2j/tools/jar/MethodInvocation";
    private static final String DEFAULT_RET_TYPE = "Ld/$$$/j;";
    private static final String DEFAULT_DESC = "(L;)" + DEFAULT_RET_TYPE;
    private static final Type OBJECT_TYPE = Type.getType(Object.class);
    private static String BASE_INVOCATION_TYPE_FMT = "d2j/gen/MI_%03d";
    List<Callback> callbacks = new ArrayList<Callback>();
    int currentInvocationIdx = 0;
    private MtdInfo key = new MtdInfo();
    private Remapper remapper = new Remapper() {

        @Override
        public String mapDesc(String desc) {
            if (desc.length() == 1) {
                return desc;
            }
            String nDesc = clzDescMap.get(desc);
            return nDesc == null ? desc : nDesc;
        }
    };
    private Set<String> ignores = new HashSet<String>();
    private Map<String, String> clzDescMap = new HashMap<String, String>();
    private Map<MtdInfo, MtdInfo> mtdMap = new HashMap<MtdInfo, MtdInfo>();

    static private void box(Type arg, MethodVisitor mv) {
        switch (arg.getSort()) {
        case Type.OBJECT:
        case Type.ARRAY:
            return;
        case Type.INT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            break;
        case Type.LONG:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
            break;
        case Type.FLOAT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Floag", "valueOf", "(F)Ljava/lang/Floag;");
            break;
        case Type.DOUBLE:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            break;
        case Type.SHORT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
            break;
        case Type.CHAR:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
            break;
        case Type.BOOLEAN:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            break;
        case Type.BYTE:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
            break;
        case Type.VOID:
            mv.visitInsn(ACONST_NULL);
            break;
        }
    }

    static private void unBox(Type orgRet, Type nRet, MethodVisitor mv) {
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
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I");
            break;
        case Type.FLOAT:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F");
            break;
        case Type.LONG:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J");
            break;
        case Type.DOUBLE:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D");
            break;
        case Type.BYTE:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "byteValue", "()B");
            break;
        case Type.SHORT:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "shortValue", "()S");
            break;
        case Type.CHAR:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
            break;
        case Type.BOOLEAN:
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
            break;
        }
    }

    private byte[] wave0(byte[] data) throws IOException {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new ClassReader(data).accept(new RemappingClassAdapter(cw, remapper) {
            Map<MtdInfo, MtdInfo> toCreate = new HashMap<MtdInfo, MtdInfo>();
            String clzName;

            @Override
            public void visit(int version, int access, String name, String signature, String superName,
                    String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                clzName = name;
            }

            public MethodVisitor visitMethod(int access, final String name, String desc, String signature,
                    String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions)) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                        MtdInfo mapTo = findTargetMethod(owner, name, desc);
                        if (mapTo != null) {
                            boolean isStatic = opcode == INVOKESTATIC;
                            Type orgRet = Type.getReturnType(desc);
                            Type orgArgs[] = Type.getArgumentTypes(desc);
                            Type nRet = Type.getReturnType(mapTo.desc);
                            Type nArgs[] = Type.getArgumentTypes(mapTo.desc);
                            if (orgRet.getSort() != Type.VOID && nRet.getSort() == Type.VOID) {
                                throw new RuntimeException("can't cast " + nRet + " to " + orgRet);
                            }

                            if (nArgs.length == 1 && nArgs[0].getInternalName().equals(INVOCATION_INTERFACE)) {
                                MtdInfo t = new MtdInfo();
                                t.owner = owner;
                                t.name = name;
                                t.desc = desc;
                                MtdInfo n = newMethodA(opcode, t, mapTo);
                                super.visitMethodInsn(INVOKESTATIC, clzName, n.name, n.desc);
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
                                super.visitMethodInsn(INVOKESTATIC, mapTo.owner, mapTo.name, mapTo.desc);
                                unBox(orgRet, nRet, this.mv);
                            }

                        } else {
                            super.visitMethodInsn(opcode, owner, name, desc);
                        }
                    }

                    private MtdInfo newMethodA(int opcode, MtdInfo t, MtdInfo mapTo) {
                        MtdInfo n = toCreate.get(t);
                        if (n != null) {
                            return n;
                        }
                        n = new MtdInfo();
                        n.owner = t.owner;
                        n.name = t.name + "$$$_A_";

                        int acc = 0;
                        String nDesc = t.desc;
                        boolean hasThis = opcode != INVOKESTATIC;
                        Type[] args = Type.getArgumentTypes(t.desc);
                        Type ret = Type.getReturnType(t.desc);
                        if (hasThis) {
                            List<Type> ts = new ArrayList(5);
                            ts.add(Type.getObjectType(t.owner));
                            ts.addAll(Arrays.asList(args));
                            nDesc = Type.getMethodDescriptor(ret, ts.toArray(new Type[ts.size()]));
                        }
                        n.desc = nDesc;
                        toCreate.put(t, n);
                        MethodVisitor mv = cw.visitMethod(ACC_SYNTHETIC | ACC_PRIVATE | ACC_STATIC, n.name, n.desc,
                                null, null);
                        mv.visitCode();
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
                        mv.visitLdcInsn(args.length);
                        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                        for (int i = 0; i < args.length; i++) {
                            mv.visitInsn(DUP);
                            mv.visitLdcInsn(i);
                            mv.visitVarInsn(args[i].getOpcode(ILOAD), i + start);
                            box(args[i], mv);
                            mv.visitInsn(AASTORE);
                        }
                        int nextIdx = callbacks.size();
                        mv.visitLdcInsn(nextIdx);
                        mv.visitMethodInsn(INVOKESPECIAL, getCurrentInvocationName(), "<init>",
                                "(Ljava/lang/Object;[Ljava/lang/Object;I)V");

                        mv.visitMethodInsn(INVOKESTATIC, mapTo.owner, mapTo.name, mapTo.desc);
                        unBox(ret, Type.getReturnType(mapTo.desc), mv);
                        mv.visitInsn(ret.getOpcode(IRETURN));
                        mv.visitMaxs(-1, -1);
                        mv.visitEnd();

                        Callback cb = new Callback();
                        cb.idx = nextIdx;
                        cb.callback = newMethodCallback(opcode, t);
                        cb.target = t;
                        cb.isSpecial = opcode == INVOKESPECIAL;
                        cb.isStatic = opcode == INVOKESTATIC;
                        callbacks.add(cb);
                        return n;
                    }

                    private MtdInfo newMethodCallback(int opcode, MtdInfo t) {
                        MtdInfo n = new MtdInfo();
                        n.owner = className;
                        n.name = t.name + "$$$$_callback";
                        if (opcode == INVOKESPECIAL || opcode == INVOKESTATIC) {
                            n.desc = "([Ljava/lang/Object;)Ljava/lang/Object;";
                        } else {
                            n.desc = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";
                        }
                        MethodVisitor mv = cw.visitMethod(opcode == INVOKESPECIAL ? ACC_PUBLIC : ACC_PUBLIC
                                | ACC_STATIC, n.name, n.desc, null, null);
                        mv.visitCode();
                        int start;
                        if (opcode != INVOKESTATIC) {
                            mv.visitVarInsn(ALOAD, 0);
                            if (opcode != INVOKESPECIAL) {
                                mv.visitTypeInsn(CHECKCAST, t.owner);
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
                        mv.visitMethodInsn(opcode, t.owner, t.name, t.desc);
                        Type ret = Type.getReturnType(t.desc);
                        box(ret, mv);
                        mv.visitInsn(ARETURN);
                        mv.visitMaxs(-1, -1);
                        mv.visitEnd();
                        return n;
                    }
                };
            }
        }, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private MtdInfo findTargetMethod(String owner, String name, String desc) {
        key.name = name;
        key.owner = owner;
        key.desc = desc;
        MtdInfo v = mtdMap.get(key);
        if (v != null) {
            return v;
        }

        // try with default ret
        key.desc = Type.getMethodDescriptor(Type.getType(DEFAULT_RET_TYPE), Type.getArgumentTypes(desc));
        v = mtdMap.get(key);
        if (v != null) {
            return v;
        }
        // try with default desc
        key.desc = DEFAULT_DESC;
        v = mtdMap.get(key);
        return v;
    }

    public void wave(Path from, final Path to) throws IOException {

        BaseCmd.walkJarOrDir(from, new BaseCmd.FileVisitorX() {
            @Override
            public void visitFile(Path file, Path relative) throws IOException {
                String name = relative.toString();
                Path targetPath = to.resolve(relative);
                Files.createDirectories(targetPath.getParent());
                if (name.endsWith(".class")) {
                    String clzName = name.substring(0, name.length() - ".class".length());
                    if (ignores.contains(clzName)) {
                        Files.copy(file, targetPath);
                    } else {
                        byte[] out = wave0(Files.readAllBytes(file));
                        Files.write(targetPath, out);
                    }
                } else {
                    if (name.startsWith("META-INF/")) {
                        if (name.equals(JarFile.MANIFEST_NAME)) {
                            try (InputStream in = Files.newInputStream(file)) {
                                Manifest mf = new Manifest(in);
                                mf.getMainAttributes().put(new Name("X-NOTICE"), "Modified");
                                mf.getEntries().clear();

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                mf.write(baos);
                                baos.flush();
                                Files.write(targetPath, baos.toByteArray());
                            }
                        } else if (name.endsWith(".DSA") || name.endsWith(".RSA") || name.endsWith(".SF")
                                || name.endsWith(".ECDSA")) {
                            // ignored
                        } else {
                            Files.copy(file, targetPath);
                        }
                    } else {
                        Files.copy(file, targetPath);
                    }
                }
            }
        });

        if (callbacks.size() > 0) {
            String type = getCurrentInvocationName();
            byte[] data = buildInvocationClz(type);
            Path target = to.resolve(type + ".class");
            Files.createDirectories(target.getParent());
            Files.write(target, data);
            nextInvocationName();
        }

    }

    private byte[] buildInvocationClz(String typeName) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_6, ACC_PUBLIC, typeName, null, "java/lang/Object", new String[] { INVOCATION_INTERFACE });
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "thiz", "Ljava/lang/Object;", null, null).visitEnd();
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "args", "[Ljava/lang/Object;", null, null).visitEnd();
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "idx", "I", null, null).visitEnd();
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;[Ljava/lang/Object;I)V", null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
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
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getMethodOwner", "()Ljava/lang/String;", null, null);
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
                MtdInfo m = cb.target;
                mv.visitLdcInsn("L" + m.owner + ";");
                mv.visitInsn(ARETURN);
            }
            mv.visitLabel(def);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("invalid idx");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getMethodName", "()Ljava/lang/String;", null, null);
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
                MtdInfo m = cb.target;
                mv.visitLdcInsn(m.name);
                mv.visitInsn(ARETURN);
            }
            mv.visitLabel(def);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("invalid idx");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }
        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getMethodDesc", "()Ljava/lang/String;", null, null);
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
                MtdInfo m = cb.target;
                mv.visitLdcInsn(m.desc);
                mv.visitInsn(ARETURN);
            }
            mv.visitLabel(def);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("invalid idx");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
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
                    new String[] { "java/lang/Throwable" });
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
                MtdInfo m = cb.callback;
                if (cb.isStatic) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "args", "[Ljava/lang/Object;");
                    mv.visitMethodInsn(INVOKESTATIC, m.owner, m.name, m.desc);
                } else if (cb.isSpecial) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "thiz", "Ljava/lang/Object;");
                    mv.visitTypeInsn(CHECKCAST, m.owner);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "args", "[Ljava/lang/Object;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, m.owner, m.name, m.desc);
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "thiz", "Ljava/lang/Object;");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, typeName, "args", "[Ljava/lang/Object;");
                    mv.visitMethodInsn(INVOKESTATIC, m.owner, m.name, m.desc);
                }
                Type ret = Type.getReturnType(m.desc);
                box(ret, mv);
                mv.visitInsn(ret.getOpcode(IRETURN));
            }
            mv.visitLabel(def);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("invalid idx");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }

        return cw.toByteArray();
    }

    public InvocationWeaver withConfig(Path is) throws IOException {
        return withConfig(Files.readAllLines(is, StandardCharsets.UTF_8));
    }

    public InvocationWeaver withConfig(InputStream is) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> list = new ArrayList<>();
            for (String ln = r.readLine(); ln != null; ln = r.readLine()) {
                list.add(ln);
            }
            return withConfig(list);
        }
    }

    public InvocationWeaver withConfig(List<String> lines) throws IOException {

        for (String ln : lines) {
            if ("".equals(ln) || ln.startsWith("#")) {
                continue;
            }
            switch (ln.charAt(0)) {
            case 'i':
            case 'I':
                ignores.add(ln.substring(2));
                break;
            case 'c':
            case 'C':
                int index = ln.lastIndexOf('=');
                if (index > 0) {
                    String key = toInternal(ln.substring(2, index));
                    String value = toInternal(ln.substring(index + 1));
                    clzDescMap.put(key, value);
                    ignores.add(value);
                }
                break;
            case 'R':
            case 'r':
                index = ln.lastIndexOf('=');
                if (index > 0) {
                    String key = ln.substring(2, index);
                    String value = ln.substring(index + 1);
                    MtdInfo mi = buildMethodInfo(key);

                    index = value.indexOf('.');
                    MtdInfo mtdValue = new MtdInfo();
                    mtdValue.owner = toInternal(value.substring(0, index));

                    int index2 = value.indexOf('(', index);
                    mtdValue.name = value.substring(index + 1, index2);
                    mtdValue.desc = value.substring(index2);

                    mtdMap.put(mi, mtdValue);

                }
                break;
            }

        }
        return this;
    }

    private String toInternal(String key) {
        if (key.endsWith(";")) {
            key = key.substring(1, key.length() - 1);
        }
        return key;
    }

    private MtdInfo buildMethodInfo(String value) {
        int index = value.indexOf('.');
        MtdInfo mtdValue = new MtdInfo();
        mtdValue.owner = toInternal(value.substring(0, index));
        int index2 = value.indexOf('(', index);
        if (index2 >= 0) {
            mtdValue.name = value.substring(index + 1, index2);
            int index3 = value.indexOf(')');
            if (index3 == value.length() - 1) {
                mtdValue.desc = value.substring(index2) + DEFAULT_RET_TYPE;
            } else {
                mtdValue.desc = value.substring(index2);
            }
        } else {
            mtdValue.name = value.substring(index + 1);
            mtdValue.desc = DEFAULT_DESC;
        }
        return mtdValue;
    }

    public String getCurrentInvocationName() {
        return String.format(BASE_INVOCATION_TYPE_FMT, currentInvocationIdx);
    }

    private void nextInvocationName() {
        currentInvocationIdx++;
        callbacks.clear();
    }

    static class Callback {
        int idx;
        MtdInfo callback;
        MtdInfo target;
        boolean isSpecial;
        boolean isStatic;
    }

    public static class MtdInfo {
        public String desc;
        public String name;
        public String owner;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            MtdInfo mtdInfo = (MtdInfo) o;

            if (!desc.equals(mtdInfo.desc))
                return false;
            if (!name.equals(mtdInfo.name))
                return false;
            if (!owner.equals(mtdInfo.owner))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = desc.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + owner.hashCode();
            return result;
        }
    }

}
