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
package com.googlecode.d2j.smali;

import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.smali.antlr4.SmaliLexer;
import com.googlecode.d2j.smali.antlr4.SmaliParser;
import com.googlecode.d2j.visitors.DexFileVisitor;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Smali {
    public static void smaliFile(Path path, DexFileVisitor dcv) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ANTLRInputStream is = new ANTLRInputStream(reader);
            is.name = path.toString();
            smali0(dcv, is);
        }
    }

    public static void smaliFile(String name, String buff, DexFileVisitor dcv) throws IOException {
        ANTLRInputStream is = new ANTLRInputStream(buff);
        is.name = name;
        smali0(dcv, is);
    }

    public static void smaliFile(String name, InputStream in, DexFileVisitor dcv) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            ANTLRInputStream is = new ANTLRInputStream(reader);
            is.name = name;
            smali0(dcv, is);
        }
    }

    public static DexClassNode smaliFile2Node(String name, InputStream in) throws IOException {
        DexFileNode dfn = new DexFileNode();
        smaliFile(name, in, dfn);
        return dfn.clzs.size() > 0 ? dfn.clzs.get(0) : null;
    }

    public static DexClassNode smaliFile2Node(String name, String buff) throws IOException {
        DexFileNode dfn = new DexFileNode();
        smaliFile(name, buff, dfn);
        return dfn.clzs.size() > 0 ? dfn.clzs.get(0) : null;
    }

    private static void smali0(DexFileVisitor dcv, CharStream is) throws IOException {
        SmaliLexer lexer = new SmaliLexer(is);
        CommonTokenStream ts = new CommonTokenStream(lexer);
        SmaliParser parser = new SmaliParser(ts);

        SmaliParser.SFileContext ctx = parser.sFile();
        AntlrSmaliUtil.acceptFile(ctx, dcv);
    }

    public static void smaliFile(String fileName, char[] data, DexFileVisitor dcv) throws IOException {
        // System.err.println("parsing " + f.getAbsoluteFile());
        ANTLRInputStream is = new ANTLRInputStream(data, data.length);
        is.name = fileName;
        smali0(dcv, is);
    }

    public static void smali(Path base, final DexFileVisitor dfv) throws IOException {
        if (Files.isDirectory(base)) {
            Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path fn = dir.getFileName();
                    if (fn != null && fn.toString().startsWith(".")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    smaliFile(file, dfv);
                    return super.visitFile(file, attrs);
                }
            });
        } else if (Files.isRegularFile(base)) {
            smaliFile(base, dfv);
        }
    }
}
