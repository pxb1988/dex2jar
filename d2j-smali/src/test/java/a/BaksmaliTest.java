package a;

import com.googlecode.d2j.smali.Baksmali;
import com.googlecode.d2j.smali.BaksmaliDexFileVisitor;
import com.googlecode.d2j.smali.BaksmaliDumper;
import com.googlecode.d2j.smali.Smali;
import com.googlecode.dex2jar.tools.BaseCmd;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class BaksmaliTest {


    @Test
    public void t() throws Exception {
        File dir = new File("../dex-translator/src/test/resources/dexes");
        File[] fs = dir.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.getName().endsWith(".dex") || f.getName().endsWith(".apk")) {
                    dotest(f.toPath());
                }
            }
        }
    }

    private void dotest(Path f) throws Exception {
        Path smali0 = new File("target/" + f.getFileName() + "-smali0.zip").toPath();
        try (FileSystem fs0 = BaseCmd.createZip(smali0)) {
            Baksmali.from(f).to(fs0.getPath("/"));
        }
        Path smali1 = new File("target/" + f.getFileName() + "-smali1.zip").toPath();
        try (FileSystem fs0 = BaseCmd.openZip(smali0); FileSystem fs1 = BaseCmd.createZip(smali1)) {
            BaksmaliDumper baksmaliDumper = new BaksmaliDumper();
            BaksmaliDexFileVisitor v = new BaksmaliDexFileVisitor(fs1.getPath("/"), baksmaliDumper);
            Smali.smali(fs0.getPath("/"), v);
        }
    }
}
