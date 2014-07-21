/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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
package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ann.Alignment;
import com.googlecode.d2j.dex.writer.io.DataOut;

import java.io.ByteArrayOutputStream;

@Alignment(1)
public class StringDataItem extends BaseItem implements Comparable<StringDataItem> {
    static public class Buffer extends ByteArrayOutputStream {
        public byte[] getBuf() {
            return buf;
        }
    }

    public static void encode(ByteArrayOutputStream out, String s) {
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            char ch = s.charAt(i);
            if (ch != 0 && ch <= 127) { // U+0000 uses two bytes.
                out.write(ch);
            } else if (ch <= 2047) {
                out.write((0xc0 | (0x1f & (ch >> 6))));
                out.write((0x80 | (0x3f & ch)));
            } else {
                out.write((0xe0 | (0x0f & (ch >> 12))));
                out.write((0x80 | (0x3f & (ch >> 6))));
                out.write((0x80 | (0x3f & ch)));
            }
        }
    }

    public static int lengthOfMutf8(String s) {
        int result = 0;
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
        }
        return result;
    }

    public final String string;

    public StringDataItem(String data) {
        this.string = data;
    }

    @Override
    public int compareTo(StringDataItem o) {
        return string.compareTo(o.string);
    }

    @Override
    public int place(int offset) {
        int length = lengthOfMutf8(string);
        return offset + lengthOfUleb128(string.length()) + length + 1; // 1 for tailing 0
    }

    @Override
    public String toString() {
        return "StringDataItem [string=" + string + "]";
    }

    @Override
    public void write(DataOut out) {
        write(out, new Buffer());
    }

    public void write(DataOut out, Buffer buff) {
        out.uleb128("string_data_length", string.length());
        encode(buff, string);
        buff.write(0);
        out.bytes("mutf8-string", buff.getBuf(), 0, buff.size());
    }
}
