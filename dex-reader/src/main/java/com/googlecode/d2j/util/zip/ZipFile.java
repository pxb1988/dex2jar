/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.d2j.util.zip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * This is code is get from Android 4.4.2 intent to read as more zip as possible
 * 
 * Ignore GPBF_ENCRYPTED_FLAG
 * 
 * Allow duplicate ZipEntry
 * 
 * Allow Nul byte in ZipEntry name
 * 
 */
public class ZipFile implements AutoCloseable, ZipConstants {
    /**
     * General Purpose Bit Flags, Bit 0. If set, indicates that the file is encrypted.
     */
    static final int GPBF_ENCRYPTED_FLAG = 1 << 0;

    /**
     * General Purpose Bit Flags, Bit 3. If this bit is set, the fields crc-32, compressed size and uncompressed size
     * are set to zero in the local header. The correct values are put in the data descriptor immediately following the
     * compressed data. (Note: PKZIP version 2.04g for DOS only recognizes this bit for method 8 compression, newer
     * versions of PKZIP recognize this bit for any compression method.)
     */
    static final int GPBF_DATA_DESCRIPTOR_FLAG = 1 << 3;

    /**
     * General Purpose Bit Flags, Bit 11. Language encoding flag (EFS). If this bit is set, the filename and comment
     * fields for this file must be encoded using UTF-8.
     */
    static final int GPBF_UTF8_FLAG = 1 << 11;

    /**
     * Supported General Purpose Bit Flags Mask. Bit mask of bits not supported. Note: The only bit that we will enforce
     * at this time is the encrypted bit. Although other bits are not supported, we must not enforce them as this could
     * break some legitimate use cases (See http://b/8617715).
     */
    static final int GPBF_UNSUPPORTED_MASK = GPBF_ENCRYPTED_FLAG;

    private List<ZipEntry> entries;

    private String comment;
    final ByteBuffer raf;

    public ZipFile(ByteBuffer in) throws IOException {
        raf = in.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        readCentralDir();
    }

    public ZipFile(byte[] data) throws IOException {
        this(ByteBuffer.wrap(data));
    }

    public List<? extends ZipEntry> entries() {
        return entries;
    }

    /**
     * Returns this file's comment, or null if it doesn't have one. See {@link java.util.zip.ZipOutputStream#setComment}
     * .
     * 
     * @throws IllegalStateException
     *             if this zip file has been closed.
     * @since 1.7
     */
    public String getComment() {
        return comment;
    }

    public ZipEntry findFirstEntry(String entryName) {
        if (entryName == null) {
            throw new NullPointerException("entryName == null");
        }

        ZipEntry ze = findFirstEntry0(entryName);
        if (ze == null) {
            ze = findFirstEntry0(entryName + "/");
        }
        return ze;
    }

