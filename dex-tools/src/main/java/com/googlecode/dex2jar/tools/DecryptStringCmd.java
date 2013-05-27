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
import java.util.List;

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

    @Opt(opt = "mo", longOpt = "decrypt-method-owner", description = "the owner of the mothed which can decrypt the stings, example: java.lang.String", argName = "owner")
    private String methodOwner;
    @Opt(opt = "mn", longOpt = "decrypt-method-name", description = "the owner of the mothed which can decrypt the stings, the method's signature must be static (type)Ljava/lang/String;", argName = "name")
    private String methodName;
    @Opt(opt = "cp", longOpt = "classpath", description = "add extra lib to classpath", argName = "cp")
    private String classpath;
    @Opt(opt = "t", longOpt = "arg-type", description = "the type of the method's argument, int,string. default is string", argName = "type")
    private String type = "string";

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        File jar = new File(remainingArgs[0]);
        if (!jar.exists()) {
            System.err.println(jar + " is not exists");
            return;
        }
        if (methodName == null || methodOwner == null) {
            System.err.println("Please set --decrypt-method-owner and --decrypt-method-name");
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

        List<String> list = new ArrayList<String>();
        if (classpath != null) {
            list.addAll(Arrays.asList(classpath.split(";|:")));
        }
        list.add(jar.getAbsolutePath());
        URL[] urls = new URL[list.size()];
        for (int i = 0; i < list.size(); i++) {
            urls[i] = new File(list.get(i)).toURI().toURL();
        }
        final Method jmethod;
        final String targetMethodDesc;
        try {
            Class<?> argType = "string".equals(type) ? String.class : int.class;
            URLClassLoader cl = new URLClassLoader(urls);
            jmethod = cl.loadClass(methodOwner).getDeclaredMethod(methodName, argType);
            jmethod.setAccessible(true);
            targetMethodDesc = Type.getMethodDescriptor(jmethod);
        } catch (Exception ex) {
            System.err.println("can't load method: String " + methodOwner + "." + methodName + "(" + type + ")");
            ex.printStackTrace();
            return;
        }
        final String methodOwnerInternalType = this.methodOwner.replace('.', '/');
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

                    ClassReader cr = new ClassReader(current.get());
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, ClassReader.EXPAND_FRAMES);

                    for (Object m0 : cn.methods) {
                        MethodNode m = (MethodNode) m0;
                        if (m.instructions == null) {
                            continue;
                        }
                        AbstractInsnNode p = m.instructions.getFirst();
                        while (p != null) {
                            if (p.getOpcode() == Opcodes.INVOKESTATIC) {
                                MethodInsnNode mn = (MethodInsnNode) p;
                                if (mn.name.equals(methodName) && mn.desc.equals(targetMethodDesc)
                                        && mn.owner.equals(methodOwnerInternalType)) {
                                    AbstractInsnNode q = p.getPrevious();
                                    AbstractInsnNode next = p.getNext();
                                    if (q.getOpcode() == Opcodes.LDC) {
                                        LdcInsnNode ldc = (LdcInsnNode) q;
                                        tryReplace(m.instructions, p, q, jmethod, ldc.cst);
                                    } else if (q.getType() == AbstractInsnNode.INT_INSN) {
                                        IntInsnNode in = (IntInsnNode) q;
                                        tryReplace(m.instructions, p, q, jmethod, in.operand);
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
                                            tryReplace(m.instructions, p, q, jmethod, x);
                                            break;
                                        }
                                    }
                                    p = next;
                                    continue;
                                }
                            }
                            p = p.getNext();
                        }
                    }

                    ClassWriter cw = new ClassWriter(0);
                    cn.accept(cw);
                    fo.write(false, cr.getClassName() + ".class", cw.toByteArray(), null);
                }
            }).walk(jar);
        } finally {
            IOUtils.closeQuietly(fo);
        }
    }

    public static AbstractInsnNode tryReplace(InsnList instructions, AbstractInsnNode p, AbstractInsnNode q,
            Method jmethod, Object arg) {
        try {
            String newValue = (String) jmethod.invoke(null, arg);
            LdcInsnNode nLdc = new LdcInsnNode(newValue);
            instructions.insertBefore(p, nLdc);
            instructions.remove(p);
            instructions.remove(q);
            return nLdc.getNext();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
