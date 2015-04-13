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
import com.googlecode.d2j.visitors.DexFileVisitor;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

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
    public void smaliFile(Path path, DexFileVisitor dcv) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ANTLRStringStream is = new ANTLRReaderStream(reader);
            is.name = path.toString();
            smali0(dcv, is);
        }
    }

    public void smaliFile(String name, InputStream in, DexFileVisitor dcv) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            ANTLRStringStream is = new ANTLRReaderStream(reader);
            is.name = name;
            smali0(dcv, is);
        }
    }

    public DexClassNode smaliFile2Node(String name, InputStream in) throws IOException {
        DexFileNode dfn = new DexFileNode();
        smaliFile(name, in, dfn);
        return dfn.clzs.size() > 0 ? dfn.clzs.get(0) : null;
    }

    private void smali0(DexFileVisitor dcv, ANTLRStringStream is) throws IOException {
        SmaliLexer lexer = new SmaliLexer(is);
        CommonTokenStream ts = new CommonTokenStream(lexer);
        SmaliParser parser = new SmaliParser(ts);
        try {
            parser.accept(dcv);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }
    }

    public void smaliFile(String fileName, char[] data, DexFileVisitor dcv) throws IOException {
        // System.err.println("parsing " + f.getAbsoluteFile());
        ANTLRStringStream is = new ANTLRStringStream(data, data.length);
        is.name = fileName;
        smali0(dcv, is);

    }

    public void smali(Path base, final DexFileVisitor dfv) throws IOException {
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
