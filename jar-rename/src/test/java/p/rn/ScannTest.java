package p.rn;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import p.rn.ClassInfo.MemberInfo;

public class ScannTest {
    @Test
    public void test() throws IOException {
        File jars = new File("src/test/resources");
        if (jars.exists()) {
            for (File file : FileUtils.listFiles(jars, new String[] { "jar" }, false)) {
                Map<String, MemberInfo> members = Scann.scanBridge(file);
                for (Map.Entry<String, MemberInfo> e : members.entrySet()) {
                    System.out.println(e.getKey() + "=" + e.getValue().name);
                }
            }
        }
    }
}
