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
        Properties p = new Properties();
        p.load(BinGen.class.getResourceAsStream("class.cfg"));

        String bat = FileUtils.readFileToString(new File(
                "src/main/resources/com/googlecode/dex2jar/bin_gen/bat_template"), "UTF-8");
        String sh = FileUtils.readFileToString(
                new File("src/main/resources/com/googlecode/dex2jar/bin_gen/sh_template"), "UTF-8");

        File binDir = new File("src/main/bin");
        String setclasspath = FileUtils.readFileToString(new File(
                "src/main/resources/com/googlecode/dex2jar/bin_gen/setclasspath.bat"), "UTF-8");
        FileUtils.writeStringToFile(new File(binDir, "setclasspath.bat"), setclasspath, "UTF-8");
        for (Object key : p.keySet()) {
            String name = key.toString();
            FileUtils.writeStringToFile(new File(binDir, key.toString() + ".sh"),
                    sh.replaceAll("__@class_name@__", p.getProperty(name)), "UTF-8");
            FileUtils.writeStringToFile(new File(binDir, key.toString() + ".bat"),
                    bat.replaceAll("__@class_name@__", p.getProperty(name)), "UTF-8");
        }
    }
}
