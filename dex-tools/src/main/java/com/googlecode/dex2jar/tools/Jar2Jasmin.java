package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;

public class Jar2Jasmin extends BaseCmd {
    public static void main(String[] args) {
        new Jar2Jasmin().doMain(args);
    }

    @Opt(opt = "d", longOpt = "debug", hasArg = false, description = "disassemble debug info")
    private boolean debugInfo = false;
    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output dir of .j files, default is $current_dir/[jar-name]-jar2jasmin/", argName = "out-dir")
    private File output;

    public Jar2Jasmin() {
        super("d2j-jar2jasmin [options] <jar>", "Disassemble .class in jar file to jasmin file");
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        File jar = new File(remainingArgs[0]);
        if (!jar.exists() || !jar.isFile()) {
            System.err.println(jar + " is not exists");
            usage();
            return;
        }

        if (output == null) {
            output = new File(FilenameUtils.getBaseName(jar.getName()) + "-jar2jasmin/");
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }
        ZipFile zip;
        try {
            zip = new ZipFile(jar);
        } catch (IOException e) {
            System.err.println(jar + " is not a validate zip file");
            e.printStackTrace(System.err);
            usage();
            return;
        }
        System.out.println("disassemble " + jar + " to " + output);
        int flags = debugInfo ? 0 : ClassReader.SKIP_DEBUG;
        for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
            ZipEntry zipEntry = e.nextElement();
            if (!zipEntry.isDirectory() && zipEntry.getName().endsWith(".class")) {
                InputStream is = null;
                PrintWriter out = null;
                try {
                    is = zip.getInputStream(zipEntry);
                    ClassReader r = new ClassReader(is);
                    out = new PrintWriter(new OutputStreamWriter(FileUtils.openOutputStream(new File(output, r
                            .getClassName().replace('.', '/') + ".j")), "UTF-8"));
                    r.accept(new JasminifierClassAdapter(out, null), flags | ClassReader.EXPAND_FRAMES);
                } catch (IOException ioe) {
                    System.err.println("error in " + zipEntry.getName());
                    ioe.printStackTrace(System.err);
                } finally {
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }
}
