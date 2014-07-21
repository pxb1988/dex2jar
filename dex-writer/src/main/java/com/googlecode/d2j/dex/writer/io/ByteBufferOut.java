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
package com.googlecode.d2j.dex.writer.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferOut implements DataOut {
    public final ByteBuffer buffer;

    public ByteBufferOut(ByteBuffer buffer) {
        this.buffer = buffer;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public void begin(String s) {
    }

    @Override
    public void bytes(String s, byte[] bs) {
        buffer.put(bs);
    }

    @Override
    public void bytes(String string, byte[] buf, int offset, int size) {
        buffer.put(buf, offset, size);
    }

    public void doUleb128(int value) {
        int remaining = value >>> 7;

        while (remaining != 0) {
            buffer.put((byte) ((value & 0x7f) | 0x80));
            value = remaining;
            remaining >>>= 7;
        }

        buffer.put((byte) (value & 0x7f));
    }

    @Override
    public void end() {
    }

    @Override
    public int offset() {
        return buffer.position();
    }

    @Override
    public void sbyte(String s, int b) {
        buffer.put((byte) b);
    }

    @Override
    public void sint(String s, int i) {
        buffer.putInt(i);
    }

    @Override
    public void skip(String s, int n) {
        buffer.position(buffer.position() + n);
    }

    @Override
    public void skip4(String s) {
        buffer.putInt(0);
    }

    @Override
    public void sleb128(String s, int value) {
        int remaining = value >> 7;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;

        while (hasMore) {
            hasMore = (remaining != end)
                    || ((remaining & 1) != ((value >> 6) & 1));

            buffer.put((byte) ((value & 0x7f) | (hasMore ? 0x80 : 0)));
            value = remaining;
            remaining >>= 7;
        }

    }

    @Override
    public void sshort(String s, int i) {
        buffer.putShort((short) i);
    }

    @Override
    public void ubyte(String s, int b) {
        buffer.put((byte) b);
    }

    @Override
    public void uint(String s, int i) {
        buffer.putInt(i);
    }

    @Override
    public void uleb128(String s, int value) {
        doUleb128(value);
    }

    @Override
    public void uleb128p1(String s, int i) {
        doUleb128(i + 1);
    }

    @Override
    public void ushort(String s, int i) {
        buffer.putShort((short) i);
    }
}
