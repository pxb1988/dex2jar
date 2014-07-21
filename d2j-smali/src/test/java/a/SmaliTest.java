package a;

import com.googlecode.d2j.dex.writer.DexFileWriter;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.smali.BaksmaliDumpOut;
import com.googlecode.d2j.smali.BaksmaliDumper;
import com.googlecode.d2j.smali.Smali;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SmaliTest {
    @Test
    public void test() throws IOException {
        DexFileNode dfn = new DexFileNode();
        new Smali().smaliFile(new File("src/test/resources/a.smali").toPath(), dfn);
        for (DexClassNode dcn : dfn.clzs) {
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(System.out));
            new BaksmaliDumper(true, true).baksmaliClass(dcn, new BaksmaliDumpOut(w));
            w.flush();
        }
    }
}
