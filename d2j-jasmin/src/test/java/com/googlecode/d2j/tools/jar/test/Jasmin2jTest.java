package com.googlecode.d2j.tools.jar.test;

import com.googlecode.d2j.jasmin.JasminDumper;
import com.googlecode.d2j.jasmin.Jasmins;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.tree.ClassNode;

import static org.junit.jupiter.api.Assertions.*;

public class Jasmin2jTest {

    public static Stream<Arguments> findJasms() {
        URL url = Jasmin2jTest.class.getResource("/jasmins/type.j");
        assertNotNull(url, "Could not find Jasm resource directory");
        try {
            Path jasmDir = Paths.get(url.toURI()).getParent();
            List<Path> paths = new ArrayList<>();
            Files.walkFileTree(jasmDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".j"))
                        paths.add(file);
                    return super.visitFile(file, attrs);
                }
            });
            return paths.stream()
                    .map(Arguments::of);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("findJasms")
    void test(Path jasmPath) {
        ClassNode cn = assertDoesNotThrow(() -> Jasmins.parse(jasmPath));
        JasminDumper dumper = new JasminDumper(new PrintWriter(System.out, true));
        dumper.dump(cn);
    }
}
