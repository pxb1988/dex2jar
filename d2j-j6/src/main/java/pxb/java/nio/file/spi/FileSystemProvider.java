package pxb.java.nio.file.spi;

import pxb.java.nio.file.FileSystem;
import pxb.java.nio.file.FileVisitor;
import pxb.java.nio.file.Path;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class FileSystemProvider {
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] xml = new byte[10 * 1024];
        for (int c = is.read(xml); c > 0; c = is.read(xml)) {
            os.write(xml, 0, c);
        }
    }

    public static byte[] readFile(File in) throws IOException {
        InputStream is = new FileInputStream(in);
        byte[] xml = new byte[is.available()];
        is.read(xml);
        is.close();
        return xml;
    }

    public static byte[] readIs(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(is, os);
        return os.toByteArray();
    }

    public static void writeFile(byte[] data, File out) throws IOException {
        FileOutputStream fos = new FileOutputStream(out);
        fos.write(data);
        fos.close();
    }

    public static FileSystemProvider ZIP = new ZipFSP();
    public static FileSystemProvider DEF = new DirFSP();

    public static List<FileSystemProvider> installedProviders() {
        return Arrays.asList(ZIP, DEF);
    }

    public abstract String getScheme();

    public abstract FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException;


    static class CreatZipFS extends FileSystem {
        ZipOutputStream zos;

        class CreateZipPath implements Path {
            String path;
            String displayName;

            public CreateZipPath(String s, String displayName) {
                this.path = s;
                this.displayName = displayName;
            }

            @Override
            public Path resolve(String other) {
                if (path.endsWith("/")) {
                    return new CreateZipPath(path + other, null);
                }
                return new CreateZipPath(path + "/" + other, null);
            }

            @Override
            public Path getFileName() {
                int t = path.length() - 1;
                if (path.endsWith("/")) {
                    t--;
                }
                int i = path.lastIndexOf('/', t);
                if (i > 0) {
                    return new CreateZipPath(path, path.substring(i + 1, t + 1));
                } else {
                    return this;
                }
            }

            @Override
            public String toString() {
                return displayName != null ? displayName : path;
            }

            @Override
            public Path getParent() {
                int i = path.lastIndexOf('/', path.length() - 2);
                if (i > 0) {
                    return new CreateZipPath(path.substring(0, i), null);
                }
                return null;
            }

            @Override
            public File toFile() {
                return null;
            }

            @Override
            public byte[] _readAllBytes() {
                throw new RuntimeException();
            }

            @Override
            public OutputStream _newOutputStream() throws IOException {
                ZipEntry e = new ZipEntry(path.substring(1));
                zos.putNextEntry(e);
                return new FilterOutputStream(zos) {
                    @Override
                    public void close() throws IOException {
                        zos.closeEntry();
                    }
                };
            }

            @Override
            public boolean _isDirectory() {
                return path.endsWith("/");
            }

            @Override
            public Path _createDirectories() throws IOException {
                createDir0(path);
                return this;
            }

            @Override
            public boolean _deleteIfExists() {
                throw new RuntimeException();
            }

            @Override
            public boolean _exists() {
                return exists(path);
            }

            @Override
            public void _write(byte[] b) throws IOException {
                OutputStream os = _newOutputStream();
                os.write(b);
                os.close();
            }

            @Override
            public void _walkFileTree(FileVisitor<? super Path> visitor) {
                throw new RuntimeException();
            }

            @Override
            public Path relativize(Path other) {
                CreateZipPath p0 = (CreateZipPath) other;
                String display = path.substring(p0.path.length());
                return new CreateZipPath(p0.path, display);
            }

            @Override
            public InputStream _newInputStream() throws IOException {
                throw new RuntimeException();
            }
        }

        private boolean createDir0(String path) throws IOException {
            int x = path.lastIndexOf('/', path.length() - 2);
            if (x > 0) {
                String n = path.substring(0, x + 1);
                createDir0(n);
            }
            if (!path.contains(path)) {
                files.add(path);
                ZipEntry zipEntry = new ZipEntry(path);
                zos.putNextEntry(zipEntry);
                zos.closeEntry();
                return true;
            }
            return false;
        }

        private Set<String> files = new HashSet<>();

        private boolean exists(String path) {
            return files.contains(path);
        }

        public CreatZipFS(ZipOutputStream zipFile) {
            this.zos = zipFile;
        }

        @Override
        public void close() throws IOException {
            zos.close();
        }

        @Override
        public Path getPath(String first, String... more) {
            return new CreateZipPath(first, null);
        }
    }

    static class ReadZipPath implements Path {
        ZipFile zipFile;
        String path;
        String displayName;

        public ReadZipPath(ZipFile zipFile, String path) {
            this(zipFile, path, null);
        }

        public ReadZipPath(ZipFile zipFile, String path, String substring) {
            this.zipFile = zipFile;
            this.path = path;
            this.displayName = substring;
        }

        @Override
        public Path resolve(String other) {
            if (path.endsWith("/")) {
                return new ReadZipPath(zipFile, path + other);
            } else {
                return new ReadZipPath(zipFile, path + "/" + other);
            }
        }

        @Override
        public Path getFileName() {
            int t = path.length() - 1;
            if (path.endsWith("/")) {
                t--;
            }
            int i = path.lastIndexOf('/', t);
            if (i > 0) {
                return new ReadZipPath(zipFile, path, path.substring(i + 1, t + 1));
            } else {
                return this;
            }
        }

        @Override
        public Path getParent() {
            int t = path.length() - 1;
            if (path.endsWith("/")) {
                t--;
            }
            int i = path.lastIndexOf('/', t);
            return i > 0 ? new ReadZipPath(zipFile, path.substring(0, i + 1), null) : null;
        }

        @Override
        public File toFile() {
            return null;
        }

        @Override
        public String toString() {
            return displayName != null ? displayName : path;
        }

        @Override
        public byte[] _readAllBytes() throws IOException {
            ZipEntry e = zipFile.getEntry(path);
            return e != null ? readIs(zipFile.getInputStream(e)) : null;
        }

        @Override
        public OutputStream _newOutputStream() throws FileNotFoundException {
            throw new RuntimeException();
        }

        @Override
        public boolean _isDirectory() {
            ZipEntry e = zipFile.getEntry(path);
            return e != null && e.isDirectory();
        }

        @Override
        public Path _createDirectories() {
            throw new RuntimeException();
        }

        @Override
        public boolean _deleteIfExists() {
            throw new RuntimeException();
        }

        @Override
        public boolean _exists() {
            ZipEntry e = zipFile.getEntry(path);
            return e != null;
        }

        @Override
        public void _write(byte[] b) throws IOException {
            throw new RuntimeException();
        }

        @Override
        public void _walkFileTree(FileVisitor<? super Path> visitor) throws IOException {
            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
                ZipEntry zipEntry = e.nextElement();
                ReadZipPath readZipPath = new ReadZipPath(zipFile, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    visitor.preVisitDirectory(readZipPath, null);
                    visitor.postVisitDirectory(readZipPath, null);
                } else {
                    visitor.visitFile(readZipPath, null);
                }
            }
        }

        @Override
        public Path relativize(Path other) {
            ReadZipPath p0 = (ReadZipPath) other;
            String display = path.substring(p0.path.length());
            return new ReadZipPath(zipFile, p0.path, display);
        }

        @Override
        public InputStream _newInputStream() throws IOException {
            ZipEntry e = zipFile.getEntry(path);
            return e != null ? zipFile.getInputStream(e) : null;
        }
    }

    static class ReadZipFS extends FileSystem {
        ZipFile zipFile;

        public ReadZipFS(ZipFile zipFile) {
            this.zipFile = zipFile;
        }

        @Override
        public void close() throws IOException {
            zipFile.close();
        }

        @Override
        public Path getPath(String first, String... more) {
            return new ReadZipPath(zipFile, first);
        }
    }

    static class ZipFSP extends FileSystemProvider {

        @Override
        public String getScheme() {
            return "zip";
        }

        @Override
        public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException {
            if (env != null && "true".equals(env.get("create"))) {
                return new CreatZipFS(new ZipOutputStream(path._newOutputStream()));
            } else {
                return new ReadZipFS(new ZipFile(((DefPath) path).file));
            }
        }
    }

    public static class DefPath implements Path {
        File file;
        String displayName;

        public DefPath(File file) {
            this.file = file;
        }

        public DefPath(File file, String name) {
            this.file = file;
            this.displayName = name;
        }

        @Override
        public String toString() {
            return displayName != null ? displayName : file.toString();
        }

        @Override
        public Path resolve(String other) {
            return new DefPath(new File(file, other));
        }

        @Override
        public Path getFileName() {
            return new DefPath(file, file.getName());
        }

        @Override
        public Path getParent() {
            return new DefPath(file.getParentFile());
        }

        @Override
        public File toFile() {
            return file;
        }

        @Override
        public byte[] _readAllBytes() throws IOException {
            return readFile(file);
        }

        @Override
        public OutputStream _newOutputStream() throws FileNotFoundException {
            return new BufferedOutputStream(new FileOutputStream(file));
        }

        @Override
        public boolean _isDirectory() {
            return file.isDirectory();
        }

        @Override
        public Path _createDirectories() {
            file.mkdirs();
            return this;
        }

        @Override
        public boolean _deleteIfExists() {
            return file.exists() && file.delete();
        }

        @Override
        public boolean _exists() {
            return file.exists();
        }

        @Override
        public void _write(byte[] b) throws IOException {
            OutputStream os = _newOutputStream();
            os.write(b);
            os.close();
        }

        @Override
        public void _walkFileTree(FileVisitor<? super Path> visitor) throws IOException {
            walk0(this, visitor);
        }

        public static void walk0(DefPath dir, FileVisitor<? super Path> visitor) throws IOException {
            visitor.preVisitDirectory(dir, null);
            File[] fs = dir.file.listFiles();
            if (fs != null) {
                for (File f : fs) {
                    if (f.isDirectory()) {
                        walk0(new DefPath(f, null), visitor);
                    } else {
                        visitor.visitFile(new DefPath(f, null), null);
                    }
                }
            }
            visitor.postVisitDirectory(dir, null);
        }

        @Override
        public Path relativize(Path other) {
            DefPath p0 = (DefPath) other;
            String display = file.getAbsolutePath().substring(p0.file.getAbsolutePath().length());
            return new DefPath(p0.file, display);
        }

        @Override
        public InputStream _newInputStream() throws FileNotFoundException {
            return new BufferedInputStream(new FileInputStream(file));
        }
    }

    static class DirFSP extends FileSystemProvider {

        @Override
        public String getScheme() {
            return "default";
        }

        @Override
        public FileSystem newFileSystem(Path path, Map<String, ?> env) {
            throw new RuntimeException();
        }
    }
}
