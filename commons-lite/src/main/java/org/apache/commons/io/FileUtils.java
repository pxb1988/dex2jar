package org.apache.commons.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * part of FileUtils from commons-io
 * 
 */
public class FileUtils {

    public static final String LINE_SEPARATOR;
    static {
        // avoid security issues
        StringWriter buf = new StringWriter(4);
        PrintWriter out = new PrintWriter(buf);
        out.println();
        LINE_SEPARATOR = buf.toString();
        out.close();
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return IOUtils.toByteArray(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static FileOutputStream openOutputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && parent.exists() == false) {
                if (parent.mkdirs() == false) {
                    throw new IOException("File '" + file + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file);
    }

    public static void writeLines(File file, String encoding, Collection<String> lines) throws IOException {
        if (lines == null) {
            return;
        }
        OutputStream output = null;
        try {
            output = openOutputStream(file);
            for (Object line : lines) {
                if (line != null) {
                    output.write(line.toString().getBytes(encoding));
                }
                output.write(LINE_SEPARATOR.getBytes(encoding));
            }
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<File> listFiles(File dir, String[] exts, boolean r) {
        if (exts.length == 0) {
            return Collections.EMPTY_LIST;
        }
        Set<String> set = new HashSet<String>(exts.length);
        for (String ext : exts) {
            set.add("." + ext);
        }
        List<File> list = new ArrayList<File>();
        doFind(dir, set, list, r);
        return list;
    }

    private static void doFind(File dir, Set<String> exts, List<File> list, boolean r) {
        File[] fs = dir.listFiles();
        if (fs == null) {
            return;
        }
        for (File f : fs) {
            if (f.isFile()) {
                String name = f.getName();
                for (String ext : exts) {
                    if (name.endsWith(ext)) {
                        list.add(f);
                        break;
                    }
                }
            } else if (r) {
                doFind(f, exts, list, r);
            }
        }
    }

    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file);
            out.write(data);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public static void writeStringToFile(File file, String str, String encoding) throws IOException {
        writeByteArrayToFile(file, str.getBytes(encoding));
    }

    public static String readFileToString(File file, String charset) throws IOException {
        return new String(readFileToByteArray(file), charset);
    }

    public static List<String> readLines(File file, String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(openInputStream(file), encoding));
        try {
            List<String> list = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null) {
                list.add(line);
                line = reader.readLine();
            }
            return list;
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
