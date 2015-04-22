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

import com.googlecode.dex2jar.tools.BaseCmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;

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
        final Path cfg = new File(args[0]).toPath();
        final Path out = new File(args[1]).toPath();
        Properties p = new Properties();
        try (InputStream is = Files.newInputStream(cfg.resolve("class.cfg"))) {
            p.load(is);
        }

        String bat = new String(Files.readAllBytes(cfg.resolve("bat_template")), StandardCharsets.UTF_8);

        String sh = new String(Files.readAllBytes(cfg.resolve("sh_template")), StandardCharsets.UTF_8);

        Files.walkFileTree(cfg, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".sh") || fileName.endsWith(".bat")) {
                    Path f = out.resolve(cfg.relativize(file));
                    BaseCmd.createParentDirectories(f);
                    Files.copy(file, f, StandardCopyOption.REPLACE_EXISTING);
                    if (fileName.endsWith(".sh")) {
                        setExec(f);
                    }
                }
                return super.visitFile(file, attrs);
            }
        });

        for (Object key : p.keySet()) {
            String name = key.toString();
            Path path = out.resolve(key.toString() + ".sh");
            BaseCmd.createParentDirectories(path);
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                String s = sh.replaceAll("__@class_name@__", p.getProperty(name));
                bw.append(s);
            }

            setExec(path);

            path = out.resolve(key.toString() + ".bat");
            BaseCmd.createParentDirectories(path);
            try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                String s = bat.replaceAll("__@class_name@__", p.getProperty(name));
                bw.append(s);
            }
        }
    }

    private static void setExec(Path path) {
        try {
            path.toFile().setExecutable(true);
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rwxr-xr-x"));
        } catch (Exception ex) {
            // ignored
        }
    }
}
