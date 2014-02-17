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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import p.rn.util.FileOut;
import p.rn.util.FileOut.OutHandler;
import p.rn.util.FileWalker;
import p.rn.util.FileWalker.StreamHandler;
import p.rn.util.FileWalker.StreamOpener;

import com.googlecode.dex2jar.tools.BaseCmd.Syntax;

@Syntax(cmd = "d2j-decrpyt-string", syntax = "[options] <jar>", desc = "Decrypt in class file", onlineHelp = "https://code.google.com/p/dex2jar/wiki/DecryptStrings")
public class DecryptStringCmd extends BaseCmd {
    public static void main(String[] args) {
        new DecryptStringCmd().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output of .jar files, default is $current_dir/[jar-name]-decrypted.jar", argName = "out")
    private File output;
    @Opt(opt = "m", longOpt = "methods", description = "a file contain a list of methods, each line like: La/b;->decrypt(III)Ljava/lang/String;", argName = "cfg")
    private File method;
    @Opt(opt = "mo", longOpt = "decrypt-method-owner", description = "the owner of the method which can decrypt the stings, example: java.lang.String", argName = "owner")
    private String methodOwner;
    @Opt(opt = "mn", longOpt = "decrypt-method-name", description = "the owner of the method which can decrypt the stings, the method's signature must be static (parameter-type)Ljava/lang/String;. Please use -pt,--parameter-type to set the argument descrypt.", argName = "name")
    private String methodName;
    @Opt(opt = "cp", longOpt = "classpath", description = "add extra lib to classpath", argName = "cp")
    private String classpath;
    @Opt(opt = "t", longOpt = "arg-type", description = "ignored")
    private String type = null;

    @Opt(opt = "pt", longOpt = "parameter-type", description = "the descript for the method which can decrypt the stings, example1: Ljava/lang/String; example2:III, default is Ljava/lang/String;", argName = "type")
    private String parameterType = "Ljava/lang/String;";
    @Opt(opt = "d", longOpt = "delete", hasArg = false, description = "delete the method which can decrypt the stings")
    private boolean deleteMethod = false;

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

        File jar = new File(remainingArgs[0]);
        if (!jar.exists()) {
            System.err.println(jar + " is not exists");
            return;
        }
        if (output == null) {
            if (jar.isDirectory()) {
                output = new File(jar.getName() + "-decrypted.jar");
            } else {
                output = new File(FilenameUtils.getBaseName(jar.getName()) + "-decrypted.jar");
            }
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            return;
        }

        System.err.println(jar + " -> " + output);

        List<MethodConfig> methodConfigs = new ArrayList<MethodConfig>();
        if (this.method != null) {
            for (String line : FileUtils.readLines(this.method, "UTF-8")) {
                if (line.length() == 0 || line.startsWith("#")) {
                    continue;
                }
                methodConfigs.add(this.build(line));
            }
        } else {
            if (methodOwner == null || methodName == null) {
                System.err.println("-mo/--decrypt-method-owner or -mn/decrypt-method-name is null");
                return;
            }
            methodConfigs.add(this.build("L" + methodOwner.replace('.', '/') + ";->" + methodName + "("
                    + this.parameterType + ")Ljava/lang/String;"));
        }

        final Map<MethodConfig, MethodConfig> map = new HashMap<MethodConfig, MethodConfig>();
        {
            List<String> list = new ArrayList<String>();
            if (classpath != null) {
                list.addAll(Arrays.asList(classpath.split(";|:")));
            }
            list.add(jar.getAbsolutePath());
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
                    ex.printStackTrace();
                    return;
                }
            }
        }

        final OutHandler fo = FileOut.create(output, true);
        try {
            new FileWalker().withStreamHandler(new StreamHandler() {

                @Override
                public void handle(boolean isDir, String name, StreamOpener current, Object nameObject)
                        throws IOException {
                    if (isDir || !name.endsWith(".class")) {
                        fo.write(isDir, name, current == null ? null : current.get(), nameObject);
                        return;
                    }
                    MethodConfig key = new MethodConfig();
                    ClassReader cr = new ClassReader(current.get());
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, ClassReader.EXPAND_FRAMES);

                    for (Object m0 : new ArrayList(cn.methods)) {
                        MethodNode m = (MethodNode) m0;
                        if (m.instructions == null) {
                            continue;
                        }
                        key.owner = cn.name;
                        key.name = m.name;
                        key.desc = m.desc;
                        if (map.containsKey(key)) {
                            if (deleteMethod) {
                                cn.methods.remove(m);
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
                                    Method jmethod = config.jmethod;
                                    try {
                                        AbstractInsnNode q = p;
                                        int pSize = jmethod.getParameterTypes().length;
                                        Object[] as = new Object[pSize];
                                        for (int i = pSize - 1; i >= 0; i--) {
                                            q = q.getPrevious();
                                            Object object = readCst(q);
                                            as[i] = convert(object, jmethod.getParameterTypes()[i]);
                                        }
                                        String newValue = (String) jmethod.invoke(null, as);
                                        LdcInsnNode nLdc = new LdcInsnNode(newValue);
                                        m.instructions.insert(p, nLdc);
                                        q = p;
                                        for (int i = 0; i <= pSize; i++) {
                                            AbstractInsnNode z = q.getPrevious();
                                            m.instructions.remove(q);
                                            q = z;
                                        }
                                        p = nLdc;
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                            p = p.getNext();
                        }
                    }

                    ClassWriter cw = new ClassWriter(0);
                    cn.accept(cw);
                    fo.write(false, cr.getClassName() + ".class", cw.toByteArray(), null);
                }

                private Object convert(Object object, Class<?> type) {
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
                    return object;
                }
            }).walk(jar);
        } finally {
            IOUtils.closeQuietly(fo);
        }
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
        if (q.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldc = (LdcInsnNode) q;
            return ldc.cst;
        } else if (q.getType() == AbstractInsnNode.INT_INSN) {
            IntInsnNode in = (IntInsnNode) q;
            return in.operand;
        } else {
            switch (q.getOpcode()) {
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
                int x = ((InsnNode) q).getOpcode() - Opcodes.ICONST_0;
                return x;
            }
        }
        throw new RuntimeException();
    }

    private Class<?>[] toJavaType(Type[] pt) throws ClassNotFoundException {
        Class<?> jt[] = new Class<?>[pt.length];
        for (int i = 0; i < pt.length; i++) {
            jt[i] = toJavaType(pt[i]);
        }
        return jt;
    }

    private Class<?> toJavaType(Type t) throws ClassNotFoundException {
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
        case Type.ARRAY:
            return Class.forName(t.getClassName());
        case Type.VOID:
            return void.class;
        }
        throw new RuntimeException();
    }

    public static AbstractInsnNode tryReplace(InsnList instructions, AbstractInsnNode p, AbstractInsnNode q,
            Method jmethod, Object arg) {
        try {

        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
