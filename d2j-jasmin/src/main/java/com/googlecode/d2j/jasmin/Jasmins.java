/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.objectweb.asm.tree.ClassNode;

public class Jasmins {
    public static ClassNode parse(Path file) throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            ANTLRStringStream is = new ANTLRReaderStream(bufferedReader);
            is.name = file.toString();
            JasminLexer lexer = new JasminLexer(is);
            CommonTokenStream ts = new CommonTokenStream(lexer);
            JasminParser parser = new JasminParser(ts);
            return parser.parse();
        } catch (RecognitionException e) {
            throw new RuntimeException("Fail to assemble " + file, e);
        }
    }
}
