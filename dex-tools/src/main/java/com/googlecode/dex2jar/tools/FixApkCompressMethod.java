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
package com.googlecode.dex2jar.tools;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import com.googlecode.d2j.util.zip.AutoSTOREDZipOutputStream;
import com.googlecode.d2j.util.zip.ZipEntry;
import com.googlecode.d2j.util.zip.ZipFile;
import com.googlecode.dex2jar.tools.BaseCmd.Syntax;

@Syntax(cmd = "d2j-fix-api-compress-method", syntax = "[options] <apk>", desc = "fix the compress method in apk")
public class FixApkCompressMethod extends BaseCmd {

    @Opt(opt = "c", longOpt = "config", description = "file list to STORED in apk")
    private Path config;
    @Opt(opt = "o", longOpt = "output", description = "The output file, defualt is c-m-fixed.apk", argName = "out", required = true)
    private Path output;

    public static void main(String[] args) {
        new FixApkCompressMethod().doMain(args);
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length < 1) {
            System.err.println("ERROR: no file to process");
            return;
        }

        System.err.printf("fix %s -> %s\n", remainingArgs[0], output);
        Set<String> toSTORED = new HashSet<>();
        if (config != null) {
            toSTORED.addAll(Files.readAllLines(config, StandardCharsets.UTF_8));
        }
        byte[] buff = new byte[1024];
        try (ZipFile zis = new ZipFile(Files.readAllBytes(new File(remainingArgs[0]).toPath()));
                ZipOutputStream zos = new AutoSTOREDZipOutputStream(Files.newOutputStream(output))) {
            for (ZipEntry entry : zis.entries()) {
                java.util.zip.ZipEntry nEntry = new java.util.zip.ZipEntry(entry.getName());
                if (entry.getMethod() == ZipEntry.STORED || toSTORED.contains(entry.getName())) {
                    nEntry.setMethod(java.util.zip.ZipEntry.STORED);
                    nEntry.setSize(entry.getSize());
                } else {
                    nEntry.setMethod(java.util.zip.ZipEntry.DEFLATED);
                }
                zos.putNextEntry(nEntry);
                if (!entry.isDirectory()) {
                    InputStream is = zis.getInputStream(entry);
                    for (int c = is.read(buff); c > 0; c = is.read(buff)) {
                        zos.write(buff, 0, c);
                    }
                    is.close();
                }
                zos.closeEntry();
            }

        }
    }
}
