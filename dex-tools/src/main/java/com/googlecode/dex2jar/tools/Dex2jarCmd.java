/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2012 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.tools;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.reader.DexFileReader;
import com.googlecode.dex2jar.v3.Dex2jar;
import com.googlecode.dex2jar.v3.DexExceptionHandlerImpl;
import com.googlecode.dex2jar.v3.Main;

public class Dex2jarCmd extends BaseCmd {

    public static void main(String[] args) {
        new Dex2jarCmd().doMain(args);
    }

    @Opt(opt = "e", longOpt = "exception-file", description = "detail exception file, default is $current_dir/[file-name]-error.zip", argName = "file")
    private File exceptionFile;
    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "n", longOpt = "not-handle-exception", hasArg = false, description = "not handle any exception throwed by dex2jar")
    private boolean notHandleException = false;
    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is $current_dir/[file-name]-dex2jar.jar", argName = "out-jar-file")
    private File output;

    @Opt(opt = "r", longOpt = "reuse-reg", hasArg = false, description = "reuse regiter while generate java .class file")
    private boolean reuseReg = false;
    @Opt(opt = "nc", longOpt = "no-code", hasArg = false, description = "skip code")
    private boolean noCode = false;

    @Opt(opt = "s", hasArg = false, description = "same with --topological-sort/-ts")
    private boolean topologicalSort1 = false;
    @Opt(opt = "v", longOpt = "verbose", hasArg = false, description = "show progress")
    private boolean verbose = false;
    @Opt(opt = "ts", longOpt = "topological-sort", hasArg = false, description = "sort block by topological, that will generate more readable code")
    private boolean topologicalSort = false;

    @Opt(opt = "d", longOpt = "debug-info", hasArg = false, description = "translate debug info")
    private boolean debugInfo = false;

    @Opt(opt = "p", longOpt = "print-ir", hasArg = false, description = "print ir to System.out")
    private boolean printIR = false;

    @Opt(opt = "os", longOpt = "optmize-synchronized", hasArg = false, description = "optmize-synchronized")
    private boolean optmizeSynchronized = false;

    public Dex2jarCmd() {
        super("d2j-dex2jar [options] <file0> [file1 ... fileN]", "convert dex to jar");
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length == 0) {
            throw new HelpException("At least one fileN is required");
        }

        if ((exceptionFile != null || output != null) && remainingArgs.length != 1) {
            throw new HelpException("-e/-o can only used with one file");
        }
        if (debugInfo && reuseReg) {
            throw new HelpException("-d/-r can not use together");
        }

        if (output != null) {
            if (output.exists() && !forceOverwrite) {
                throw new HelpException(output + " exists, use --force to overwrite");
            }
        } else {
            for (String fileName : remainingArgs) {
                if (!new File(fileName).exists()) {
                    throw new HelpException(fileName + " not exists");
                }
                File file = new File(FilenameUtils.getBaseName(fileName) + "-dex2jar.jar");
                if (file.exists() && !forceOverwrite) {
                    throw new HelpException(file + " exists, use --force to overwrite");
                }
            }
        }

        for (String fileName : remainingArgs) {
            File file = output == null ? new File(FilenameUtils.getBaseName(fileName) + "-dex2jar.jar") : output;
            System.err.println("dex2jar " + fileName + " -> " + file);

            DexFileReader reader = new DexFileReader(new File(fileName));
            DexExceptionHandlerImpl handler = notHandleException ? null : new DexExceptionHandlerImpl()
                    .skipDebug(!debugInfo);

            Dex2jar.from(reader).withExceptionHandler(handler).reUseReg(reuseReg)
                    .topoLogicalSort(topologicalSort || topologicalSort1).skipDebug(!debugInfo)
                    .optimizeSynchronized(this.optmizeSynchronized).printIR(printIR).verbose(verbose).noCode(noCode)
                    .to(file);

            if (!notHandleException) {
                Map<Method, Exception> exceptions = handler.getExceptions();
                if (exceptions != null && exceptions.size() > 0) {
                    File errorFile = exceptionFile == null ? new File(FilenameUtils.getBaseName(fileName)
                            + "-error.zip") : exceptionFile;
                    handler.dumpException(reader, errorFile);
                    System.err.println("Detail Error Information in File " + errorFile);
                    System.err
                            .println("Please report this file to http://code.google.com/p/dex2jar/issues/entry if possible.");
                }
            }
        }
    }

    @Override
    protected String getVersionString() {
        return "reader-" + DexFileReader.class.getPackage().getImplementationVersion() + ", translator-"
                + Main.class.getPackage().getImplementationVersion() + ", ir-"
                + ET.class.getPackage().getImplementationVersion();
    }

}
