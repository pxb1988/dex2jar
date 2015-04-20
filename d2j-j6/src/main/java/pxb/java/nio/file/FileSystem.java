package pxb.java.nio.file;

import java.io.Closeable;

public abstract class FileSystem implements Closeable {
    public abstract Path getPath(String first, String... more);
}
