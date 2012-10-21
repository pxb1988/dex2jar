/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.xmlgraphics.image.codec.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A subclass of <code>SeekableStream</code> that may be used to wrap
 * a regular <code>InputStream</code>.  Seeking backwards is supported
 * by means of an in-memory cache.  For greater efficiency,
 * <code>FileCacheSeekableStream</code> should be used in
 * circumstances that allow the creation of a temporary file.
 *
 * <p> The <code>mark()</code> and <code>reset()</code> methods are
 * supported.
 *
 * <p><b> This class is not a committed part of the JAI API.  It may
 * be removed or changed in future releases of JAI.</b>
 */
public final class MemoryCacheSeekableStream extends SeekableStream {

    /** The source input stream. */
    private InputStream src;

    /** Position of first unread byte. */
    private long pointer = 0;

    /** Log_2 of the sector size. */
    private static final int SECTOR_SHIFT = 9;

    /** The sector size. */
    private static final int SECTOR_SIZE = 1 << SECTOR_SHIFT;

    /** A mask to determine the offset within a sector. */
    private static final int SECTOR_MASK = SECTOR_SIZE - 1;

    /** A Vector of source sectors. */
    private List data = new ArrayList();

    /** Number of sectors stored. */
    int sectors = 0;

    /** Number of bytes read. */
    int length = 0;

    /** True if we've previously reached the end of the source stream */
    boolean foundEOS = false;

    /**
     * Constructs a <code>MemoryCacheSeekableStream</code> that takes
     * its source data from a regular <code>InputStream</code>.
     * Seeking backwards is supported by means of an in-memory cache.
     */
    public MemoryCacheSeekableStream(InputStream src) {
        this.src = src;
    }

    /**
     * Ensures that at least <code>pos</code> bytes are cached,
     * or the end of the source is reached.  The return value
     * is equal to the smaller of <code>pos</code> and the
     * length of the source stream.
     */
    private long readUntil(long pos) throws IOException {
        // We've already got enough data cached
        if (pos < length) {
            return pos;
        }
        // pos >= length but length isn't getting any bigger, so return it
        if (foundEOS) {
            return length;
        }

        int sector = (int)(pos >> SECTOR_SHIFT);

        // First unread sector
        int startSector = length >> SECTOR_SHIFT;

        // Read sectors until the desired sector
        for (int i = startSector; i <= sector; i++) {
            byte[] buf = new byte[SECTOR_SIZE];
            data.add(buf);

            // Read up to SECTOR_SIZE bytes
            int len = SECTOR_SIZE;
            int off = 0;
            while (len > 0) {
                int nbytes = src.read(buf, off, len);
                // Found the end-of-stream
                if (nbytes == -1) {
                    foundEOS = true;
                    return length;
                }
                off += nbytes;
                len -= nbytes;

                // Record new data length
                length += nbytes;
            }
        }

        return length;
    }

    /**
     * Returns <code>true</code> since all
     * <code>MemoryCacheSeekableStream</code> instances support seeking
     * backwards.
     */
    public boolean canSeekBackwards() {
        return true;
    }

    /**
     * Returns the current offset in this file.
     *
     * @return     the offset from the beginning of the file, in bytes,
     *             at which the next read occurs.
     */
    public long getFilePointer() {
        return pointer;
    }

    /**
     * Sets the file-pointer offset, measured from the beginning of this
     * file, at which the next read occurs.
     *
     * @param      pos   the offset position, measured in bytes from the
     *                   beginning of the file, at which to set the file
     *                   pointer.
     * @exception  IOException  if <code>pos</code> is less than
     *                          <code>0</code> or if an I/O error occurs.
     */
    public void seek(long pos) throws IOException {
        if (pos < 0) {
            throw new IOException("MemoryCacheSeekableStream0");
        }
        pointer = pos;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     */
    public int read() throws IOException {
        long next = pointer + 1;
        long pos = readUntil(next);
        if (pos >= next) {
            byte[] buf =
                (byte[])data.get((int)(pointer >> SECTOR_SHIFT));
            return buf[(int)(pointer++ & SECTOR_MASK)] & 0xff;
        } else {
            return -1;
        }
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read, possibly
     * zero. The number of bytes actually read is returned as an integer.
     *
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * <p> If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     *
     * <p> If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is
     * thrown.
     *
     * <p> If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     *
     * <p> The first byte read is stored into element <code>b[off]</code>, the
     * next one into <code>b[off+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     *
     * <p> In every case, elements <code>b[0]</code> through
     * <code>b[off]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.
     *
     * <p> If the first byte cannot be read for any reason other than end of
     * file, then an <code>IOException</code> is thrown. In particular, an
     * <code>IOException</code> is thrown if the input stream has been closed.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     */
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if ((off < 0) || (len < 0) || (off + len > b.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }

        long pos = readUntil(pointer + len);
        // End-of-stream
        if (pos <= pointer) {
            return -1;
        }

        byte[] buf = (byte[])data.get((int)(pointer >> SECTOR_SHIFT));
        int nbytes = Math.min(len, SECTOR_SIZE - (int)(pointer & SECTOR_MASK));
        System.arraycopy(buf, (int)(pointer & SECTOR_MASK),
                         b, off, nbytes);
        pointer += nbytes;
        return nbytes;
    }
}
