package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;

public class Jar2Dex {
    private static final Options options = new Options();;
    static {
        options.addOption(new Option("o", "output", true,
                "output .dex file, default is $current_dir/[jar-name]-jar2dex.dex"));
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
        formatter.printHelp("d2j-jar2dex [options] <dir>", "invoke dx to convert jar to dex", options, "");
    }

    /**
     * @param args
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IOException
     */
    public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ClassNotFoundException, NoSuchMethodException, SecurityException, IOException {
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

        File jar = new File(remainingArgs[0]);
        if (!jar.exists() || !jar.isFile()) {
            System.err.println(jar + " is not exists");
            usage();
            return;
        }

        File output = null;
        boolean force = false;
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
            }
        }
        if (output == null) {
            output = new File(FilenameUtils.getBaseName(jar.getName()) + "-jar2dex.dex");
        }

        if (output.exists() && !force) {
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
