package com.googlecode.d2j.jasmin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.objectweb.asm.tree.ClassNode;

public final class Jasmins {

    private Jasmins() {
        throw new UnsupportedOperationException();
    }

    public static ClassNode parse(Path file) throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parse(file.toString(), bufferedReader);
        } catch (RecognitionException e) {
            throw new RuntimeException("Failed to assemble " + file, e);
        }
    }

    public static ClassNode parse(String fileName, Reader bufferedReader) throws IOException, RecognitionException {
        ANTLRStringStream is = new ANTLRReaderStream(bufferedReader);
        is.name = fileName;
        JasminLexer lexer = new JasminLexer(is);
        CommonTokenStream ts = new CommonTokenStream(lexer);
        JasminParser parser = new JasminParser(ts);
        return parser.parse();
    }

    public static ClassNode parse(String fileName, InputStream is) throws IOException, RecognitionException {
        return parse(fileName, new InputStreamReader(is, StandardCharsets.UTF_8));
    }

}
