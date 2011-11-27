/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Stack;

/**
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
/* default */abstract class EndianDataIn extends ByteArrayInputStream implements DataIn {

    private Stack<Integer> stack = new Stack<Integer>();
    private int base;

    public EndianDataIn(byte[] data, int base) {
        super(data);
        this.base = base;
    }

    public int readShortx() {
        return (short) readUShortx();
    }

    public int readIntx() {
        return readUIntx();
    }

    public int getCurrentPosition() {
        return super.pos - base;
    }

    public void move(int absOffset) {
        super.pos = absOffset + base;
    }

    public void pop() {
        this.move(stack.pop());
    }

    public void push() {
        stack.push(super.pos);
    }

    public void pushMove(int absOffset) {
        this.push();
        this.move(absOffset);
    }

    public byte[] readBytes(int size) {
        byte[] data = new byte[size];
        try {
            super.read(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public long readLeb128() {
        int bitpos = 0;
        long vln = 0L;
        do {
            int inp = super.read();
            vln |= ((long) (inp & 0x7F)) << bitpos;
            bitpos += 7;
            if ((inp & 0x80) == 0)
                break;
        } while (true);
        if (((1L << (bitpos - 1)) & vln) != 0)
            vln -= (1L << bitpos);
        return vln;
    }

    public long readULeb128() {
        long value = 0;
        int count = 0;
        int b = super.read();
        while ((b & 0x80) != 0) {
            value |= (b & 0x7f) << count;
            count += 7;
            b = super.read();
        }
        value |= (b & 0x7f) << count;
        return value;
    }

    public void skip(int bytes) {
        super.skip(bytes);
    }

    public int readByte() {
        return (byte) readUByte();
    }

    public int readUByte() {
        if (super.pos >= super.count) {
            throw new RuntimeException("EOF");
        }
        return super.read();
    }
}

/**
 * @see DexFileReader#REVERSE_ENDIAN_CONSTANT
 * @author Panxiaobo
 * 
 */
class BigEndianDataIn extends EndianDataIn implements DataIn {

    public BigEndianDataIn(byte[] data, int base) {
        super(data, base);
    }

    @Override
    public int readUShortx() {
        return (readUByte() << 8) | readUByte();
    }

    public int readUIntx() {
        return (readUByte() << 24) | (readUByte() << 16) | (readUByte() << 8) | readUByte();
    }
}

/**
 * @see DexFileReader#ENDIAN_CONSTANT
 * @author Panxiaobo
 * 
 */
class LittleEndianDataIn extends EndianDataIn implements DataIn {

    public LittleEndianDataIn(byte[] data, int base) {
        super(data, base);
    }

    public int readUShortx() {
        return readUByte() | (readUByte() << 8);
    }

    public int readUIntx() {
        return readUByte() | (readUByte() << 8) | (readUByte() << 16) | (readUByte() << 24);

    }
}
