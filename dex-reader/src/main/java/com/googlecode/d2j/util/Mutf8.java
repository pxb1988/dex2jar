/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.d2j.util;

import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;

/**
 * Modified UTF-8 as described in the dex file format spec.
 * 
 * <p>
 * Derived from libcore's MUTF-8 encoder at java.nio.charset.ModifiedUtf8.
 */
public final class Mutf8 {
    private Mutf8() {
    }

    /**
     * Decodes bytes from {@code in} into {@code sb} until a delimiter 0x00 is encountered. Returns a new string
     * containing the decoded characters.
     */
    public static String decode(ByteBuffer in, StringBuilder sb) throws UTFDataFormatException {
        while (true) {
            char a = (char) (in.get() & 0xff);
            if (a == 0) {
                return sb.toString();
            }

            if (a < '\u0080') {
                sb.append(a);
            } else if ((a & 0xe0) == 0xc0) {
                int b = in.get() & 0xff;
                if ((b & 0xC0) != 0x80) {
                    throw new UTFDataFormatException("bad second byte");
                }
                sb.append((char) (((a & 0x1F) << 6) | (b & 0x3F)));
            } else if ((a & 0xf0) == 0xe0) {
                int b = in.get() & 0xff;
                int c = in.get() & 0xff;
                if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80)) {
                    throw new UTFDataFormatException("bad second or third byte");
                }
                sb.append((char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F)));
            } else {
                throw new UTFDataFormatException("bad byte");
            }
        }
    }

    /**
     * Returns the number of bytes the modified UTF8 representation of 's' would take.
     */
    private static long countBytes(String s, boolean shortLength) throws UTFDataFormatException {
        long result = 0;
        final int length = s.length();
        for (int i = 0; i < length; ++i) {
            char ch = s.charAt(i);
            if (ch != 0 && ch <= 127) { // U+0000 uses two bytes.
                ++result;
            } else if (ch <= 2047) {
                result += 2;
            } else {
                result += 3;
            }
            if (shortLength && result > 65535) {
                throw new UTFDataFormatException("String more than 65535 UTF bytes long");
            }
        }
        return result;
    }

    /**
     * Encodes the modified UTF-8 bytes corresponding to {@code s} into {@code dst}, starting at {@code offset}.
     */
    public static void encode(byte[] dst, int offset, String s) {
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            char ch = s.charAt(i);
            if (ch != 0 && ch <= 127) { // U+0000 uses two bytes.
                dst[offset++] = (byte) ch;
            } else if (ch <= 2047) {
                dst[offset++] = (byte) (0xc0 | (0x1f & (ch >> 6)));
                dst[offset++] = (byte) (0x80 | (0x3f & ch));
            } else {
                dst[offset++] = (byte) (0xe0 | (0x0f & (ch >> 12)));
                dst[offset++] = (byte) (0x80 | (0x3f & (ch >> 6)));
                dst[offset++] = (byte) (0x80 | (0x3f & ch));
            }
        }
    }

    /**
     * Returns an array containing the <i>modified UTF-8</i> form of {@code s}.
     */
    public static byte[] encode(String s) throws UTFDataFormatException {
        int utfCount = (int) countBytes(s, true);
        byte[] result = new byte[utfCount];
        encode(result, 0, s);
        return result;
    }
}
