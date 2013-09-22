/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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
package com.googlecode.dex2jar.bin_gen;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class BinGen {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("bin-gen cfg-dir out-dir");
            return;
        }
        File cfg = new File(args[0]);
        File out = new File(args[1]);
        Properties p = new Properties();
        p.load(FileUtils.openInputStream(new File(cfg, "class.cfg")));

        String bat = FileUtils.readFileToString(new File(cfg, "bat_template"), "UTF-8");
        String sh = FileUtils.readFileToString(new File(cfg, "sh_template"), "UTF-8");

        for (File file2copy : FileUtils.listFiles(cfg, new String[]{"sh", "bat"}, false)) {
            String content = FileUtils.readFileToString(file2copy, "UTF-8");
            FileUtils.writeStringToFile(new File(out, file2copy.getName()), content, "UTF-8");
        }

        for (Object key : p.keySet()) {
            String name = key.toString();
            FileUtils.writeStringToFile(new File(out, key.toString() + ".sh"),
                    sh.replaceAll("__@class_name@__", p.getProperty(name)), "UTF-8");
            FileUtils.writeStringToFile(new File(out, key.toString() + ".bat"),
                    bat.replaceAll("__@class_name@__", p.getProperty(name)), "UTF-8");
        }
    }
}
