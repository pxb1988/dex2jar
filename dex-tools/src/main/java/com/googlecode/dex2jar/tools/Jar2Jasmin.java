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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;

import p.rn.util.FileOut;
import p.rn.util.FileOut.OutHandler;
import p.rn.util.FileWalker;
import p.rn.util.FileWalker.StreamHandler;
import p.rn.util.FileWalker.StreamOpener;

public class Jar2Jasmin extends BaseCmd {
    public static void main(String[] args) {
        new Jar2Jasmin().doMain(args);
    }

    @Opt(opt = "d", longOpt = "debug", hasArg = false, description = "disassemble debug info")
    private boolean debugInfo = false;
    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output dir of .j files, default is $current_dir/[jar-name]-jar2jasmin/", argName = "out-dir")
    private File output;
    @Opt(opt = "e", longOpt = "encoding", description = "encoding for .j files, default is UTF-8", argName = "enc")
    private String encoding = "UTF-8";

    public Jar2Jasmin() {
        super("d2j-jar2jasmin [options] <jar>", "Disassemble .class in jar file to jasmin file");
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        File jar = new File(remainingArgs[0]);
        if (!jar.exists()) {
            System.err.println(jar + " is not exists");
            usage();
            return;
        }

        if (output == null) {
            if (jar.isDirectory()) {
                output = new File(jar.getName() + "-jar2jasmin/");
            } else {
                output = new File(FilenameUtils.getBaseName(jar.getName()) + "-jar2jasmin/");
            }
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        System.out.println("disassemble " + jar + " -> " + output);
        final int flags = debugInfo ? 0 : ClassReader.SKIP_DEBUG;
        final OutHandler fo = FileOut.create(output, false);
        try {
            new FileWalker().withStreamHandler(new StreamHandler() {

                @Override
                public void handle(boolean isDir, String name, StreamOpener current, Object nameObject)
                        throws IOException {
                    if (isDir || !name.endsWith(".class")) {
                        return;
                    }

                    OutputStream os = null;
                    PrintWriter out = null;
                    try {
                        InputStream is = current.get();
                        ClassReader r = new ClassReader(is);
                        os = fo.openOutput(r.getClassName().replace('.', '/') + ".j", nameObject);
                        out = new PrintWriter(new OutputStreamWriter(os, encoding));
                        r.accept(new JasminifierClassAdapter(out, null), flags | ClassReader.EXPAND_FRAMES);
                    } catch (IOException ioe) {
                        System.err.println("error in " + name);
                        ioe.printStackTrace(System.err);
                    } finally {
                        IOUtils.closeQuietly(out);
                        IOUtils.closeQuietly(os);
                    }
                }
            }).walk(jar);
        } finally {
            IOUtils.closeQuietly(fo);
        }
    }
}