    private ZipEntry findFirstEntry0(String entryName) {
        for (ZipEntry e : entries) {
            if (e.getName().equals(entryName)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Returns an input stream on the data of the specified {@code android.ZipEntry}.
     * 
     * @param entry
     *            the android.ZipEntry.
     * @return an input stream of the data contained in the {@code android.ZipEntry}.
     * @throws java.io.IOException
     *             if an {@code IOException} occurs.
     * @throws IllegalStateException
     *             if this zip file has been closed.
     */
    public InputStream getInputStream(ZipEntry entry) throws IOException {
        // We don't know the entry data's start position. All we have is the
        // position of the entry's local header.
        // http://www.pkware.com/documents/casestudies/APPNOTE.TXT
        final ByteBuffer is = (ByteBuffer) raf.duplicate().order(ByteOrder.LITTLE_ENDIAN)
                .position((int) entry.localHeaderRelOffset);

        skip(is, 6); // skip check localMagic
        // final int localMagic = is.getInt();
        // if (localMagic != LOCSIG) {
        // throwZipException("Local File Header", localMagic);
        // }
        // skip(is, 2);

        // At position 6 we find the General Purpose Bit Flag.
        int gpbf = is.getShort() & 0xffff;

        // Offset 26 has the file name length, and offset 28 has the extra field length.
        // These lengths can differ from the ones in the central header.
        skip(is, 18);
        int fileNameLength = is.getShort() & 0xffff;
        int extraFieldLength = is.getShort() & 0xffff;

        // Skip the variable-size file name and extra field data.
        skip(is, fileNameLength + extraFieldLength);

        if (entry.compressionMethod == ZipEntry.STORED) {
            final ByteBuffer buf = (ByteBuffer) is.slice().order(ByteOrder.LITTLE_ENDIAN).limit((int) entry.size);
            return new ByteBufferBackedInputStream(buf);
        } else {
            final ByteBuffer buf = (ByteBuffer) is.slice().order(ByteOrder.LITTLE_ENDIAN)
                    .limit((int) entry.compressedSize);
            int bufSize = Math.max(1024, (int) Math.min(entry.getSize(), 65535L));
            return new ZipInflaterInputStream(new ByteBufferBackedInputStream(buf), new Inflater(true), bufSize, entry);
        }
    }

    static void skip(ByteBuffer is, int i) {
        is.position(is.position() + i);
    }

    /**
     * Returns the number of {@code ZipEntries} in this {@code android.ZipFile}.
     * 
     * @return the number of entries in this file.
     * @throws IllegalStateException
     *             if this zip file has been closed.
     */
    public int size() {
        return entries.size();
    }

    /**
     * Find the central directory and read the contents.
     * 
     * <p>
     * The central directory can be followed by a variable-length comment field, so we have to scan through it
     * backwards. The comment is at most 64K, plus we have 18 bytes for the end-of-central-dir stuff itself, plus
     * apparently sometimes people throw random junk on the end just for the fun of it.
     * 
     * <p>
     * This is all a little wobbly. If the wrong value ends up in the EOCD area, we're hosed. This appears to be the way
     * that everybody handles it though, so we're in good company if this fails.
     */
    private void readCentralDir() throws IOException {
        ByteBuffer raf = this.raf;
        // Scan back, looking for the End Of Central Directory field. If the zip file doesn't
        // have an overall comment (unrelated to any per-entry comments), we'll hit the EOCD
        // on the first try.
        // No need to synchronize raf here -- we only do this when we first open the zip file.
        long scanOffset = raf.limit() - ENDHDR;
        if (scanOffset < 0) {
            throw new ZipException("File too short to be a zip file: " + raf.limit());
        }

        // not check Magic
        // raf.position(0);
        // final int headerMagic = raf.getInt();
        // if (headerMagic != LOCSIG) {
        // throw new ZipException("Not a zip archive");
        // }

        long stopOffset = scanOffset - 65536;
        if (stopOffset < 0) {
            stopOffset = 0;
        }

        while (true) {
            raf.position((int) scanOffset);
            if (raf.getInt() == ENDSIG) {
                break;
            }

            scanOffset--;
            if (scanOffset < stopOffset) {
                throw new ZipException("End Of Central Directory signature not found");
            }
        }

        // Read the End Of Central Directory. ENDHDR includes the signature bytes,
        // which we've already read.

        // Pull out the information we need.
        int diskNumber = raf.getShort() & 0xffff;
        int diskWithCentralDir = raf.getShort() & 0xffff;
        int numEntries = raf.getShort() & 0xffff;
        int totalNumEntries = raf.getShort() & 0xffff;
        skip(raf, 4); // Ignore centralDirSize.
        long centralDirOffset = ((long) raf.getInt()) & 0xffffffffL;
        int commentLength = raf.getShort() & 0xffff;

        if (numEntries != totalNumEntries || diskNumber != 0 || diskWithCentralDir != 0) {
            throw new ZipException("Spanned archives not supported");
        }
        boolean skipCommentsAndExtra = true;

        if (commentLength > 0) {
            if (skipCommentsAndExtra) {
                skip(raf, commentLength);
            } else {
                byte[] commentBytes = new byte[commentLength];
                raf.get(commentBytes);
                comment = new String(commentBytes, 0, commentBytes.length, StandardCharsets.UTF_8);
            }
        }

        // Seek to the first CDE and read all entries.
        // We have to do this now (from the constructor) rather than lazily because the
        // public API doesn't allow us to throw IOException except from the constructor
        // or from getInputStream.
        ByteBuffer buf = (ByteBuffer) raf.duplicate().order(ByteOrder.LITTLE_ENDIAN).position((int) centralDirOffset);
        entries = new ArrayList<>(numEntries);
        for (int i = 0; i < numEntries; ++i) {
            ZipEntry newEntry = new ZipEntry(buf, skipCommentsAndExtra);
            if (newEntry.localHeaderRelOffset >= centralDirOffset) {
                // Ignore the entry
                // throw new ZipException("Local file header offset is after central directory");
            } else {
                entries.add(newEntry);
            }
        }
    }

    static void throwZipException(String msg, int magic) throws ZipException {
        final String hexString = String.format("0x%08x", magic);
        throw new ZipException(msg + " signature not found; was " + hexString);
    }

    @Override
    public void close() throws IOException {

    }

    static class ZipInflaterInputStream extends InflaterInputStream {
        private final ZipEntry entry;
        private long bytesRead = 0;

        public ZipInflaterInputStream(InputStream is, Inflater inf, int bsize, ZipEntry entry) {
            super(is, inf, bsize);
            this.entry = entry;
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            final int i;
            try {
                i = super.read(buffer, byteOffset, byteCount);
            } catch (IOException e) {
                throw new IOException("Error reading data for " + entry.getName() + " near offset " + bytesRead, e);
            }
            if (i == -1) {
                if (entry.size != bytesRead) {
                    throw new IOException("Size mismatch on inflated file: " + bytesRead + " vs " + entry.size);
                }
            } else {
                bytesRead += i;
            }
            return i;
        }

        @Override
        public int available() throws IOException {
            return super.available() == 0 ? 0 : (int) (entry.getSize() - bytesRead);
        }
    }

    private static class ByteBufferBackedInputStream extends InputStream {
        private final ByteBuffer buf;

        public ByteBufferBackedInputStream(ByteBuffer buf) {
            this.buf = buf;
        }

        @Override
        public int read() throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }
            return buf.get() & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }
            len = Math.min(len, buf.remaining());
            buf.get(b, off, len);
            return len;
        }
    }
}
