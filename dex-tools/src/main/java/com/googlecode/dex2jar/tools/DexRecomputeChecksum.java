package com.googlecode.dex2jar.tools;

import com.googlecode.d2j.dex.writer.DexFileWriter;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

@BaseCmd.Syntax(cmd = "d2j-dex-recompute-checksum", syntax = "[options] dex", desc = "recompute crc and sha1 of dex.")
public class DexRecomputeChecksum extends BaseCmd {

    public static void main(String... args) {
        new DexRecomputeChecksum().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;

    @Opt(opt = "o", longOpt = "output", description = "output .dex file, default is [dex-name]-rechecksum.dex",
            argName = "out-dex-file")
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
                output = new File(jar.getFileName() + "-rechecksum.dex").toPath();
            } else {
                output = new File(getBaseName(jar.getFileName().toString()) + "-rechecksum.dex").toPath();
            }
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        byte[] data = Files.readAllBytes(jar);

        ByteBuffer b = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(32, data.length);
        DexFileWriter.updateChecksum(b, data.length);
        Files.write(output, data);
    }

}
