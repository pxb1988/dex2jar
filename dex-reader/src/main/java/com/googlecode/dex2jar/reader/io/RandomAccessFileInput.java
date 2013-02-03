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

import com.googlecode.dex2jar.DexException;

public class RandomAccessFileInput extends DataInputDataIn implements DataIn, Closeable {
    public RandomAccessFileInput(File file, boolean isLE) throws FileNotFoundException {
        super(new RandomAccessFile(file, "r"), isLE);
    }

    @Override
    public void close() throws IOException {
        ((RandomAccessFile) in).close();
    }

    @Override
    public int getCurrentPosition() {
        try {
            return (int) ((RandomAccessFile) in).getFilePointer();
        } catch (IOException e) {
            throw new DexException(e);
        }
    }

    @Override
    public void move(int absOffset) {
        try {
            ((RandomAccessFile) in).seek(absOffset);
        } catch (IOException e) {
            throw new DexException(e);
        }
    }
}
