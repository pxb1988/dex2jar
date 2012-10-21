package com.googlecode.dex2jar.reader.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.xmlgraphics.image.codec.util.MemoryCacheSeekableStream;

public class InputStreamDataIn extends DataInputDataIn implements Closeable {
    public static InputStreamDataIn open(InputStream in) {
        return new InputStreamDataIn(in, true);
    }

    public static InputStreamDataIn openApk(File file) {
        InputStream in;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No valid apk", e);
        }
        return openApk(in);
    }

    public static InputStreamDataIn openApk(InputStream in) {
        try {
            ZipInputStream zis = new ZipInputStream(in);
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                if (entry.getName().equals("classes.dex")) {
                    return new InputStreamDataIn(zis, true);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Not valid apk", e);
        }
        throw new RuntimeException("No valid apk");
    }

    public InputStreamDataIn(InputStream stream, boolean isLE) {
        super(new MemoryCacheSeekableStream(stream), isLE);
    }

    @Override
    public void close() throws IOException {
        ((MemoryCacheSeekableStream) in).close();
    }

    @Override
    public int getCurrentPosition() {
        return (int) ((MemoryCacheSeekableStream) in).getFilePointer();
    }

    @Override
    public void move(int absOffset) {
        try {
            ((MemoryCacheSeekableStream) in).seek(absOffset);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
