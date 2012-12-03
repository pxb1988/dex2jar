package p.rn;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import p.rn.name.Renamer;

public class RenamerTest {
    @Test
    public void test() throws IOException {
        File jars = new File("src/test/resources");
        File saveToDir = new File("target");
        if (jars.exists()) {
            for (File file : FileUtils.listFiles(jars, new String[] { "jar" }, false)) {
                new Renamer().from(file).withConfig(new File(saveToDir, file.getName() + ".init.txt"))
                        .to(new File(saveToDir, file.getName()));
            }
        }
    }
}
