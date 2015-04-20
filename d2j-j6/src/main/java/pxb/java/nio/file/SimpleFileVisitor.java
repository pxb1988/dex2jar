package pxb.java.nio.file;

import pxb.java.nio.file.attribute.BasicFileAttributes;

import java.io.IOException;

public class SimpleFileVisitor<T> implements FileVisitor<T> {
    @Override
    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs)
            throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(T file, BasicFileAttributes attrs)
            throws IOException {
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
        if (exc != null)
            throw exc;
        return FileVisitResult.CONTINUE;
    }
}
