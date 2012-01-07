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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Stack;

import com.googlecode.dex2jar.DexException;

public class BeRandomAccessFileInput implements DataIn, Closeable {
    protected RandomAccessFile r;

    public BeRandomAccessFileInput(File file) {
        try {
            r = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            throw new DexException(e);
        }
    }

    @Override
    public int getCurrentPosition() {
        try {
            return (int) r.getFilePointer();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    private Stack<Integer> stack = new Stack<Integer>();

    @Override
    public void move(int absOffset) {
        try {
            r.seek(absOffset);
        } catch (IOException e) {
            throw new DexException(e);
        }
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
            return r.read();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public byte[] readBytes(int size) {
        byte[] data = new byte[size];
        try {
            r.read(data);
        } catch (IOException e) {
            throw new DexException(e);
        }
        return data;
    }

    @Override
    public int readIntx() {
        return (int) readUIntx();
    }

    @Override
    public int readUIntx() {
        try {
            return r.readInt();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public int readShortx() {
        try {
            return r.readShort();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public int readUShortx() {
        try {
            return r.readUnsignedShort();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    public long readLeb128() {
        int bitpos = 0;
        long vln = 0L;
        do {
            int inp = readUByte();
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
    public int readUByte() {
        try {
            return r.readUnsignedByte();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public void skip(int bytes) {
        try {
            r.skipBytes(bytes);
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public void close() throws IOException {
        r.close();
    }

}
