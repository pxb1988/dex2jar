package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Jasmin2Jar extends BaseCmd {
    public static void main(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        new Jasmin2Jar().doMain(args);
    }

    @Opt(opt = "g", longOpt = "autogenerate-linenumbers", hasArg = false, description = "autogenerate-linenumbers")
    boolean autogenLines = false;
    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is $current_dir/[jar-name]-jasmin2jar.jar", argName = "out-jar-file")
    private File output;

    public Jasmin2Jar() {
        super("d2j-jasmin2jar [options] <dir>", "d2j-jasmin2jar - assemble .j files to .class file");
    }

    @Override
    protected void doCommandLine() throws Exception {
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

        if (output == null) {
            output = new File(dir.getName() + "-jasmin2jar.jar");
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        System.out.println("assemble " + dir + " to " + output);

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
        } finally {
            IOUtils.closeQuietly(zos);
        }
    }

}
