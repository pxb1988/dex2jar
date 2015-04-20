package pxb.java.nio.file;


import pxb.java.nio.file.attribute.FileAttribute;
import pxb.java.nio.file.spi.FileSystemProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Files {
    public static Path write(Path path, byte[] bytes, OpenOption... options)
            throws IOException {
        path._write(bytes);
        return path;
    }

    public static byte[] readAllBytes(Path path) throws IOException {
        return path._readAllBytes();
    }

    public static boolean isDirectory(Path path, LinkOption... options) {
        return path._isDirectory();
    }

    public static boolean exists(Path path, LinkOption... options) {
        return path._exists();
    }

    public static OutputStream newOutputStream(Path path, OpenOption... options)
            throws IOException {
        return path._newOutputStream();
    }

    public static InputStream newInputStream(Path path, OpenOption... options)
            throws IOException {
        return path._newInputStream();
    }

    public static boolean deleteIfExists(Path path) throws IOException {
        return path._deleteIfExists();
    }

    public static Path createDirectories(Path dir, FileAttribute... attrs)
            throws IOException {
        return dir._createDirectories();
    }

    public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor)
            throws IOException {
        start._walkFileTree(visitor);
        return start;
    }

    public static Path createTempFile(String prefix,
                                      String suffix,
                                      FileAttribute<?>... attrs) throws IOException {
        File f = File.createTempFile(prefix, suffix);
        return new FileSystemProvider.DefPath(f, null);
    }

    public static Path copy(Path source, Path target, CopyOption... options) throws IOException {
        InputStream is = source._newInputStream();
        OutputStream os = target._newOutputStream();
        FileSystemProvider.copy(is, os);
        is.close();
        os.close();
        return target;
    }
}
