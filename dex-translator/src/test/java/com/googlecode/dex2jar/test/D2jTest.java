package com.googlecode.dex2jar.test;

import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class D2jTest {
    @ParameterizedTest
    @MethodSource("findDexFileClassArgs")
    void test(ArgumentContainer args) {
        try {
            TestUtils.translateAndCheck(args.containingFile, args.cls);
        } catch (Exception ex) {
            fail(ex);
        }
    }

    public static Stream<Arguments> findDexFileClassArgs() {
        Collection<Path> files = TestUtils.listTestDexFiles();
        return files.stream()
                .flatMap(p -> Stream.of(readDex(p))
                        .flatMap(f -> f.clzs.stream()
                                .map(c -> new ArgumentContainer(p, f, c))))
                .map(Arguments::of);
    }

    private static DexFileNode readDex(Path f) {
        DexFileNode fileNode = new DexFileNode();
        DexFileReader reader;
        try {
            reader = new DexFileReader(ZipUtil.readDex(f));
        } catch (IOException e) {
            throw new RuntimeException("Fail to read dex:" + f);
        }
        reader.accept(fileNode);
        return fileNode;
    }

}
