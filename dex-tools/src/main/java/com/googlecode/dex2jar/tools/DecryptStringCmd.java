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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import com.googlecode.dex2jar.tools.BaseCmd.Syntax;

@Syntax(cmd = "d2j-decrypt-string", syntax = "[options] <jar>", desc = "Decrypt in class file", onlineHelp = "https://code.google.com/p/dex2jar/wiki/DecryptStrings")
public class DecryptStringCmd extends BaseCmd {
    public static void main(String... args) {
        new DecryptStringCmd().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output of .jar files, default is $current_dir/[jar-name]-decrypted.jar", argName = "out")
    private Path output;

    @Opt(opt = "mo", longOpt = "decrypt-method-owner", description = "the owner of the mothed which can decrypt the stings, example: java.lang.String", argName = "owner")
    private String methodOwner;
    @Opt(opt = "mn", longOpt = "decrypt-method-name", description = "the owner of the mothed which can decrypt the stings, the method's signature must be static (Ljava/lang/String;)Ljava/lang/String;", argName = "name")
    private String methodName;
    @Opt(opt = "cp", longOpt = "classpath", description = "", argName = "cp")
    private String classpath;

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        final Path jar = new File(remainingArgs[0]).toPath();
        if (!Files.exists(jar)) {
            System.err.println(jar + " is not exists");
            return;
        }
        if (methodName == null || methodOwner == null) {
            System.err.println("Please set --decrypt-method-owner and --decrypt-method-name");
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

        List<String> list = new ArrayList<String>();
        if (classpath != null) {
            list.addAll(Arrays.asList(classpath.split(";|:")));
        }
        list.add(jar.toAbsolutePath().toString());
        URL[] urls = new URL[list.size()];
        for (int i = 0; i < list.size(); i++) {
            urls[i] = new File(list.get(i)).toURI().toURL();
        }
        final Method jmethod;
        try {
            URLClassLoader cl = new URLClassLoader(urls);
            jmethod = cl.loadClass(methodOwner).getMethod(methodName, String.class);
            jmethod.setAccessible(true);
        } catch (Exception ex) {
            System.err.println("can't load method: String " + methodOwner + "." + methodName + "(String), message:"
                    + ex.getMessage());
            return;
        }
        final String methodOwnerInternalType = this.methodOwner.replace('.', '/');
        try (FileSystem outputFileSystem = createZip(output)) {
            final Path outputBase = outputFileSystem.getPath("/");
            walkJarOrDir(jar, new FileVisitorX() {
                @Override
                public void visitFile(Path file, Path relative) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {

                        ClassReader cr = new ClassReader(Files.readAllBytes(file));
                        ClassNode cn = new ClassNode();
                        cr.accept(cn, 0);

                        for (Object m0 : cn.methods) {
                            MethodNode m = (MethodNode) m0;
                            if (m.instructions == null) {
                                continue;
                            }
                            AbstractInsnNode p = m.instructions.getFirst();
                            while (p != null) {
                                if (p.getOpcode() == Opcodes.LDC) {
                                    LdcInsnNode ldc = (LdcInsnNode) p;
                                    if (ldc.cst instanceof String) {
                                        String v = (String) ldc.cst;
                                        AbstractInsnNode q = p.getNext();
                                        if (q.getOpcode() == Opcodes.INVOKESTATIC) {
                                            MethodInsnNode mn = (MethodInsnNode) q;
                                            if (mn.name.equals(methodName)
                                                    && mn.desc.equals("(Ljava/lang/String;)Ljava/lang/String;")
                                                    && mn.owner.equals(methodOwnerInternalType)) {
                                                try {
                                                    Object newValue = jmethod.invoke(null, v);
                                                    ldc.cst = newValue;
                                                } catch (Exception e) {
                                                    // ignore
                                                }
                                                m.instructions.remove(q);
                                            }
                                        }
                                    }
                                }
                                p = p.getNext();
                            }
                        }

                        ClassWriter cw = new ClassWriter(0);
                        cn.accept(cw);
                        Files.write(outputBase.resolve(relative), cw.toByteArray());
                    } else {
                        Files.copy(file, outputBase.resolve(relative));
                    }
                }
            });
        }
    }
}
