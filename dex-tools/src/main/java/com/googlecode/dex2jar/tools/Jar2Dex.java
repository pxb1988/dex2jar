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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import p.rn.util.FileOut;
import p.rn.util.FileOut.OutHandler;
import p.rn.util.FileWalker;
import p.rn.util.FileWalker.OutAdapter;

public class Jar2Dex extends BaseCmd {
    public static void main(String[] args) {
        new Jar2Dex().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output .dex file, default is $current_dir/[jar-name]-jar2dex.dex", argName = "out-dex-file")
    private File output;

    public Jar2Dex() {
        super("d2j-jar2dex [options] <dir>", "Convert jar to dex by invoking dx.");
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
                output = new File(jar.getName() + "-jar2dex.dex");
            } else {
                output = new File(FilenameUtils.getBaseName(jar.getName()) + "-jar2dex.dex");
            }
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        File realJar;
        if (jar.isDirectory()) {
            realJar = File.createTempFile("d2j", ".jar");
            realJar.deleteOnExit();
            System.out.println("zipping " + jar + " -> " + realJar);
            OutHandler out = FileOut.create(realJar, true);
            try {
                new FileWalker().withStreamHandler(new OutAdapter(out)).walk(jar);
            } finally {
                IOUtils.closeQuietly(out);
            }
        } else {
            realJar = jar;
        }

        System.out.println("jar2dex " + realJar + " -> " + output);

        Class<?> c = Class.forName("com.android.dx.command.Main");
        Method m = c.getMethod("main", String[].class);

        List<String> ps = new ArrayList<String>();
        ps.addAll(Arrays.asList("--dex", "--no-strict", "--output=" + output.getCanonicalPath(),
                realJar.getCanonicalPath()));
        System.out.println("call com.android.dx.command.Main.main" + ps);
        m.invoke(null, new Object[] { ps.toArray(new String[0]) });
    }

}
