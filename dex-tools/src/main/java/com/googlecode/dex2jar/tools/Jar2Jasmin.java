package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;

public class Jar2Jasmin {
    private static final Options options = new Options();;
    static {
        options.addOption(new Option("d", "debug", false, "disassemble debug info"));
        options.addOption(new Option("o", "output", true,
                "output dir of .j files, default is $current_dir/[jar-name]-2jasmin"));
        options.addOption(new Option("f", "force", false, "force overwrite"));
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
        formatter.printHelp("d2j-jar2jasmin [options] <jar>", "disassemble .class in jar file to jasmin file", options,
                "");
    }

    public static void main(String[] args) {
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

        File jar = new File(remainingArgs[0]);
        if (!jar.exists() || !jar.isFile()) {
            System.err.println(jar + " is not exists");
            usage();
            return;
        }
        File output = null;
        boolean force = false;
        boolean debug = false;
        for (Option option : commandLine.getOptions()) {
            String opt = option.getOpt();
            switch (opt.charAt(0)) {
            case 'o': {
                String v = commandLine.getOptionValue("o");
                if (v != null) {
                    output = new File(v);
                }
            }
                break;
            case 'f': {
                force = true;
            }
                break;
            case 'd': {
                debug = true;
            }
                break;
            }
        }

        if (output == null) {
            output = new File(FilenameUtils.getBaseName(jar.getName()) + "2jasmin/");
        }

        if (output.exists() && !force) {
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
        int flags = debug ? 0 : ClassReader.SKIP_DEBUG;
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
        System.out.println("Done.");
    }
}
