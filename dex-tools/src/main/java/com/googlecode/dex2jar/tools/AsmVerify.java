package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.AbstractVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;

public class AsmVerify extends BaseCmd {
    static Field buf;

    static {
        try {
            buf = AbstractVisitor.class.getDeclaredField("buf");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        buf.setAccessible(true);

    }

    private static String getShortName(final String name) {
        int n = name.lastIndexOf('/');
        return n == -1 ? name : "o";
    }

    public static void main(String[] args) {
        new AsmVerify().doMain(args);
    }

    static void printAnalyzerResult(MethodNode method, Analyzer a, final PrintWriter pw)
            throws IllegalArgumentException, IllegalAccessException {
        Frame[] frames = a.getFrames();
        TraceMethodVisitor mv = new TraceMethodVisitor();
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
            pw.printf(format, j, s, buf.get(mv)); // mv.text.get(j));
        }
        for (int j = 0; j < method.tryCatchBlocks.size(); ++j) {
            ((TryCatchBlockNode) method.tryCatchBlocks.get(j)).accept(mv);
            pw.print(" " + buf.get(mv));
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
            if (!file.exists() || !file.isFile()) {
                System.err.println(fn + " is not exists");
                usage();
                return;
            }
            files.add(file);
        }

        for (File file : files) {
            ZipFile zip = null;
            try {
                zip = new ZipFile(file);
            } catch (IOException e1) {
                System.err.println(file + " is not a validate zip file");
                if (detail) {
                    e1.printStackTrace(System.err);
                }
                usage();
                return;
            }
            System.out.println("Verify jar " + file);
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                ZipEntry zipEntry = e.nextElement();
                if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class")) {
                    InputStream is = zip.getInputStream(zipEntry);
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
            }
        }
    }
}
