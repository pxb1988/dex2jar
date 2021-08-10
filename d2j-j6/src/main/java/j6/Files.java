package j6;

import java.io.File;
import pxb.java.nio.file.spi.FileSystemProvider;

public class Files {
    public static Object toPath(File file) {
        return new FileSystemProvider.DefPath(file);
    }
}
