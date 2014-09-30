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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * An entry within a zip file. An entry has attributes such as its name (which is actually a path) and the uncompressed
 * size of the corresponding data. An entry does not contain the data itself, but can be used as a key with
 * {@link java.util.zip.ZipFile#getInputStream}. The class documentation for {@link java.util.zip.ZipInputStream} and
 * {@link java.util.zip.ZipOutputStream} shows how {@code android.ZipEntry} is used in conjunction with those two
 * classes.
 */
public final class ZipEntry implements ZipConstants, Cloneable {
    String name;
    String comment;

    long crc = -1; // Needs to be a long to distinguish -1 ("not set") from the 0xffffffff CRC32.

    long compressedSize = -1;
    long size = -1;

    int compressionMethod = -1;
    int time = -1;
    int modDate = -1;

    byte[] extra;

    int nameLength = -1;
    long localHeaderRelOffset = -1;

    /**
     * Zip entry state: Deflated.
     */
    public static final int DEFLATED = 8;

    /**
     * Zip entry state: Stored.
     */
    public static final int STORED = 0;

    /**
     * Returns the comment for this {@code android.ZipEntry}, or {@code null} if there is no comment. If we're reading a
     * zip file using {@code ZipInputStream}, the comment is not available.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the compressed size of this {@code android.ZipEntry}.
     * 
     * @return the compressed size, or -1 if the compressed size has not been set.
     */
    public long getCompressedSize() {
        return compressedSize;
    }

    /**
     * Gets the checksum for this {@code android.ZipEntry}.
     * 
     * @return the checksum, or -1 if the checksum has not been set.
     */
    public long getCrc() {
        return crc;
    }

    /**
     * Gets the extra information for this {@code android.ZipEntry}.
     * 
     * @return a byte array containing the extra information, or {@code null} if there is none.
     */
    public byte[] getExtra() {
        return extra;
    }

    /**
     * Gets the compression method for this {@code android.ZipEntry}.
     * 
     * @return the compression method, either {@code DEFLATED}, {@code STORED} or -1 if the compression method has not
     *         been set.
     */
    public int getMethod() {
        return compressionMethod;
    }

    /**
     * Gets the name of this {@code android.ZipEntry}.
     * 
     * @return the entry name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the uncompressed size of this {@code android.ZipEntry}.
     * 
     * @return the uncompressed size, or {@code -1} if the size has not been set.
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the last modification time of this {@code android.ZipEntry}.
     * 
     * @return the last modification time as the number of milliseconds since Jan. 1, 1970.
     */
    public long getTime() {
        if (time != -1) {
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(1980 + ((modDate >> 9) & 0x7f), ((modDate >> 5) & 0xf) - 1, modDate & 0x1f, (time >> 11) & 0x1f,
                    (time >> 5) & 0x3f, (time & 0x1f) << 1);
            return cal.getTime().getTime();
        }
        return -1;
    }

    /**
     * Determine whether or not this {@code android.ZipEntry} is a directory.
     * 
     * @return {@code true} when this {@code android.ZipEntry} is a directory, {@code false} otherwise.
     */
    public boolean isDirectory() {
        return name.charAt(name.length() - 1) == '/';
    }

    /**
     * Returns the string representation of this {@code android.ZipEntry}.
     * 
     * @return the string representation of this {@code android.ZipEntry}.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a deep copy of this zip entry.
     */
    @Override
    public Object clone() {
        try {
            ZipEntry result = (ZipEntry) super.clone();
            result.extra = extra != null ? extra.clone() : null;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    ZipEntry(ByteBuffer it0, boolean skipCommentsAndExtra) throws IOException {
        ByteBuffer it = (ByteBuffer) it0.slice().order(ByteOrder.LITTLE_ENDIAN).limit(CENHDR);
        ZipFile.skip(it0, CENHDR);
        int sig = it.getInt();
        if (sig != CENSIG) {
            ZipFile.throwZipException("Central Directory Entry", sig);
        }

        it.position(8);
        int gpbf = it.getShort() & 0xffff;

        compressionMethod = it.getShort() & 0xffff;
        time = it.getShort() & 0xffff;
        modDate = it.getShort() & 0xffff;

        // These are 32-bit values in the file, but 64-bit fields in this object.
        crc = ((long) it.getInt()) & 0xffffffffL;
        compressedSize = ((long) it.getInt()) & 0xffffffffL;
        size = ((long) it.getInt()) & 0xffffffffL;

        nameLength = it.getShort() & 0xffff;
        int extraLength = it.getShort() & 0xffff;
        int commentByteCount = it.getShort() & 0xffff;

        // This is a 32-bit value in the file, but a 64-bit field in this object.
        it.position(42);
        localHeaderRelOffset = ((long) it.getInt()) & 0xffffffffL;

        byte[] nameBytes = new byte[nameLength];
        it0.get(nameBytes);
        // if (containsNulByte(nameBytes)) {
        // throw new ZipException("Filename contains NUL byte: " + Arrays.toString(nameBytes));
        // }
        name = new String(nameBytes, 0, nameBytes.length, StandardCharsets.UTF_8);

        if (extraLength > 0) {
            if (skipCommentsAndExtra) {
                ZipFile.skip(it0, extraLength);
            } else {
                extra = new byte[extraLength];
                it.get(extra);
            }
        }

        // The RI has always assumed UTF-8. (If GPBF_UTF8_FLAG isn't set, the encoding is
        // actually IBM-437.)
        if (commentByteCount > 0) {
            if (skipCommentsAndExtra) {
                ZipFile.skip(it0, commentByteCount);
            } else {
                byte[] commentBytes = new byte[commentByteCount];
                it0.get(commentBytes);
                comment = new String(commentBytes, 0, commentBytes.length, StandardCharsets.UTF_8);
            }
        }
    }

    private static boolean containsNulByte(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0) {
                return true;
            }
        }
        return false;
    }
}
