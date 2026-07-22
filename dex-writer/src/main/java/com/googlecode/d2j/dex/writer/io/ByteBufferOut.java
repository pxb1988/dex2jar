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
