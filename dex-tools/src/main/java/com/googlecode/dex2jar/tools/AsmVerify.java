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
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;

import p.rn.util.FileWalker;
import p.rn.util.FileWalker.StreamHandler;
import p.rn.util.FileWalker.StreamOpener;

public class AsmVerify extends BaseCmd {

    private static String getShortName(final String name) {
        int n = name.lastIndexOf('/');
        return n == -1 ? name : "o";
    }

    public static void main(String[] args) {
        new AsmVerify().doMain(args);
    }

    static class XTraceMethodVisitor extends TraceMethodVisitor {
        public StringBuffer getBuf() {
            return buf;
        }
    }

    static void printAnalyzerResult(MethodNode method, Analyzer a, final PrintWriter pw)
            throws IllegalArgumentException {
        Frame[] frames = a.getFrames();
        XTraceMethodVisitor mv = new XTraceMethodVisitor();
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
            pw.printf(format, j, s, mv.getBuf()); // mv.text.get(j));
        }
        for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
            ((TryCatchBlockNode) method.tryCatchBlocks.get(j)).accept(mv);
            pw.print(" " + mv.getBuf());
        }
        pw.println();
        pw.flush();
    }

    @Opt(opt = "d", longOpt = "detail", hasArg = false, description = "Print detail error message")
    boolean detail = false;

    public AsmVerify() {
        super("d2j-asm-verify [options] <jar0> [jar1 ... jarN]", "Verify .class in jar");
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length < 1) {
            usage();
            return;
        }

        List<File> files = new ArrayList<File>();
        for (String fn : remainingArgs) {
            File file = new File(fn);
            if (!file.exists()) {
                System.err.println(fn + " is not exists");
                usage();
                return;
            }
            files.add(file);
        }

        for (File file : files) {
            System.out.println("verify " + file);
            new FileWalker().withStreamHandler(new StreamHandler() {

                @Override
                public void handle(boolean isDir, String name, StreamOpener current, Object nameObject)
                        throws IOException {
                    if (isDir || !name.endsWith(".class")) {
                        return;
                    }
                    InputStream is = current.get();
                    ClassReader cr = new ClassReader(is);
                    ClassNode cn = new ClassNode();
                    cr.accept(new CheckClassAdapter(cn, false), ClassReader.SKIP_DEBUG);

                    List<?> methods = cn.methods;
                    for (int i = 0; i < methods.size(); ++i) {
                        MethodNode method = (MethodNode) methods.get(i);
                        BasicVerifier verifier = new BasicVerifier();
                        Analyzer a = new Analyzer(verifier);
                        try {
                            a.analyze(cn.name, method);
                        } catch (Exception ex) {
                            System.err.println("Error verify method " + cr.getClassName() + "." + method.name + " "
                                    + method.desc);
                            if (detail) {
                                ex.printStackTrace(System.err);
                                printAnalyzerResult(method, a, new PrintWriter(System.err));
                            }
                        }
                    }
                }
            }).walk(file);
        }
    }
}
