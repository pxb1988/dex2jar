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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.googlecode.d2j.util.zip.AutoSTOREDZipOutputStream;
import com.googlecode.dex2jar.tools.BaseCmd.Syntax;

@Syntax(cmd = "d2j-std-zip", syntax = "[options] <zip>", desc = "clean up apk to standard zip")
public class StdApkCmd extends BaseCmd {

    @Opt(opt = "o", longOpt = "output", description = "The output file", argName = "out", required = true)
    private Path output;

    public static void main(String... args) {
        new StdApkCmd().doMain(args);
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length < 1) {
            System.err.println("ERROR: no file to process");
            return;
        }

        System.err.printf("fix %s -> %s\n", remainingArgs[0], output);

        byte[] buffer = new byte[1000];
        try (ZipOutputStream zos = new AutoSTOREDZipOutputStream(Files.newOutputStream(output))) {
            byte[] data = Files.readAllBytes(new File(remainingArgs[0]).toPath());
            try(com.googlecode.d2j.util.zip.ZipFile zipFile = new com.googlecode.d2j.util.zip.ZipFile(data)) {
                for (com.googlecode.d2j.util.zip.ZipEntry e : zipFile.entries()) {
                    ZipEntry nEntry = new ZipEntry(e.getName());

                    nEntry.setMethod(e.getMethod() == com.googlecode.d2j.util.zip.ZipEntry.STORED ? ZipEntry.STORED
                            : ZipEntry.DEFLATED);
                    zos.putNextEntry(nEntry);

                    if (!nEntry.isDirectory()) {
                        try (InputStream is = zipFile.getInputStream(e)) {
                            while (true) {
                                int c = is.read(buffer);
                                if (c < 0) {
                                    break;
                                }
                                zos.write(buffer, 0, c);
                            }
                        }
                    }
                    zos.closeEntry();
                }
            }
            zos.finish();
        }
    }
}
