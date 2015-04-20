package j6;

import pxb.java.nio.file.spi.FileSystemProvider;

import java.io.File;

public class Files {
    public static Object toPath(File file) throws Throwable {
        return new FileSystemProvider.DefPath(file);
    }
}
