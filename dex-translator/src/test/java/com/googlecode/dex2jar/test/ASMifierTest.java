package com.googlecode.dex2jar.test;

import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.util.ASMifierFileV;
import com.googlecode.dex2jar.tools.BaseCmd;
import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.junit.Test;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class ASMifierTest {

    public static String getBaseName(String fn) {
        int x = fn.lastIndexOf('.');
        return x >= 0 ? fn.substring(0, x) : fn;
    }

    @Test
    public void test() throws Exception {
        try {
            for (Path f : TestUtils.listTestDexFiles()) {
                System.out.println("asmifier file " + f);
                File distDir = new File("target", getBaseName(f.getFileName().toString()) + "_asmifier.zip");
                try (FileSystem fs = BaseCmd.createZip(distDir.toPath())) {
                    ASMifierFileV.doFile(f, fs.getPath("/"));
                }
            }
        } catch (Exception e) {
            DexFileReader.niceExceptionMessage(e, 0);
            throw e;
        }
    }

}
