/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package com.googlecode.dex2jar.reader.io;

import java.io.DataInput;
import java.io.IOException;
import java.util.Stack;

import com.googlecode.dex2jar.DexException;

public abstract class DataInputDataIn implements DataIn {
    protected DataInput in;
    private boolean isLE;

    private Stack<Integer> stack = new Stack<Integer>();

    public DataInputDataIn(DataInput in, boolean isLE) {
        this.in = in;
        this.isLE = isLE;
    }

    @Override
    public void pop() {
        move(stack.pop());
    }

    @Override
    public void push() {
        stack.push(getCurrentPosition());
    }

    @Override
    public void pushMove(int absOffset) {
        this.push();
        this.move(absOffset);
    }

    @Override
    public int readByte() {
        try {
            return in.readByte();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public byte[] readBytes(int size) {
        byte[] data = new byte[size];
        try {
            in.readFully(data);
        } catch (IOException e) {
            throw new DexException(e);
        }
        return data;
    }

    @Override
    public int readIntx() {
        return readUIntx();
    }

    @Override
    public long readLeb128() {
        int bitpos = 0;
        long vln = 0L;
        do {
            int inp = readUByte();
            vln |= ((long) (inp & 0x7F)) << bitpos;
            bitpos += 7;
            if ((inp & 0x80) == 0) {
                break;
            }
        } while (true);
        if (((1L << (bitpos - 1)) & vln) != 0) {
            vln -= (1L << bitpos);
        }
        return vln;
    }

    @Override
    public int readShortx() {
        try {
            return in.readShort();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public int readUByte() {
        try {
            return in.readUnsignedByte();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public int readUIntx() {
        if (isLE) {
            return readUByte() | (readUByte() << 8) | (readUByte() << 16) | (readUByte() << 24);
        } else {
            return (readUByte() << 24) | (readUByte() << 16) | (readUByte() << 8) | readUByte();
        }
    }

    @Override
    public long readULeb128() {
        long value = 0;
        int count = 0;
        int b = readUByte();
        while ((b & 0x80) != 0) {
            value |= (b & 0x7f) << count;
            count += 7;
            b = readUByte();
        }
        value |= (b & 0x7f) << count;
        return value;
    }

    @Override
    public int readUShortx() {
        if (isLE) {
            return readUByte() | (readUByte() << 8);
        } else {
            return (readUByte() << 8) | readUByte();
        }
    }

    @Override
    public void skip(int bytes) {
        try {
            in.skipBytes(bytes);
        } catch (IOException e) {
            throw new DexException(e);
        }
    }
}
