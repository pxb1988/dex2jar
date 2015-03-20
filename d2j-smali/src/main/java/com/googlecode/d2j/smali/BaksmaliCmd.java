package com.googlecode.d2j.smali;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.googlecode.dex2jar.tools.BaseCmd;
import com.googlecode.dex2jar.tools.BaseCmd.Syntax;

@Syntax(cmd = "d2j-baksmali", syntax = "[options] <dex>", desc = "disassembles and/or dumps a dex file", onlineHelp = "https://sourceforge.net/p/dex2jar/wiki/Smali")
public class BaksmaliCmd extends BaseCmd {
    @Opt(opt = "b", longOpt = "no-debug-info", hasArg = false, description = "[not impl] don't write out debug info (.local, .param, .line, etc.)")
    private boolean noDebug;
    @Opt(opt = "p", longOpt = "no-parameter-registers", hasArg = false, description = "use the v<n> syntax instead of the p<n> syntax for registers mapped to method parameters")
    private boolean noParameterRegisters;
    @Opt(opt = "l", longOpt = "use-locals", hasArg = false, description = "output the .locals directive with the number of non-parameter registers, rather than the .register")
    private boolean useLocals;
    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output dir of .smali files, default is $current_dir/[jar-name]-out/", argName = "out")
    private Path output;

    public static void main(String[] args) {
        new BaksmaliCmd().doMain(args);
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length < 1) {
            System.err.println("ERRPR: no file to process");
            return;
        } else if (remainingArgs.length > 1) {
            System.err.println("ERRPR: too many files to process");
            return;
        }

        File dex = new File(remainingArgs[0]);
        if (!dex.exists()) {
            System.err.println("ERROR: " + dex + " is not exists");
            return;
        }
        if (output == null) {
            output = new File(getBaseName(dex.getName()) + "-out").toPath();
        }
        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            return;
        }
        Baksmali b = Baksmali.from(dex);
        if (noDebug) {
            b.noDebug();
        }
        if (noParameterRegisters) {
            b.noParameterRegisters();
        }
        if (useLocals) {
            b.useLocals();
        }
        System.err.println("baksmali " + dex + " -> " + output);
        b.to(output);
    }
}
