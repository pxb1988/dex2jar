package pxb.java.nio.file;

import java.io.IOException;
import pxb.java.nio.file.attribute.BasicFileAttributes;

public interface  FileVisitor<T> {

    FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs)
            throws IOException;

    FileVisitResult visitFile(T file, BasicFileAttributes attrs)
            throws IOException;

    FileVisitResult visitFileFailed(T file, IOException exc)
            throws IOException;

    FileVisitResult postVisitDirectory(T dir, IOException exc)
            throws IOException;
}
