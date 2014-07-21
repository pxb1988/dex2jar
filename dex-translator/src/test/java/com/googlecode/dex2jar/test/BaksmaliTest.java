package com.googlecode.dex2jar.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

import com.googlecode.dex2jar.tools.BaseCmd;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import com.googlecode.d2j.smali.Baksmali;
import com.googlecode.d2j.smali.BaksmaliDexFileVisitor;
import com.googlecode.d2j.smali.BaksmaliDumper;
import com.googlecode.d2j.smali.Smali;

public class BaksmaliTest {
    @Test
    public void t() throws IOException, RecognitionException {

        List<Path> files = TestUtils.listTestDexFiles();

        for (Path f : files) {
            Path smali0 = new File("target/" + f.getFileName() + "-smali0.zip").toPath();
            try (FileSystem fs0 = BaseCmd.createZip(smali0)) {
                Baksmali.from(f).to(fs0.getPath("/"));
            }
            Path smali1 = new File("target/" + f.getFileName() + "-smali1.zip").toPath();
            try (FileSystem fs0 = BaseCmd.openZip(smali0); FileSystem fs1 = BaseCmd.createZip(smali1)) {
                BaksmaliDumper baksmaliDumper = new BaksmaliDumper();
                BaksmaliDexFileVisitor v = new BaksmaliDexFileVisitor(fs1.getPath("/"), baksmaliDumper);
                new Smali().smali(fs0.getPath("/"), v);
            }
        }
    }
}
