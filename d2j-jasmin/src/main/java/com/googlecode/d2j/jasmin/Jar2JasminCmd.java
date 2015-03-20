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
package com.googlecode.d2j.jasmin;

import com.googlecode.dex2jar.tools.BaseCmd;
import com.googlecode.dex2jar.tools.BaseCmd.Syntax;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Syntax(cmd = "d2j-jar2jasmin", syntax = "[options] <jar>", desc = "Disassemble .class in jar file to jasmin file", onlineHelp = "https://sourceforge.net/p/dex2jar/wiki/Jasmin")
public class Jar2JasminCmd extends BaseCmd {
    @Opt(opt = "d", longOpt = "debug", hasArg = false, description = "disassemble debug info")
    private boolean debugInfo = false;
    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output dir of .j files, default is $current_dir/[jar-name]-jar2jasmin/", argName = "out-dir")
    private Path output;
    @Opt(opt = "e", longOpt = "encoding", description = "encoding for .j files, default is UTF-8", argName = "enc")
    private String encoding = "UTF-8";

    public static void main(String... args) {
        new Jar2JasminCmd().doMain(args);
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }

        Path jar = new File(remainingArgs[0]).toPath().toAbsolutePath();
        if (!Files.exists(jar)) {
            System.err.println(jar + " is not exists");
            usage();
            return;
        }

        if (output == null) {
            output = new File(getBaseName(jar) + "-jar2jasmin/").toPath();
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        System.out.println("disassemble " + jar + " -> " + output);

        if (!output.toString().endsWith(".jar") && !output.toString().endsWith(".apk")) {
            disassemble0(jar, output);
        } else {
            try (FileSystem fs = createZip(output)) {
                disassemble0(jar, fs.getPath("/"));
            }
        }
    }

    private void disassemble0(Path in, final Path output) throws IOException, URISyntaxException {
        if (Files.isDirectory(in)) { // a dir
            travelFileTree(in, output);
        } else if (in.toString().endsWith(".class")) {
            disassemble1(in, output);
        } else {
            try (FileSystem fs = openZip(in)) {
                travelFileTree(fs.getPath("/"), output);
            }
        }
    }

    private void travelFileTree(Path in, final Path output) throws IOException {
        Files.walkFileTree(in, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".class")) {
                    disassemble1(file, output);
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    private void disassemble1(Path file, Path output) throws IOException {
        ClassReader r = new ClassReader(Files.readAllBytes(file));
        Path jFile = output.resolve(r.getClassName().replace('.', '/') + ".j");
        createParentDirectories(jFile);
        try (BufferedWriter out = Files.newBufferedWriter(jFile, Charset.forName(encoding))) {
            PrintWriter pw = new PrintWriter(out);
            ClassNode node = new ClassNode();
            r.accept(node, (debugInfo ? 0 : ClassReader.SKIP_DEBUG) | ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES);
            new JasminDumper(pw).dump(node);
            pw.flush();
        }
    }
}
