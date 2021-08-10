package pxb.java.nio.file;

import java.io.IOException;
import pxb.java.nio.file.attribute.BasicFileAttributes;

public interface FileVisitor<T> {

    FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs);

    FileVisitResult visitFile(T file, BasicFileAttributes attrs);

    FileVisitResult visitFileFailed(T file, IOException exc)
            throws IOException;

    FileVisitResult postVisitDirectory(T dir, IOException exc)
            throws IOException;
}
