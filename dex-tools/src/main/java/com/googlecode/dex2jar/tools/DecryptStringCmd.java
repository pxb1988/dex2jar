/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2012 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.tools;

import com.googlecode.d2j.converter.IR2JConverter;
import com.googlecode.d2j.converter.J2IRConverter;
import com.googlecode.d2j.util.Escape;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.ts.*;
import com.googlecode.dex2jar.ir.ts.array.FillArrayTransformer;
import com.googlecode.dex2jar.tools.BaseCmd.Syntax;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Syntax(cmd = "d2j-decrypt-string", syntax = "[options] <jar>", desc = "Decrypt in class file", onlineHelp = "https://sourceforge.net/p/dex2jar/wiki/DecryptStrings\nhttps://bitbucket.org/pxb1988/dex2jar/wiki/DecryptStrings")
public class DecryptStringCmd extends BaseCmd {
    public static void main(String... args) {
        new DecryptStringCmd().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output of .jar files, default is $current_dir/[jar-name]-decrypted.jar", argName = "out")
    private Path output;
    @Opt(opt = "m", longOpt = "methods", description = "a file contain a list of methods, each line like: La/b;->decrypt(III)Ljava/lang/String;", argName = "cfg")
    private Path method;
    @Opt(opt = "mo", longOpt = "decrypt-method-owner", description = "the owner of the method which can decrypt the stings, example: java.lang.String", argName = "owner")
    private String methodOwner;
    @Opt(opt = "mn", longOpt = "decrypt-method-name", description = "the owner of the method which can decrypt the stings, the method's signature must be static (parameter-type)Ljava/lang/String;. Please use -pt,--parameter-type to set the argument descrypt.", argName = "name")
    private String methodName;
    @Opt(opt = "cp", longOpt = "classpath", description = "add extra lib to classpath", argName = "cp")
    private String classpath;
    //extended parameter option: e.g. '-t int,byte,string' to specify a routine such as decryptionRoutine(int a, byte b, String c)
    @Opt(opt = "t", longOpt = "arg-types", description = "comma-separated list of types:boolean,byte,short,char,int,long,float,double,string. Default is string", argName = "type")
    private String parameterJTypes;
    @Opt(opt = "pd", longOpt = "parameters-descriptor", description = "the descriptor for the method which can decrypt the stings, example1: Ljava/lang/String; example2: III, default is Ljava/lang/String;", argName = "type")
    private String parametersDescriptor;
    @Opt(opt = "d", longOpt = "delete", hasArg = false, description = "delete the method which can decrypt the stings")
    private boolean deleteMethod = false;
    @Opt(opt = "da", longOpt = "deep-analyze", hasArg = false, description = "use dex2jar IR to static analyze and find more values like byte[]")
    private boolean deepAnalyze = false;

    static class MethodConfig {
        Method jmethod;
        /**
         * in java/lang/String format
         */
        String owner;
        String name;
        String desc;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((desc == null) ? 0 : desc.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((owner == null) ? 0 : owner.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MethodConfig other = (MethodConfig) obj;
            if (desc == null) {
                if (other.desc != null)
                    return false;
            } else if (!desc.equals(other.desc))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (owner == null) {
                if (other.owner != null)
                    return false;
            } else if (!owner.equals(other.owner))
                return false;
            return true;
        }
    }

    MethodConfig build(String line) {
        int idx = line.indexOf("->");
        if (idx < 0) {
            throw new RuntimeException("Can't read line:" + line);
        }
        String owner = line.substring(0, idx);

        if (owner.startsWith("L") && owner.endsWith(";")) {
            owner = owner.substring(1, owner.length() - 1);
        }

        int idx2 = line.indexOf('(', idx);
        if (idx2 < 0) {
            throw new RuntimeException("Can't read line:" + line);
        }

        String name = line.substring(idx + 2, idx2);

        String desc = line.substring(idx2);
        if (desc.endsWith(")")) {
            desc = desc + "Ljava/lang/String;";
        }

        MethodConfig config = new MethodConfig();
        config.owner = owner;
        config.desc = desc;
        config.name = name;
        return config;

    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length == 0) {
            throw new HelpException("One <jar> file is required");
        } else if (remainingArgs.length > 1) {
            throw new HelpException("Only one <jar> file is required, But we found " + remainingArgs.length);
        }

        final Path jar = new File(remainingArgs[0]).toPath();
        if (!Files.exists(jar)) {
            System.err.println(jar + " is not exists");
            return;
        }
        if (output == null) {
            if (Files.isDirectory(jar)) {
                output = new File(jar.getFileName() + "-decrypted.jar").toPath();
            } else {
                output = new File(getBaseName(jar.getFileName().toString()) + "-decrypted.jar").toPath();
            }
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            return;
        }

        System.err.println(jar + " -> " + output);

        List<MethodConfig> methodConfigs = collectMethodConfigs();
        if (methodConfigs == null || methodConfigs.size() == 0) {
            System.err.println("No method selected !");
            return;
        }

        final Map<MethodConfig, MethodConfig> map = loadMethods(jar, methodConfigs);
        try (FileSystem outputFileSystem = createZip(output)) {
            final Path outputBase = outputFileSystem.getPath("/");
            walkJarOrDir(jar, new FileVisitorX() {
                @Override
                public void visitFile(Path file, String relative) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {
                        Path dist1 = outputBase.resolve(relative);
                        createParentDirectories(dist1);
                        byte[] data = Files.readAllBytes(file);
                        ClassNode cn = readClassNode(data);

                        if (decrypt(cn, map)) {
                            byte[] data2 = toByteArray(cn);
                            Files.write(dist1, data2);
                        } else {
                            Files.write(dist1, data);
                        }
                    } else {
                        Path dist1 = outputBase.resolve(relative);
                        createParentDirectories(dist1);
                        Files.copy(file, dist1);
                    }
                }
            });
        }
    }

