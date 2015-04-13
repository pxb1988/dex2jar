package com.googlecode.dex2jar.tools;

import com.googlecode.d2j.tools.jar.InvocationWeaver;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

@BaseCmd.Syntax(cmd = "d2j-jar-weaver", syntax = "[options] jar", desc = "replace invoke in jar", onlineHelp = "https://sourceforge.net/p/dex2jar/wiki/JarWeaver")
public class JarWeaverCmd extends BaseCmd {
    @Opt(opt = "o", longOpt = "output", description = "output .jar file", argName = "out-jar-file", required = true)
    private Path output;
    @Opt(opt = "c", longOpt = "config", description = "config file", argName = "config", required = true)
    private Path config;
    @Opt(opt = "s", longOpt = "stub-jar", description = "stub jar", argName = "stub")
    private Path stub;

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length == 0) {
            throw new HelpException("no jar");
        }

        InvocationWeaver invocationWeaver = (InvocationWeaver) new InvocationWeaver().withConfig(config);

        try (FileSystem fs = createZip(output)) {
            final Path outRoot = fs.getPath("/");
            for (String str : remainingArgs) {
                Path p = new File(str).toPath();
                System.err.println(p + " -> " + output);
                if (Files.isDirectory(p)) {
                    invocationWeaver.wave(p, outRoot);
                } else {
                    try (FileSystem fs2 = openZip(p)) {
                        invocationWeaver.wave(fs2.getPath("/"), outRoot);
                    }
                }
            }
            if (stub != null) {
                System.err.println(stub + " -> " + output);
                walkJarOrDir(stub, new FileVisitorX() {
                    @Override
                    public void visitFile(Path file, String relative) throws IOException {
                        Path out = outRoot.resolve(relative);
                        if (Files.exists(out)) {
                            System.err.println("skip " + relative + " in " + stub);
                        } else {
                            createParentDirectories(out);
                            Files.copy(file, out);
                        }
                    }
                });
            }
        }
    }

    public static void main(String[] args) {
        new JarWeaverCmd().doMain(args);
    }

}
