package p.rn.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class FileOut {

    public interface OutHandler extends Closeable {
        public void write(boolean isDir, String name, InputStream is, Object nameObject) throws IOException;

        public void write(boolean isDir, String name, byte[] data, Object nameObject) throws IOException;

        public OutputStream openOutput(String name, Object nameObject) throws IOException;

    }

    public static OutHandler create(File dirORzip) throws IOException {
        return create(dirORzip, dirORzip.exists() ? (dirORzip.isFile()) : true);
    }

    public static OutHandler create(File dirORzip, boolean isZip) throws IOException {
        if (isZip) {
            return new ZipOutHandler(FileUtils.openOutputStream(dirORzip));
        } else {
            return new FileOutHandler(dirORzip);
        }
    }

    private static class FileOutHandler implements OutHandler {

        public void close() throws IOException {

        }

        public FileOutHandler(File dir) {
            super();
            this.dir = dir;
        }

        File dir;

        public void write(boolean isDir, String name, InputStream is, Object nameObject) throws IOException {
            if (!isDir) {
                FileOutputStream fos = null;
                try {
                    fos = FileUtils.openOutputStream(new File(dir, name));
                    IOUtils.copy(is, fos);
                } finally {
                    IOUtils.closeQuietly(fos);
                }
            }
        }

        public void write(boolean isDir, String name, byte[] data, Object nameObject) throws IOException {
            if (!isDir) {
                FileUtils.writeByteArrayToFile(new File(dir, name), data);
            }
        }

        @Override
        public OutputStream openOutput(String name, Object nameObject) throws IOException {
            return FileUtils.openOutputStream(new File(dir, name));
        }
    }

    private static class ZipOutHandler implements OutHandler {
        private Set<String> dirs = new HashSet<String>();

        private ZipOutputStream zos;

        public ZipOutHandler(OutputStream os) {
            zos = new ZipOutputStream(os);
        }

        private void check(String dir) throws IOException {
            if (dirs.contains(dir)) {
                return;
            }
            dirs.add(dir);
            int i = dir.lastIndexOf('/');
            if (i > 0) {
                check(dir.substring(0, i));
            }
            zos.putNextEntry(new ZipEntry(dir));
            zos.closeEntry();
        }

        public void close() {
            IOUtils.closeQuietly(zos);
        }

        public void write(boolean isDir, String name, byte[] data, Object nameObject) throws IOException {
            zos.putNextEntry(buildEntry(name, nameObject));
            if (!isDir) {
                zos.write(data);
            }
            zos.closeEntry();
        }

        public void write(boolean isDir, String name, InputStream is, Object nameObject) throws IOException {
            zos.putNextEntry(buildEntry(name, nameObject));
            if (!isDir) {
                IOUtils.copy(is, zos);
            }
            zos.closeEntry();
        }

        private ZipEntry buildEntry(String name, Object nameObject) throws IOException {
            int i = name.lastIndexOf('/');
            if (i > 0) {
                check(name.substring(0, i));
            }
            if (nameObject instanceof ZipEntry) {
                ZipEntry ze = (ZipEntry) nameObject;
                if (name.equals(ze.getName())) {
                    ZipEntry nZe = new ZipEntry(name);
                    nZe.setComment(ze.getComment());
                    nZe.setTime(ze.getTime());
                    return nZe;
                }
            }
            return new ZipEntry(name);
        }

        @Override
        public OutputStream openOutput(String name, Object nameObject) throws IOException {
            zos.putNextEntry(buildEntry(name, nameObject));
            return new FilterOutputStream(zos) {

                @Override
                public void close() throws IOException {
                    this.flush();
                    zos.closeEntry();
                }

            };
        }
    }
}
