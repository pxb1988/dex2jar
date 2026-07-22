package com.googlecode.dex2jar.test;

import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.smali.Smali;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class Smali2jTest {
    @ParameterizedTest
    @MethodSource("findDexFileClassArgs")
    void test(ArgumentContainer args) {
        if (args.allowFailure()) {
            try {
                TestUtils.translateAndCheck(args.containingFile, args.cls);
            } catch (Exception ex) {
                // Failure allowed
                ex.printStackTrace();
            }
        } else {
            try {
                TestUtils.translateAndCheck(args.containingFile, args.cls);
            } catch (Exception ex) {
                fail(ex);
            }
        }
    }

    public static Stream<Arguments> findDexFileClassArgs() {
        URL url = Smali2jTest.class.getResource("/smalis/writeString.smali");
        System.out.println("url is " + url);
        assertNotNull(url);

        final String file = url.getFile();
        assertNotNull(file);

        Path dirxpath = new File(file).toPath();

        final Path basePath = dirxpath.getParent();

        final Set<Path> files = new TreeSet<>();
        try {
            Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".smali")) {
                        files.add(file);
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return files.stream()
                .flatMap(p -> Stream.of(fileFromPath(p))
                        .flatMap(f -> f.clzs.stream()
                                .map(c -> new ArgumentContainer(p, f, c))))
                .map(Arguments::of);
    }

    private static DexFileNode fileFromPath(Path p) {
        final DexFileNode fileNode = new DexFileNode();
        try {
            Smali.smaliFile(p, fileNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileNode;
    }

}
