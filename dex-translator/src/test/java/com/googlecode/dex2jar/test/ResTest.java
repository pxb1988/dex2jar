package com.googlecode.dex2jar.test;

import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.reader.DexFileReader;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class ResTest {
    private static File dir;

    public static class FileSet {
        String name;
        List<Path> files = new ArrayList<>(3);
    }

    @BeforeAll
    static void setup() {
        Class<?> testClass = ResTest.class;
        URL url = testClass.getResource("/" + testClass.getName().replace('.', '/') + ".class");
        assertNotNull(url);
        String file = url.getFile();
        assertNotNull(file);
        String dirx = file.substring(0, file.length() - testClass.getName().length() - ".class".length());
        dir = new File(dirx, "res");
    }

    @ParameterizedTest
    @MethodSource("findFileSets")
    void test(FileSet fileSet) {
        try {
            File dex = TestUtils.dexP(fileSet.files, new File(dir, fileSet.name + ".dex"));
            DexFileNode fileNode = new DexFileNode();
            DexFileReader r = new DexFileReader(dex);
            r.accept(fileNode);
            for (DexClassNode classNode : fileNode.clzs) {
                TestUtils.translateAndCheck(fileNode, classNode);
            }
        } catch (Exception ex) {
            fail(ex);
        }
    }

    public static Stream<Arguments> findFileSets() {
        Map<String, FileSet> m = new HashMap<>();
        for (Path f : TestUtils.listPath(dir, ".class")) {
            String name = getBaseName(f.getFileName().toString());

            int i = name.indexOf('$');
            String z = i > 0 ? name.substring(0, i) : name;
            FileSet fs = m.get(z);
            if (fs == null) {
                fs = new FileSet();
                fs.name = z;
                m.put(z, fs);
            }
            fs.files.add(f);
        }
        return m.values().stream()
                .map(Arguments::of);
    }

    public static String getBaseName(String fn) {
        int x = fn.lastIndexOf('.');
        return x >= 0 ? fn.substring(0, x) : fn;
    }
}
