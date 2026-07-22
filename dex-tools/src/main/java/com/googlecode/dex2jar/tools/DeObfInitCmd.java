package com.googlecode.dex2jar.tools;

import com.googlecode.d2j.tools.jar.InitOut;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class DeObfInitCmd extends BaseCmd {

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;

    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is "
            + "$current_dir/[file-name]-deobf-init.txt", argName = "out-file")
    private Path output;

    @Opt(opt = "min", longOpt = "min-length", description = "do the rename if the length < MIN, default is 2",
            argName = "MIN")
    private int min = 2;

    @Opt(opt = "max", longOpt = "max-length", description = "do the rename if the length > MIN, default is 40",
            argName = "MAX")
    private int max = 40;

    public DeObfInitCmd() {
        super("d2j-init-deobf [options] <jar>", "generate an init config file for deObfuscate a jar");
    }

    public static void main(String... args) {
        new DeObfInitCmd().doMain(args);
    }

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
                output = new File(jar.getFileName().toString() + "-deobf-init.txt").toPath();
            } else {
                output = new File(getBaseName(jar.getFileName().toString()) + "-deobf-init.txt").toPath();
            }
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }
        System.out.println("generate " + jar + " -> " + output);
        new InitOut().from(jar).maxLength(max).minLength(min).to(output);
    }

}
