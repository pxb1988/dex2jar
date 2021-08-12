package j6;

import java.io.File;
import pxb.java.nio.file.spi.FileSystemProvider;

public final class Files {

    private Files() {
        throw new UnsupportedOperationException();
    }

    public static Object toPath(File file) {
        return new FileSystemProvider.DefPath(file);
    }

}
