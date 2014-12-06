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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.objectweb.asm.*;

@BaseCmd.Syntax(cmd = "d2j-jar-access", syntax = "[options] <jar>", desc = "add or remove class/method/field access in jar file")
public class JarAccessCmd extends BaseCmd implements Opcodes {
    public static void main(String... args) {
        new JarAccessCmd().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output dir of .j files, default is $current_dir/[jar-name]-access.jar", argName = "out-dir")
    private Path output;

    @Opt(opt = "rd", longOpt = "remove-debug", hasArg = false, description = "remove debug info")
    private boolean removeDebug = false;

    @Opt(opt = "rf", longOpt = "remove-field-access", description = "remove access from field", argName = "ACC")
    private String removeFieldAccess;
    @Opt(opt = "rm", longOpt = "remove-method-access", description = "remove access from method", argName = "ACC")
    private String removeMethodAccess;
    @Opt(opt = "rc", longOpt = "remove-class-access", description = "remove access from class", argName = "ACC")
    private String removeClassAccess;
    @Opt(opt = "af", longOpt = "add-field-access", description = "add access from field", argName = "ACC")
    private String addFieldAccess;
    @Opt(opt = "am", longOpt = "add-method-access", description = "add access from method", argName = "ACC")
    private String addMethodAccess;
    @Opt(opt = "ac", longOpt = "add-class-access", description = "add access from class", argName = "ACC")
    private String addClassAccess;

    static int str2acc(String s) {
        if (s == null) {
            return 0;
        }
        int result = 0;
        s = s.toLowerCase();
        if (s.contains("public")) {
            result |= Opcodes.ACC_PUBLIC;
        }
        if (s.contains("private")) {
            result |= Opcodes.ACC_PRIVATE;
        }
        if (s.contains("protected")) {
            result |= Opcodes.ACC_PROTECTED;
        }
        if (s.contains("final")) {
            result |= Opcodes.ACC_FINAL;
        }
        if (s.contains("static")) {
            result |= Opcodes.ACC_STATIC;
        }
        if (s.contains("super")) {
            result |= Opcodes.ACC_SUPER;
        }
        if (s.contains("synchronized")) {
            result |= Opcodes.ACC_SYNCHRONIZED;
        }
        if (s.contains("volatile")) {
            result |= Opcodes.ACC_VOLATILE;
        }
        if (s.contains("bridge")) {
            result |= Opcodes.ACC_BRIDGE;
        }
        if (s.contains("transient")) {
            result |= Opcodes.ACC_TRANSIENT;
        }
        if (s.contains("varargs")) {
            result |= Opcodes.ACC_VARARGS;
        }
        if (s.contains("native")) {
            result |= Opcodes.ACC_NATIVE;
        }
        if (s.contains("strict")) {
            result |= Opcodes.ACC_STRICT;
        }
        if (s.contains("interface")) {
            result |= Opcodes.ACC_INTERFACE;
        }
        if (s.contains("abstract")) {
            result |= Opcodes.ACC_ABSTRACT;
        }
        if (s.contains("synthetic")) {
            result |= Opcodes.ACC_SYNTHETIC;
        }
        if (s.contains("annotation")) {
            result |= Opcodes.ACC_ANNOTATION;
        }
        if (s.contains("enum")) {
            result |= Opcodes.ACC_ENUM;
        }
        if (s.contains("deprecated")) {
            result |= Opcodes.ACC_DEPRECATED;
        }
        return result;
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        Path jar = new File(remainingArgs[0]).toPath();
        if (!Files.exists(jar)) {
            System.err.println(jar + " is not exists");
            usage();
            return;
        }

        if (output == null) {
            if (Files.isDirectory(jar)) {
                output = new File(jar.getFileName() + "-access.jar").toPath();
            } else {
                output = new File(getBaseName(jar.getFileName().toString()) + "-access.jar").toPath();
            }
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        final int rf = ~str2acc(removeFieldAccess);
        final int rm = ~str2acc(removeMethodAccess);
        final int rc = ~str2acc(removeClassAccess);

        final int af = str2acc(addFieldAccess);
        final int am = str2acc(addMethodAccess);
        final int ac = str2acc(addClassAccess);

        final int flags = removeDebug ? ClassReader.SKIP_DEBUG : 0;

        try (FileSystem outFileSystem = createZip(output)) {
            final Path outRoot = outFileSystem.getPath("/");
            walkJarOrDir(jar, new FileVisitorX() {
                @Override
                public void visitFile(Path file, String relative) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {

                        final ClassReader r = new ClassReader(Files.readAllBytes(file));

                        ClassWriter cr = new ClassWriter(0);
                        r.accept(new ClassVisitor(ASM4, cr) {

                            @Override
                            public void visit(int version, int access, String name, String signature, String superName,
                                    String[] interfaces) {
                                int na = (access & rc) | ac;
                                if (access != na) {
                                    System.out.println("c " + name);
                                }
                                super.visit(version, na, name, signature, superName, interfaces);
                            }

                            @Override
                            public FieldVisitor visitField(int access, String name, String desc, String signature,
                                    Object value) {
                                int na = (access & rf) | af;
                                if (na != access) {
                                    System.out.println("f " + r.getClassName() + "." + name);
                                }
                                return super.visitField(na, name, desc, signature, value);
                            }

                            @Override
                            public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                    String[] exceptions) {
                                int na = (access & rm) | am;
                                if (na != access) {
                                    System.out.println("m " + r.getClassName() + "." + name + desc);
                                }
                                return super.visitMethod(na, name, desc, signature, exceptions);
                            }

                        }, flags | ClassReader.EXPAND_FRAMES);
                        Files.write(outRoot.resolve(relative), cr.toByteArray());

                    } else {
                        Files.copy(file, outRoot.resolve(relative));
                    }
                }
            });
        }
    }
}
