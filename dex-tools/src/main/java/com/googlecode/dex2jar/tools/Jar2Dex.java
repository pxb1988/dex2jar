package com.googlecode.dex2jar.tools;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@BaseCmd.Syntax(cmd = "d2j-jar2dex", syntax = "[options] <dir>", desc = "Convert jar to dex by invoking dx.")
public class Jar2Dex extends BaseCmd {

    public static void main(String... args) {
        new Jar2Dex().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;

    @Opt(opt = "s", longOpt = "sdk", description = "set minSdkVersion")
    private int minSdkVersion = 13;

    @Opt(opt = "o", longOpt = "output", description = "output .dex file, default is $current_dir/[jar-name]-jar2dex"
            + ".dex", argName = "out-dex-file")
    private Path output;

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        Path jar = new File(remainingArgs[0]).toPath();
        if (!Files.exists(jar)) {
            System.err.println(jar + " doesn't exist");
            usage();
            return;
        }

        if (output == null) {
            if (Files.isDirectory(jar)) {
                output = new File(jar.getFileName() + "-jar2dex.dex").toPath();
            } else {
                output = new File(getBaseName(jar.getFileName().toString()) + "-jar2dex.dex").toPath();
            }
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        Path tmp = null;
        final Path realJar;
        try {
            if (Files.isDirectory(jar)) {
                realJar = Files.createTempFile("d2j", ".jar");
                tmp = realJar;
                System.out.println("zipping " + jar + " -> " + realJar);
                try (FileSystem fs = createZip(realJar)) {
                    final Path outRoot = fs.getPath("/");
                    walkJarOrDir(jar, (file, relative) -> {
                        if (file.getFileName().toString().endsWith(".class")) {
                            Files.copy(file, outRoot.resolve(relative));
                        }
                    });
                }
            } else {
                realJar = jar;
            }

            System.out.println("jar2dex " + realJar + " -> " + output);

            Class<?> c = Class.forName("com.android.dx.command.Main");
            Method m = c.getMethod("main", String[].class);

            String[] ps = new String[]{"--dex", "--no-strict",
                    "--output=" + output.toAbsolutePath(),
                    "--min-sdk-version=" + minSdkVersion,
                    realJar.toAbsolutePath().toString()};
            System.out.println("call com.android.dx.command.Main.main" + Arrays.toString(ps));
            m.invoke(null, (Object) ps);
        } finally {
            if (tmp != null) {
                Files.deleteIfExists(tmp);
            }
        }
    }

}
