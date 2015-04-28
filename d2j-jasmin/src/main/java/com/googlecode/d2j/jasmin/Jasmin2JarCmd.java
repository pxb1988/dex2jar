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
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@BaseCmd.Syntax(cmd = "d2j-jasmin2jar", syntax = "[options] <jar>", desc = "Assemble .j files to .class file", onlineHelp = "https://sourceforge.net/p/dex2jar/wiki/Jasmin")
public class Jasmin2JarCmd extends BaseCmd implements Opcodes {
    private static int versions[] = { 0, V1_1, V1_2, V1_3, V1_4, V1_5, V1_6, V1_7, 52 // V1_8 ?
            , 53 // V1_9 ?
    };
    @Opt(opt = "g", longOpt = "autogenerate-linenumbers", hasArg = false, description = "autogenerate-linenumbers")
    boolean autogenLines = false;
    @Opt(opt = "f", longOpt = "force", hasArg = false, description = "force overwrite")
    private boolean forceOverwrite = false;
    @Opt(opt = "o", longOpt = "output", description = "output .jar file, default is $current_dir/[jar-name]-jasmin2jar.jar", argName = "out-jar-file")
    private Path output;
    @Opt(opt = "e", longOpt = "encoding", description = "encoding for .j files, default is UTF-8", argName = "enc")
    private String encoding = "UTF-8";

    @Opt(opt = "d", longOpt = "dump", description = "dump to stdout", hasArg = false)
    private boolean dump;

    @Opt( longOpt = "no-compute-max", description = "", hasArg = false)
    private boolean noComputeMax;

    @Opt(opt = "cv", longOpt = "class-version", description = "default .class version, [1~9], default 6 for JAVA6")
    private int classVersion = 6;

    public static void main(String... args) throws ClassNotFoundException, SecurityException {
        new Jasmin2JarCmd().doMain(args);
    }

    @Override
    protected void doCommandLine() throws Exception {
        if (remainingArgs.length != 1) {
            usage();
            return;
        }
        if (classVersion < 1 || classVersion > 9) {
            throw new HelpException("-cv,--class-version out of range, 1-9 is supported.");
        }

        Path jar = new File(remainingArgs[0]).toPath().toAbsolutePath();
        if (!Files.exists(jar)) {
            System.err.println(jar + " is not exists");
            usage();
            return;
        }

        if (output == null) {
            output = new File(getBaseName(jar) + "-jasmin2jar/").toPath();
        }

        if (Files.exists(output) && !forceOverwrite) {
            System.err.println(output + " exists, use --force to overwrite");
            usage();
            return;
        }

        System.out.println("assemble " + jar + " -> " + output);

        if (!output.toString().endsWith(".jar") && !output.toString().endsWith(".zip")) {
            assemble0(jar, output);
        } else {
            try (FileSystem fs = createZip(output)) {
                assemble0(jar, fs.getPath("/"));
            }
        }
    }

    private void assemble0(Path in, Path output) throws IOException, URISyntaxException {
        if (Files.isDirectory(in)) { // a dir
            travelFileTree(in, output);
        } else if (in.toString().endsWith(".j")) {
            assemble1(in, output);
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
                if (file.getFileName().toString().endsWith(".j")) {
                    assemble1(file, output);
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    private void assemble1(Path file, Path output) throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(file, Charset.forName(encoding))) {
            ANTLRStringStream is = new ANTLRReaderStream(bufferedReader);
            is.name = file.toString();
            JasminLexer lexer = new JasminLexer(is);
            CommonTokenStream ts = new CommonTokenStream(lexer);
            JasminParser parser = new JasminParser(ts);
            parser.rebuildLine = autogenLines;
            ClassWriter cw = new ClassWriter(noComputeMax?0:ClassWriter.COMPUTE_MAXS);
            ClassNode cn = parser.parse();
            if (cn.version == 0) {
                cn.version = versions[classVersion];
            }
            if (dump) {
                new JasminDumper(new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)).dump(cn);
            }
            cn.accept(cw);
            Path clzFile = output.resolve(cn.name.replace('.', '/') + ".class");
            createParentDirectories(clzFile);
            Files.write(clzFile, cw.toByteArray());
        } catch (RecognitionException e) {
            System.err.println("Fail to assemble " + file);
            e.printStackTrace();
        }
    }
}
