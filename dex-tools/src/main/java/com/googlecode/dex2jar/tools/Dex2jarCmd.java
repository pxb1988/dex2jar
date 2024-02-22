package com.googlecode.dex2jar.tools;

import com.googlecode.d2j.dex.Dex2jar;
import com.googlecode.d2j.reader.BaseDexFileReader;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.MultiDexFileReader;
import com.googlecode.dex2jar.ir.ET;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@BaseCmd.Syntax(cmd = "d2j-dex2jar", syntax = "[options] <file0> [file1 ... fileN]", desc = "convert dex to jar")
public class Dex2jarCmd extends BaseCmd {

    public static void main(String... args) {
        new Dex2jarCmd().doMain(args);
        //new Dex2jarCmd().doMain("app-debug.apk", "--force");
    }

    @Opt(opt = "e", longOpt = "exception-file", description = "detail exception file, default is "
            + "$current_dir/[file-name]-error.zip", argName = "file")
    private Path exceptionFile;

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;

    @Opt(opt = "n", longOpt = "not-handle-exception", hasArg = false, description = "not handle any exceptions thrown"
            + " by dex2jar")
    private boolean notHandleException = false;

    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is $current_dir/[file-name]-dex2jar"
            + ".jar", argName = "out-jar-file")
    private Path output;

    @Opt(opt = "r", longOpt = "reuse-reg", hasArg = false, description = "reuse register while generate java .class "
            + "file")
    private boolean reuseReg = false;

    @Opt(opt = "s", hasArg = false, description = "same with --topological-sort/-ts")
    private boolean topologicalSort1 = false;

    @Opt(opt = "ts", longOpt = "topological-sort", hasArg = false, description = "sort block by topological, that "
            + "will generate more readable code, default enabled")
    private boolean topologicalSort = false;

    @Opt(opt = "d", longOpt = "debug-info", hasArg = false, description = "translate debug info")
    private boolean debugInfo = false;

    @Opt(opt = "p", longOpt = "print-ir", hasArg = false, description = "print ir to System.out")
    private boolean printIR = false;

    @Opt(opt = "os", longOpt = "optmize-synchronized", hasArg = false, description = "optimize-synchronized")
    private boolean optmizeSynchronized = false;

    @Opt(longOpt = "skip-exceptions", hasArg = false, description = "skip-exceptions")
    private boolean skipExceptions = false;

    @Opt(opt = "nc", longOpt = "no-code", hasArg = false, description = "")
    private boolean noCode = false;

    @Opt(opt = "dsn", longOpt = "dont-sanitize-names", hasArg = false, description = "do not replace '_' by '-'")
    private boolean dontSanitizeNames = false;

    @Opt(opt = "cf", longOpt = "compute-frames", hasArg = false,
            description = "instructs ASM to compute frames - experimental!")
    private boolean computeFrames = false;

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length == 0) {
            usage();
            return;
        }

        if ((exceptionFile != null || output != null) && remainingArgs.length != 1) {
            System.err.println("-e/-o can only used with one file");
            return;
        }
        if (debugInfo && reuseReg) {
            System.err.println("-d/-r can not use together");
            return;
        }

        Path currentDir = new File(".").toPath();

        if (output != null) {
            if (Files.exists(output) && !forceOverwrite) {
                System.err.println(output + " exists, use --force to overwrite");
                return;
            }
        } else {
            for (String fileName : remainingArgs) {
                Path file = currentDir.resolve(getBaseName(new File(fileName).toPath()) + "-dex2jar.jar");
                if (Files.exists(file) && !forceOverwrite) {
                    System.err.println(file + " exists, use --force to overwrite");
                    return;
                }
            }
        }

        for (String fileName : remainingArgs) {
            // long baseTS = System.currentTimeMillis();
            String baseName = getBaseName(new File(fileName).toPath());
            Path file = output == null ? currentDir.resolve(baseName + "-dex2jar.jar") : output;
            System.err.println("dex2jar " + fileName + " -> " + file);

            BaseDexFileReader reader = MultiDexFileReader.open(Files.readAllBytes(new File(fileName).toPath()));
            BaksmaliBaseDexExceptionHandler handler = notHandleException ? null : new BaksmaliBaseDexExceptionHandler();
            Dex2jar.from(reader).withExceptionHandler(handler).reUseReg(reuseReg).topoLogicalSort()
                    .skipDebug(!debugInfo).optimizeSynchronized(this.optmizeSynchronized).printIR(printIR)
                    .noCode(noCode).skipExceptions(skipExceptions).dontSanitizeNames(dontSanitizeNames)
                    .computeFrames(computeFrames).to(file);

            if (!notHandleException) {
                if (handler.hasException()) {
                    Path errorFile = exceptionFile == null ? currentDir.resolve(baseName + "-error.zip")
                            : exceptionFile;
                    System.err.println("Detail Error Information in File " + errorFile);
                    System.err.println(BaksmaliBaseDexExceptionHandler.REPORT_MESSAGE);
                    handler.dump(errorFile, originalArgs);
                }
            }
            // long endTS = System.currentTimeMillis();
            // System.err.println(String.format("%.2f", (float) (endTS - baseTS) / 1000));
        }
    }

    @Override
    protected String getVersionString() {
        return "reader-" + DexFileReader.class.getPackage().getImplementationVersion() + ", translator-"
                + Dex2jar.class.getPackage().getImplementationVersion() + ", ir-"
                + ET.class.getPackage().getImplementationVersion();
    }

}
