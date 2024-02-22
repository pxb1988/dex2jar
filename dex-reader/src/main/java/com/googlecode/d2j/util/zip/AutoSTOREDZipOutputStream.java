package com.googlecode.d2j.util.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Auto calc size/crc for STORED
 */
public class AutoSTOREDZipOutputStream extends ZipOutputStream {

    public AutoSTOREDZipOutputStream(OutputStream out) {
        super(out);
    }

    private final CRC32 crc = new CRC32();

    private ZipEntry delayedEntry;

    private AccessBufByteArrayOutputStream delayedOutputStream;

    @Override
    public void putNextEntry(ZipEntry e) throws IOException {
        if (e.getMethod() != ZipEntry.STORED) {
            super.putNextEntry(e);
        } else {
            delayedEntry = e;
            if (delayedOutputStream == null) {
                delayedOutputStream = new AccessBufByteArrayOutputStream();
            }
        }
    }

    @Override
    public void closeEntry() throws IOException {
        ZipEntry delayedEntry = this.delayedEntry;
        if (delayedEntry != null) {
            AccessBufByteArrayOutputStream delayedOutputStream = this.delayedOutputStream;
            byte[] buf = delayedOutputStream.getBuf();
            int size = delayedOutputStream.size();
            delayedEntry.setSize(size);
            delayedEntry.setCompressedSize(size);
            crc.reset();
            crc.update(buf, 0, size);
            delayedEntry.setCrc(crc.getValue());
            super.putNextEntry(delayedEntry);
            super.write(buf, 0, size);
            this.delayedEntry = null;
            delayedOutputStream.reset();
        }
        super.closeEntry();
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (delayedEntry != null) {
            delayedOutputStream.write(b, off, len);
        } else {
            super.write(b, off, len);
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (delayedEntry != null) {
            delayedOutputStream.write(b);
        } else {
            super.write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (delayedEntry != null) {
            delayedOutputStream.write(b);
        } else {
            super.write(b);
        }
    }

    @Override
    public void close() throws IOException {
        delayedOutputStream = null;
        super.close();
    }

}
