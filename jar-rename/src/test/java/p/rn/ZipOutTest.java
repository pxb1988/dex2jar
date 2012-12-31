package p.rn;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import p.rn.util.FileOut;
import p.rn.util.FileOut.OutHandler;

public class ZipOutTest {
    @Test
    public void testI154() throws IOException {
        OutHandler out = FileOut.create(new File("target/zip-out.zip"), true);
        out.write(false, "a/b/c.data", new byte[0], null);
        out.write(true, "a/b/", (byte[]) null, null);
        out.write(false, "/e.data", new byte[0], null);
        out.write(true, "/", (byte[]) null, null);
        out.close();
    }
}
