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

import com.googlecode.d2j.tools.jar.InitOut;

import java.io.File;

public class DeObfInitCmd extends BaseCmd {
    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is $current_dir/[file-name]-deobf-init.txt", argName = "out-file")
    private File output;
    @Opt(opt = "min", longOpt = "min-length", description = "do the rename if the length < MIN, default is 2", argName = "MIN")
    private int min = 2;
    @Opt(opt = "max", longOpt = "max-length", description = "do the rename if the length > MIN, default is 40", argName = "MAX")
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

        File jar = new File(remainingArgs[0]);
        if (!jar.exists()) {
            System.err.println(jar + " is not exists");
            usage();
            return;
        }
        if (output == null) {
            if (jar.isDirectory()) {
                output = new File(jar.getName() + "-deobf-init.txt");
            } else {
                output = new File(getBaseName(jar.getName()) + "-deobf-init.txt");
            }
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }
        System.out.println("generate " + jar + " -> " + output);
        new InitOut().from(jar).maxLength(max).minLength(min).to(output.toPath());
    }

}
