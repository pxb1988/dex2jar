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
 * @see DexFileReader#REVERSE_ENDIAN_CONSTANT
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
/* default */class ReverseEndianDataIn extends ByteArrayInputStream implements DataIn {

    private Stack<Integer> stack = new Stack<Integer>();

    public ReverseEndianDataIn(byte[] data) {
        super(data);
    }

    public ReverseEndianDataIn(byte[] data, int currentPosition) {
        this(data);
        move(currentPosition);
    }

    public int getCurrentPosition() {
        return super.pos;
    }

    public void move(int absOffset) {
        super.pos = absOffset;
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

    public int readByte() {
        return (byte) super.read();
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

    public int readIntx() {
        return (super.read() << 24) | (super.read() << 16) | (super.read() << 8) | super.read();
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

    public int readShortx() {
        return (short) readUShortx();
    }

    public int readUByte() {
        return super.read();
    }

    public int readUIntx() {
        return readIntx();
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

    @Override
    public int readUShortx() {
        return (readUByte() << 8) | readUByte();
    }

    public void skip(int bytes) {
        super.skip(bytes);
    }
}
