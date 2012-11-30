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

import org.apache.commons.io.FilenameUtils;

import com.googlecode.dex2jar.tools.BaseCmd.Syntax;

import p.rn.name.Renamer;

@Syntax(cmd = "d2j-jar-remap", syntax = "[options] jar", desc = "rename package/class/method/field name in a jar", onlineHelp = "https://code.google.com/p/dex2jar/wiki/DeObfuscateJarWithDexTool")
public class JarRemap extends BaseCmd {
    public static void main(String[] args) {
        new JarRemap().doMain(args);
    }

    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is $current_dir/[jar-name]-remap.jar", argName = "out-jar")
    private File output;
    @Opt(opt = "c", longOpt = "config", required = true, description = "config file for remap, this is REQUIRED", argName = "config")
    private File config;

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
                output = new File(jar.getName() + "-remap.jar");
            } else {
                output = new File(FilenameUtils.getBaseName(jar.getName()) + "-remap.jar");
            }
        }

        if (output.exists() && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        System.out.println("remap " + jar + " -> " + output);
        new Renamer().from(jar).withConfig(config).to(output);
    }
}
