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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.googlecode.dex2jar.tools.BaseCmd.Syntax;

@Syntax(cmd = "d2j-asm-verify", syntax = "[options] <jar0> [jar1 ... jarN]", desc = "Verify .class in jar")
public class AsmVerify extends BaseCmd {

    private static String getShortName(final String name) {
        int n = name.lastIndexOf('/');
        return n == -1 ? name : "o";
    }

    public static void main(String... args) {
        new AsmVerify().doMain(args);
    }

    static Field buf;
    static {
        try {
            buf = Printer.class.getDeclaredField("buf");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        buf.setAccessible(true);
    }

    static void printAnalyzerResult(MethodNode method, Analyzer a, final PrintWriter pw)
            throws IllegalArgumentException {
        Frame[] frames = a.getFrames();
        Textifier t = new Textifier();
        TraceMethodVisitor mv = new TraceMethodVisitor(t);
        String format = "%05d %-" + (method.maxStack + method.maxLocals + 6) + "s|%s";
        for (int j = 0; j < method.instructions.size(); ++j) {
            method.instructions.get(j).accept(mv);

            StringBuffer s = new StringBuffer();
            Frame f = frames[j];
            if (f == null) {
                s.append('?');
            } else {
                for (int k = 0; k < f.getLocals(); ++k) {
                    s.append(getShortName(f.getLocal(k).toString()));
                }
                s.append(" : ");
                for (int k = 0; k < f.getStackSize(); ++k) {
                    s.append(getShortName(f.getStack(k).toString()));
                }
            }
            try {
                pw.printf(format, j, s, buf.get(t)); // mv.text.get(j));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        for (TryCatchBlockNode tryCatchBlockNode: method.tryCatchBlocks) {
            tryCatchBlockNode.accept(mv);
            try {
                pw.print(" " + buf.get(t));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        pw.println();
        pw.flush();
    }

    @Opt(opt = "d", longOpt = "detail", hasArg = false, description = "Print detail error message")
    boolean detail = false;

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length < 1) {
            usage();
            return;
        }

        List<Path> files = new ArrayList<>();
        for (String fn : remainingArgs) {
            Path file = new File(fn).toPath();
            if (!Files.exists(file)) {
                System.err.println(fn + " is not exists");
                usage();
                return;
            }
            files.add(file);
        }

        for (Path file : files) {
            System.out.println("verify " + file);
            walkJarOrDir(file, new FileVisitorX() {
                @Override
                public void visitFile(Path file, String relative) throws IOException {
                    if (file.getFileName().toString().endsWith(".class")) {
                        ClassReader cr = new ClassReader(Files.readAllBytes(file));
                        ClassNode cn = new ClassNode();
                        cr.accept(new CheckClassAdapter(cn, false), ClassReader.SKIP_DEBUG);
                        for (MethodNode method : cn.methods) {
                            BasicVerifier verifier = new BasicVerifier();
                            Analyzer<BasicValue> a = new Analyzer<>(verifier);
                            try {
                                a.analyze(cn.name, method);
                            } catch (Exception ex) {
                                System.err.println("Error verify method " + cr.getClassName() + "." + method.name + " "
                                        + method.desc);
                                if (detail) {
                                    ex.printStackTrace(System.err);
                                    printAnalyzerResult(method, a, new PrintWriter(new OutputStreamWriter(System.err, StandardCharsets.UTF_8)));
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
