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

import jasmin.ClassFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import p.rn.util.FileOut;
import p.rn.util.FileOut.OutHandler;
import p.rn.util.FileWalker;
import p.rn.util.FileWalker.StreamHandler;
import p.rn.util.FileWalker.StreamOpener;

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

    @Opt(opt = "e", longOpt = "encoding", description = "encoding for .j files, default is UTF-8", argName = "enc")
    private String encoding = "UTF-8";

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
        if (!dir.exists()) {
            System.err.println(dir + " is not exists");
            usage();
            return;
        }

        if (output == null) {
            if (dir.isDirectory()) {
                output = new File(dir.getName() + "-jasmin2jar.jar");
            } else {
                output = new File(FilenameUtils.getBaseName(dir.getName()) + "-jasmin2jar.jar");
            }
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        System.out.println("assemble " + dir + " -> " + output);

        final OutHandler fo = FileOut.create(output, true);
        try {
            new FileWalker().withStreamHandler(new StreamHandler() {
                @Override
                public void handle(boolean isDir, String name, StreamOpener current, Object nameObject)
                        throws IOException {
                    if (isDir || !name.endsWith(".j")) {
                        return;
                    }
                    try {
                        ClassFile classFile = new ClassFile();
                        Reader reader = new InputStreamReader(current.get(), encoding);
                        classFile.readJasmin(reader, name, autogenLines);

                        int errorcount = classFile.errorCount();
                        if (errorcount > 0) {
                            System.err.println(name + ": Found " + errorcount + " errors");
                            return;
                        }
                        String clzName = classFile.getClassName();
                        OutputStream os = fo.openOutput(clzName.replace('.', '/') + ".class", nameObject);
                        try {
                            classFile.write(os);
                        } finally {
                            IOUtils.closeQuietly(os);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            }).walk(dir);
        } finally {
            IOUtils.closeQuietly(fo);
        }
    }

}
