package pxb.java.nio.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Path {

    Path resolve(String other);

    Path getFileName();

    Path getParent();

    File toFile();

    String toString();

    byte[] _readAllBytes() throws IOException;

    OutputStream _newOutputStream() throws IOException;

    boolean _isDirectory();

    Path _createDirectories() throws IOException;

    boolean _deleteIfExists();

    boolean _exists();

    void _write(byte[] b) throws IOException;

    void _walkFileTree(FileVisitor<? super Path> visitor) throws IOException;

    Path relativize(Path other);

    InputStream _newInputStream() throws IOException;
}
