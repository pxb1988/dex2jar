package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

public class Jasmin2Jar {
    private static final Options options = new Options();;
    static {
        options.addOption(new Option("o", "output", true,
                "output .jar file, default is $current_dir/[jar-name]-jasmin2jar.jar"));
        options.addOption(new Option("f", "force", false, "force overwrite"));
        options.addOption(new Option("g", "autogenerate-linenumbers", false, "autogenerate-linenumbers"));
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
        formatter.printHelp("d2j-jasmin2jar [options] <dir>", "assemble .j files to .class file", options, "");
    }

    /**
     * @param args
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            usage();
            return;
        }
        String[] remainingArgs = commandLine.getArgs();
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        File dir = new File(remainingArgs[0]);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println(dir + " is not exists");
            usage();
            return;
        }

        File output = null;
        boolean force = false;
        boolean autogenLines = false;
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
            case 'g': {
                autogenLines = true;
            }
                break;
            }
        }
        if (output == null) {
            output = new File(FilenameUtils.getBaseName(dir.getName()) + "-jasmin2jar.jar");
        }

        if (output.exists() && !force) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }
        Class<?> clz = Class.forName("jasmin.ClassFile");
        Method readJasmin = clz.getMethod("readJasmin", Reader.class, String.class, boolean.class);
        Method errorCount = clz.getMethod("errorCount");
        Method getClassName = clz.getMethod("getClassName");
        Method write = clz.getMethod("write", OutputStream.class);

        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(FileUtils.openOutputStream(output));
            for (File f : FileUtils.listFiles(dir, new String[] { "j" }, true)) {
                Object classFile = clz.newInstance();
                Reader reader = null;
                try {
                    reader = new InputStreamReader(FileUtils.openInputStream(f), "UTF-8");
                    readJasmin.invoke(classFile, reader, f.getName(), autogenLines);
                } finally {
                    IOUtils.closeQuietly(reader);
                }
                int errorcount = (Integer) errorCount.invoke(classFile);
                if (errorcount > 0) {
                    System.err.println(f + ": Found " + errorcount + " errors");
                    return;
                }
                String clzName = (String) getClassName.invoke(classFile);
                ZipEntry e = new ZipEntry(clzName.replace('.', '/') + ".class");
                zos.putNextEntry(e);
                write.invoke(classFile, zos);
                zos.closeEntry();
            }
        } catch (Exception ex) {
        } finally {
            IOUtils.closeQuietly(zos);
        }

    }

}
