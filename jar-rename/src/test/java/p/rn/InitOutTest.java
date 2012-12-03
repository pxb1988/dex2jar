package p.rn;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import p.rn.name.InitOut;

public class InitOutTest {
    @Test
    public void t() throws IOException {
        File saveToDir = new File("target");
        File jars = new File("src/test/resources");
        if (jars.exists()) {
            for (File file : FileUtils.listFiles(jars, new String[] { "jar" }, false)) {
                new InitOut().from(file).to(new File(saveToDir, file.getName() + ".init.txt"));
            }
        }
    }
}
