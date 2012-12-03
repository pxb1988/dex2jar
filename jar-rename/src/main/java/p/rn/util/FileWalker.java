package p.rn.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import p.rn.util.FileOut.OutHandler;

public class FileWalker {

    private static final class FileStreamOpener implements StreamOpener, Closeable {
        private File file;

        private FileInputStream fis;

        public void close() {
            IOUtils.closeQuietly(fis);
        }

        public InputStream get() throws IOException {
            fis = FileUtils.openInputStream(file);
            return fis;
        }

        public void setFile(File file) {
            this.file = file;
            fis = null;
        }
    }

    public interface StreamHandler {
        public void handle(boolean isDir, String name, StreamOpener current, Object nameObject) throws IOException;
    }

    public interface StreamOpener {
        InputStream get() throws IOException;
    }

    public static class OutAdapter implements StreamHandler {
        protected OutHandler outHandler;

        public OutAdapter(OutHandler outHandler) {
            super();
            this.outHandler = outHandler;
        }

        @Override
        public void handle(boolean isDir, String name, StreamOpener current, Object nameObject) throws IOException {
            outHandler.write(isDir, name, current == null ? null : current.get(), nameObject);
        }
    }

    private StreamHandler handler;

    public void setStreamHandler(StreamHandler handler) {
        this.handler = handler;
    }

    public void walk(File dirORzip) throws IOException {
        if (dirORzip.isDirectory()) {
            walkDir("", dirORzip, new FileStreamOpener());
        } else {

            final ZipInputStream zis = new ZipInputStream(FileUtils.openInputStream(dirORzip));
            try {
                StreamOpener opener = new StreamOpener() {
                    public InputStream get() {
                        return zis;
                    }
                };
                for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                    handler.handle(entry.isDirectory(), entry.getName(), entry.isDirectory() ? null : opener, entry);
                    zis.closeEntry();
                }
            } finally {
                IOUtils.closeQuietly(zis);
            }
        }
    }

    private void walkDir(String s, File dir, FileStreamOpener current) throws IOException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                handler.handle(true, s + file.getName() + '/', null, file);
                walkDir(s + file.getName() + '/', file, current);
            } else {
                current.setFile(file);
                try {
                    handler.handle(false, s + file.getName(), current, file);
                } finally {
                    IOUtils.closeQuietly(current);
                }
            }
        }
    }

    public FileWalker withStreamHandler(StreamHandler handler) {
        this.handler = handler;
        return this;
    }
}
