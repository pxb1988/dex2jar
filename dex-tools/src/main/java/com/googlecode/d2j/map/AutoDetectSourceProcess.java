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
package com.googlecode.d2j.map;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import com.googlecode.dex2jar.tools.BaseCmd;

public class AutoDetectSourceProcess {

    public final void process(String file) throws IOException {
        Path path = new File(file).toPath();
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    onFile(file, true);
                    return FileVisitResult.CONTINUE;
                }

            });
        } else if (Files.isRegularFile(path)) {
            onFile(path, true);
        }
    }

    void onFile(Path file, boolean unzip) throws IOException {
        String name = file.getFileName().toString().toLowerCase();
        if (unzip) {
            if (name.endsWith(".apk") || name.endsWith(".zip") || name.endsWith(".jar")) {
                try (FileSystem fs = BaseCmd.openZip(file)) {
                    Path dex = fs.getPath("/", "classes.dex");
                    if (Files.exists(dex)) {
                        onFile(dex, true);
                    } else {
                        for (Path root : fs.getRootDirectories()) {
                            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                        throws IOException {
                                    onFile(file, false);
                                    return FileVisitResult.CONTINUE;
                                }

                            });
                        }
                    }
                }
            } else if (name.endsWith(".dex")) {
                onDex(file);
            }
        }
        if (name.endsWith(".class")) {
            onClass(file);
        } else if (name.endsWith(".j")) {
            onJasmin(file);
        } else if (name.endsWith(".smali")) {
            onSmali(file);
        }
    }

    protected void onDex(Path file) throws IOException {

    }

    protected void onClass(Path file) throws IOException {

    }

    protected void onSmali(Path file) throws IOException {

    }

    protected void onJasmin(Path file) throws IOException {

    }

}
