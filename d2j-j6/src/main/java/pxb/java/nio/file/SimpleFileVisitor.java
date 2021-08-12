package pxb.java.nio.file;

import java.io.IOException;
import pxb.java.nio.file.attribute.BasicFileAttributes;

public class SimpleFileVisitor<T> implements FileVisitor<T> {

    @Override
    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(T file, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(T file, IOException exc)
            throws IOException {
        throw exc;
    }

    @Override
    public FileVisitResult postVisitDirectory(T dir, IOException exc)
            throws IOException {
        if (exc != null) {
            throw exc;
        }
        return FileVisitResult.CONTINUE;
    }

}
