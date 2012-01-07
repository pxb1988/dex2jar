package com.googlecode.dex2jar.tools;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class Jar2Dex extends BaseCmd {
    public static void main(String[] args) {
        new Jar2Dex().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output .dex file, default is $current_dir/[jar-name]-jar2dex.dex", argName = "out-dex-file")
    private File output;

    public Jar2Dex() {
        super("d2j-jar2dex [options] <dir>", "Convert jar to dex by invoking dx.");
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
            output = new File(FilenameUtils.getBaseName(jar.getName()) + "-jar2dex.dex");
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }
        Class<?> c = Class.forName("com.android.dx.command.Main");
        Method m = c.getMethod("main", String[].class);

        List<String> ps = new ArrayList<String>();
        ps.addAll(Arrays.asList("--dex", "--no-strict", "--output=" + output.getCanonicalPath(), jar.getCanonicalPath()));
        System.out.println("call com.android.dx.command.Main.main" + ps);
        m.invoke(null, new Object[] { ps.toArray(new String[0]) });
    }

}
