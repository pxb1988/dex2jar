package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
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

public class AsmVerify {
    private static final Options options = new Options();;
    static {
        options.addOption(new Option("d", "detail", false, "print detail error message"));
    }

    /**
     * Prints the usage message.
     */
    private static void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new Comparator<Option>() {

            @Override
            public int compare(Option o1, Option o2) {

                return o1.getOpt().compareTo(o2.getOpt());
            }
        });
        formatter.printHelp("d2j-asm-verify [options] <jar0> [jar1 ... jarN]", "verify .class in jar", options, "");
    }

    /**
     * @param args
     * @throws IOException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            usage();
            return;
        }
        String[] remainingArgs = commandLine.getArgs();
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
        boolean detail = false;
        for (Option option : commandLine.getOptions()) {
            String opt = option.getOpt();
            switch (opt.charAt(0)) {
            case 'd':
                detail = true;
                break;
            }
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
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                ZipEntry zipEntry = e.nextElement();
                System.out.println("Verify jar " + zipEntry.getName());
                if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class")) {
                    InputStream is = zip.getInputStream(zipEntry);
                    ClassReader cr = new ClassReader(is);
                    ClassNode cn = new ClassNode();
                    cr.accept(new CheckClassAdapter(cn, false), ClassReader.SKIP_DEBUG);

                    List methods = cn.methods;
                    for (int i = 0; i < methods.size(); ++i) {
                        MethodNode method = (MethodNode) methods.get(i);
                        BasicVerifier verifier = new BasicVerifier();
                        Analyzer a = new Analyzer(verifier);
                        try {
                            a.analyze(cn.name, method);
                        } catch (Exception ex) {
                            System.err.println("Error verify method " + method.name + " " + method.desc);
                            if (detail) {
                                printAnalyzerResult(method, a, new PrintWriter(System.err));
                            }
                        }
                    }
                }
            }
        }
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

    private static String getShortName(final String name) {
        int n = name.lastIndexOf('/');
        return n == -1 ? name : "o";
    }

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
}