    private byte[] toByteArray(ClassNode cn) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private ClassNode readClassNode(byte[] data) {
        ClassReader cr = new ClassReader(data);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES);
        return cn;
    }

    private boolean decrypt(ClassNode cn, Map<MethodConfig, MethodConfig> map) {
        if (deepAnalyze) {
            return decryptByIr(cn, map);
        } else {
            return decryptByStack(cn, map);
        }
    }

    private boolean decryptByIr(ClassNode cn, Map<MethodConfig, MethodConfig> map) {
        MethodConfig key = this.key;
        boolean changed = false;
        System.err.println(" >   on " + cn.name);
        for (Iterator<MethodNode> it = cn.methods.iterator(); it.hasNext(); ) {
            MethodNode m = it.next();
            if (m.instructions == null) {
                continue;
            }
            key.owner = cn.name;
            key.name = m.name;
            key.desc = m.desc;
            if (map.containsKey(key)) {
                if (deleteMethod) {
                    it.remove();
                }
                continue;
            }
            boolean find = false;
            // search for the decrypt method
            for (AbstractInsnNode p = m.instructions.getFirst(); p != null; p = p.getNext()) {
                if (p.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode mn = (MethodInsnNode) p;
                    key.owner = mn.owner;
                    key.name = mn.name;
                    key.desc = mn.desc;
                    MethodConfig config = map.get(key);
                    if (config != null) {
                        find = true;
                    }
                }
            }
            if (find) {
                try {
                    // copy m to m2 for cleanup debug info
                    MethodNode m2 = new MethodNode();
                    m2.tryCatchBlocks = new ArrayList<>();
                    m2.name = m.name;
                    m2.access = m.access;
                    m2.desc = m.desc;
                    m.accept(m2);
                    cleanDebug(m2);
                    // convert m2 to ir
                    IrMethod irMethod = J2IRConverter.convert(cn.name, m2);
                    // opt and decrypt
                    optAndDecrypt(irMethod, map);

                    // convert ir to m3
                    MethodNode m3 = new MethodNode();
                    m3.tryCatchBlocks = new ArrayList<>();
                    new IR2JConverter(true).convert(irMethod, m3);

                    // copy back m3 to m
                    m.maxLocals = -1;
                    m.maxLocals = -1;
                    m.instructions = m3.instructions;
                    m.tryCatchBlocks = m3.tryCatchBlocks;
                    m.localVariables = null;
                    changed = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return changed;
    }

    MethodConfig key = new MethodConfig();

    private boolean decryptByStack(ClassNode cn, Map<MethodConfig, MethodConfig> map) {
        MethodConfig key = this.key;
        boolean changed = false;
        for (Iterator<MethodNode> it = cn.methods.iterator(); it.hasNext(); ) {
            MethodNode m = it.next();
            if (m.instructions == null) {
                continue;
            }
            key.owner = cn.name;
            key.name = m.name;
            key.desc = m.desc;
            if (map.containsKey(key)) {
                if (deleteMethod) {
                    it.remove();
                }
                continue;
            }
            AbstractInsnNode p = m.instructions.getFirst();
            while (p != null) {
                if (p.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode mn = (MethodInsnNode) p;
                    key.owner = mn.owner;
                    key.name = mn.name;
                    key.desc = mn.desc;
                    MethodConfig config = map.get(key);
                    if (config != null) {
                        //here we are, given that the decryption method is successfully recognised
                        Method jmethod = config.jmethod;
                        try {
                            int pSize = jmethod.getParameterTypes().length;
                            // arguments' list. each parameter's value is retrieved by reading bytecode backwards, starting from the INVOKESTATIC statement
                            Object[] as = readArgumentValues(mn, jmethod, pSize);
                            //decryption routine invocation
                            String newValue = (String) jmethod.invoke(null, as);
                            //LDC statement generation
                            LdcInsnNode nLdc = new LdcInsnNode(newValue);
                            //insertion of the decrypted string's LDC statement, after INVOKESTATIC statement
                            m.instructions.insert(mn, nLdc);
                            //removal of INVOKESTATIC and previous push statements
                            removeInsts(m, mn, pSize);
                            p = nLdc;
                            changed = true;
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                }
                p = p.getNext();
            }
        }
        return changed;
    }

    protected final CleanLabel T_cleanLabel = new CleanLabel();
    protected final Ir2JRegAssignTransformer T_ir2jRegAssign = new Ir2JRegAssignTransformer();
    protected final NewTransformer T_new = new NewTransformer();
    protected final RemoveConstantFromSSA T_removeConst = new RemoveConstantFromSSA();
    protected final RemoveLocalFromSSA T_removeLocal = new RemoveLocalFromSSA();
    protected final ExceptionHandlerTrim T_trimEx = new ExceptionHandlerTrim();
    protected final TypeTransformer T_type = new TypeTransformer();
    protected final DeadCodeTransformer T_deadCode = new DeadCodeTransformer();
    protected final FillArrayTransformer T_fillArray = new FillArrayTransformer();
    protected final AggTransformer T_agg = new AggTransformer();
    protected final UnSSATransformer T_unssa = new UnSSATransformer();
    protected final ZeroTransformer T_zero = new ZeroTransformer();
    protected final VoidInvokeTransformer T_voidInvoke = new VoidInvokeTransformer();
    protected final NpeTransformer T_npe = new NpeTransformer();

    public void optAndDecrypt(IrMethod irMethod, final Map<MethodConfig, MethodConfig> map) {
        T_deadCode.transform(irMethod);
        T_cleanLabel.transform(irMethod);
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

        new StmtTraveler() {
            @Override
            public Value travel(Value op) {
                op = super.travel(op);
                if (op.vt == Value.VT.INVOKE_STATIC) {
                    InvokeExpr ie = (InvokeExpr) op;
                    MethodConfig key = DecryptStringCmd.this.key;
                    key.owner = ie.owner.substring(1, ie.owner.length() - 1);
                    key.name = ie.name;
                    key.desc = buildMethodDesc(ie.args, ie.ret);

                    MethodConfig c = map.get(key);
                    if (c != null) {
                        try {
                            Method jmethod = c.jmethod;
                            if (ie.args.length != jmethod.getParameterTypes().length) {
                                throw new RuntimeException();
                            }
                            Object args[] = new Object[ie.args.length];
                            for (int i = 0; i < args.length; i++) {
                                args[i] = convertIr2Jobj(ie.getOps()[i], ie.args[i]);
                            }
                            String str = (String) jmethod.invoke(null, args);
                            System.err.println("   < " + Escape.v(str));
                            return Exprs.nString(str);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                return op;
            }
        }.travel(irMethod.stmts);

        T_type.transform(irMethod);
        T_unssa.transform(irMethod);
        T_trimEx.transform(irMethod);
        T_ir2jRegAssign.transform(irMethod);
    }

    private Object convertIr2Jobj(Value value, String type) {
        if (value instanceof Constant) {
            if (Constant.Null.equals(((Constant) value).value)) {
                return null;
            }
        }
        switch (type) {
            case "Z": {
                Object obj = ((Constant) value).value;
                return obj instanceof Boolean ? obj : ((Number) obj).intValue() != 0;
            }
            case "B": {
                Object obj = ((Constant) value).value;
                return ((Number) obj).byteValue();
            }
            case "S": {
                Object obj = ((Constant) value).value;
                return ((Number) obj).shortValue();
            }
            case "C": {
                Object obj = ((Constant) value).value;
                return obj instanceof Character ? obj : (char) ((Number) obj).intValue();
            }
            case "I": {
                Object obj = ((Constant) value).value;
                return ((Number) obj).intValue();
            }
            case "J": {
                Object obj = ((Constant) value).value;
                return ((Number) obj).longValue();
            }
            case "F": {
                Object obj = ((Constant) value).value;
                return obj instanceof Float ? obj : Float.intBitsToFloat(((Number) obj).intValue());
            }
            case "D": {
                Object obj = ((Constant) value).value;
                return obj instanceof Double ? obj : Double.longBitsToDouble(((Number) obj).longValue());
            }
            case "Ljava/lang/String;":
                return (String) ((Constant) value).value;
            case "[Z":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof boolean[]) {
                        return obj;
                    } else {

                        boolean[] b = new boolean[Array.getLength(obj)];
                        for (int i = 0; i < b.length; i++) {
                            b[i] = ((Number) Array.get(obj, i)).intValue() != 0;
                        }
                        return b;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    boolean b[] = new boolean[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        if (obj instanceof Boolean) {
                            b[i] = ((Boolean) obj).booleanValue();
                        } else {
                            b[i] = ((Number) obj).intValue() != 0;
                        }
                    }
                    return b;
                }
                throw new RuntimeException();
            case "[B":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof byte[]) {
                        return obj;
                    } else {
                        byte[] b = new byte[Array.getLength(obj)];
                        for (int i = 0; i < b.length; i++) {
                            b[i] = ((Number) Array.get(obj, i)).byteValue();
                        }
                        return b;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    byte b[] = new byte[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        b[i] = ((Number) obj).byteValue();
                    }
                    return b;
                }
                throw new RuntimeException();
            case "[S":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof short[]) {
                        return obj;
                    } else {
                        short[] b = new short[Array.getLength(obj)];
                        for (int i = 0; i < b.length; i++) {
                            b[i] = ((Number) Array.get(obj, i)).shortValue();
                        }
                        return b;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    short b[] = new short[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        b[i] = ((Number) obj).shortValue();
                    }
                    return b;
                }
                throw new RuntimeException();
            case "[C":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof char[]) {
                        return obj;
                    } else {
                        char[] b = new char[Array.getLength(obj)];
                        for (int i = 0; i < b.length; i++) {
                            b[i] = (char) ((Number) Array.get(obj, i)).intValue();
                        }
                        return b;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    char b[] = new char[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        b[i] = obj instanceof Character ? ((Character) obj).charValue() : (char) ((Number) obj).intValue();
                    }
                    return b;
                }
                throw new RuntimeException();
            case "[I":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof int[]) {
                        return obj;
                    } else {
                        int[] b = new int[Array.getLength(obj)];
                        for (int i = 0; i < b.length; i++) {
                            b[i] = ((Number) Array.get(obj, i)).intValue();
                        }
                        return b;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    int b[] = new int[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        b[i] = ((Number) obj).intValue();
                    }
                    return b;
                }
                throw new RuntimeException();
            case "[J":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof long[]) {
                        return obj;
                    } else {
                        long[] b = new long[Array.getLength(obj)];
                        for (int i = 0; i < b.length; i++) {
                            b[i] = ((Number) Array.get(obj, i)).longValue();
                        }
                        return b;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    long b[] = new long[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        b[i] = ((Number) obj).longValue();
                    }
                    return b;
                }
                throw new RuntimeException();
            case "[F":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof float[]) {
                        return obj;
                    } else {
                        float[] b = new float[Array.getLength(obj)];
                        for (int i = 0; i < b.length; i++) {
                            b[i] = (char) ((Number) Array.get(obj, i)).intValue();
                        }
                        return b;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    float b[] = new float[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        b[i] = obj instanceof Float ? ((Float) obj).floatValue() : Float.intBitsToFloat(((Number) obj).intValue());
                    }
                    return b;
                }
                throw new RuntimeException();
            case "[D":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof double[]) {
                        return obj;
                    } else {
                        double[] b = new double[Array.getLength(obj)];
                        for (int i = 0; i < b.length; i++) {
                            b[i] = (char) ((Number) Array.get(obj, i)).intValue();
                        }
                        return b;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    double b[] = new double[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        b[i] = obj instanceof Double ? ((Double) obj).doubleValue() : Double.longBitsToDouble(((Number) obj).longValue());
                    }
                    return b;
                }
                throw new RuntimeException();
            case "[Ljava/lang/String;":
                if (value instanceof Constant) {
                    Object obj = ((Constant) value).value;
                    if (obj instanceof String[]) {
                        return obj;
                    }
                } else if (value instanceof FilledArrayExpr) {
                    String b[] = new String[value.getOps().length];
                    for (int i = 0; i < b.length; i++) {
                        Object obj = ((Constant) value.getOps()[i]).value;
                        if (obj instanceof String) {
                            b[i] = (String) obj;
                        } else if (Constant.Null.equals(obj)) {
                            b[i] = null;
                        } else {
                            throw new RuntimeException();
                        }
                    }
                    return b;
                }
                throw new RuntimeException();
        }
        throw new RuntimeException();
    }

    private String buildMethodDesc(String[] args, String ret) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (String s : args) {
            sb.append(s);
        }
        return sb.append(')').append(ret).toString();
    }

    private void cleanDebug(MethodNode mn) {
        for (AbstractInsnNode p = mn.instructions.getFirst(); p != null; ) {
            if (p.getType() == AbstractInsnNode.LINE) {
                AbstractInsnNode q = p.getNext();
                mn.instructions.remove(p);
                p = q;
            } else {
                p = p.getNext();
            }
        }
        mn.localVariables = null;
    }

    void removeInsts(MethodNode m, MethodInsnNode mn, int pSize) {
        // remove args
        for (int i = 0; i < pSize; i++) {
            m.instructions.remove(mn.getPrevious());
        }
        // remove INVOKESTATIC
        m.instructions.remove(mn);
    }

    Object[] readArgumentValues(MethodInsnNode mn, Method jmethod, int pSize) {
        AbstractInsnNode q = mn;
        Object[] as = new Object[pSize];
        for (int i = pSize - 1; i >= 0; i--) {
            q = q.getPrevious();
            Object object = readCst(q);
            as[i] = convert(object, jmethod.getParameterTypes()[i]);
        }
        return as;
    }

    Object convert(Object object, Class<?> type) {
        if (int.class.equals(type)) {
            return ((Number) object).intValue();
        }
        if (byte.class.equals(type)) {
            return ((Number) object).byteValue();
        }
        if (short.class.equals(type)) {
            return ((Number) object).shortValue();
        }
        if (char.class.equals(type)) {
            return (char) ((Number) object).intValue();
        }
        if (boolean.class.equals(type)) {
            return (char) ((Number) object).intValue() != 0;
        }
        if (long.class.equals(type)) {
            return (char) ((Number) object).longValue();
        }
        if (float.class.equals(type)) {
            return (char) ((Number) object).floatValue();
        }
        if (double.class.equals(type)) {
            return (char) ((Number) object).doubleValue();
        }
        return object;
    }

    /**
     * load java methods from jar and --classpath
     *
     * @param jar
     * @param methodConfigs
     * @return
     * @throws Exception
     */
    private Map<MethodConfig, MethodConfig> loadMethods(Path jar, List<MethodConfig> methodConfigs) throws Exception {
        final Map<MethodConfig, MethodConfig> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        if (classpath != null) {
            list.addAll(Arrays.asList(classpath.split(";|:")));
        }
        list.add(jar.toAbsolutePath().toString());
        URL[] urls = new URL[list.size()];
        for (int i = 0; i < list.size(); i++) {
            urls[i] = new File(list.get(i)).toURI().toURL();
        }

        URLClassLoader cl = new URLClassLoader(urls);
        for (MethodConfig config : methodConfigs) {
            try {
                Class<?> clz = cl.loadClass(config.owner.replace('/', '.'));
                if (clz == null) {
                    System.err.println("clz is null:" + config.owner);
                }
                Method jmethod = findAnyMethodMatch(clz, config.name,
                        toJavaType(Type.getArgumentTypes(config.desc)));
                jmethod.setAccessible(true);
                config.jmethod = jmethod;
                map.put(config, config);
            } catch (Exception ex) {
                System.err.println("can't load method: L" + config.owner + ";->" + config.name + config.desc);
                throw ex;
            }
        }
        return map;
    }

    /**
     * collect methods from --methods and --method-owner,--method-name
     *
     * @return
     * @throws IOException
     */
    private List<MethodConfig> collectMethodConfigs() throws IOException {
        List<MethodConfig> methodConfigs = new ArrayList<>();
        if (this.method != null) {
            for (String line : Files.readAllLines(this.method, StandardCharsets.UTF_8)) {
                if (line.length() == 0 || line.startsWith("#")) {
                    continue;
                }
                methodConfigs.add(this.build(line));
            }
        }
        if (methodOwner != null && methodName != null) {
            if (this.parametersDescriptor != null) {
                methodConfigs.add(this.build("L" + methodOwner.replace('.', '/') + ";->" + methodName + "("
                        + this.parametersDescriptor + ")Ljava/lang/String;"));
            } else if (this.parameterJTypes != null) {

                //parameterJTypes is a comma-separated list of the decryption method's parameters
                String[] type_list = parameterJTypes.split(",|;|:");
                //switch for all the supported types. String is default
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < type_list.length; i++) {
                    switch (type_list[i]) {
                        case "boolean":
                            sb.append("Z");
                            break;
                        case "byte":
                            sb.append("B");
                            break;
                        case "short":
                            sb.append("S");
                            break;
                        case "char":
                            sb.append("C");
                            break;
                        case "int":
                            sb.append("I");
                            break;
                        case "long":
                            sb.append("J");
                            break;
                        case "float":
                            sb.append("F");
                            break;
                        case "double":
                            sb.append("D");
                            break;
                        case "string":
                            sb.append("Ljava/lang/String;");
                            break;

                        default:
                            throw new RuntimeException("not support type " + type_list[i] + " on -t/--arg-types");
                    }
                }
                methodConfigs.add(this.build("L" + methodOwner.replace('.', '/') + ";->" + methodName + "("
                        + sb + ")Ljava/lang/String;"));
            } else {
                methodConfigs.add(this.build("L" + methodOwner.replace('.', '/') + ";->" + methodName + "(Ljava/lang/String;)Ljava/lang/String;"));
            }
        }
        return methodConfigs;
    }

    /**
     * fix for issue 216, travel all the parent of class and use getDeclaredMethod to find methods
     */
    private Method findAnyMethodMatch(Class<?> clz, String name, Class<?>[] classes) {
        try {
            Method m = clz.getDeclaredMethod(name, classes);
            if (m != null) {
                return m;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Class<?> sup = clz.getSuperclass();
        if (sup != null) {
            Method m = findAnyMethodMatch(sup, name, classes);
            if (m != null) {
                return m;
            }
        }
        Class<?>[] itfs = clz.getInterfaces();
        if (itfs != null && itfs.length > 0) {
            for (Class<?> itf : itfs) {
                Method m = findAnyMethodMatch(itf, name, classes);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }

    Object readCst(AbstractInsnNode q) {

        switch (q.getOpcode()) {
            case Opcodes.LDC:
                // LDC: String, integer, long and double cases (Opcodes.LDC comprehends LDC_W and LDC2_W)
                // push 32bit or 64bit int/float
                // push string/type
                LdcInsnNode ldc = (LdcInsnNode) q;
                if (ldc.cst instanceof Type) {
                    throw new RuntimeException("not support .class value yet!");
                }
                return ldc.cst;

            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                // INT_INSN ("instruction with a single int operand")
                // push 8bit or 16bit int
                IntInsnNode in = (IntInsnNode) q;
                return in.operand;

            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
                // ICONST_*: push a tiny int, -1 <= value <= 5
                return q.getOpcode() - Opcodes.ICONST_0;
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
                return (long) (q.getOpcode() - Opcodes.LCONST_0);
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
                return (float) (q.getOpcode() - Opcodes.FCONST_0);
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
                return (double) (q.getOpcode() - Opcodes.DCONST_0);
        }

        throw new RuntimeException();
    }

    Class<?>[] toJavaType(Type[] pt) throws ClassNotFoundException {
        Class<?> jt[] = new Class<?>[pt.length];
        for (int i = 0; i < pt.length; i++) {
            jt[i] = toJavaType(pt[i]);
        }
        return jt;
    }

    Class<?> toJavaType(Type t) throws ClassNotFoundException {
        switch (t.getSort()) {
            case Type.BOOLEAN:
                return boolean.class;
            case Type.BYTE:
                return byte.class;
            case Type.SHORT:
                return short.class;
            case Type.CHAR:
                return char.class;
            case Type.INT:
                return int.class;
            case Type.FLOAT:
                return float.class;
            case Type.LONG:
                return long.class;
            case Type.DOUBLE:
                return double.class;
            case Type.OBJECT:
                return Class.forName(t.getClassName());
            case Type.ARRAY:
                return Class.forName(t.getDescriptor());
            case Type.VOID:
                return void.class;
        }
        throw new RuntimeException();
    }
}
