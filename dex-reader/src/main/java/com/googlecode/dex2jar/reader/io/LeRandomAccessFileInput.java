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

import java.io.File;

public class LeRandomAccessFileInput extends BeRandomAccessFileInput {

    public LeRandomAccessFileInput(File file) {
        super(file);
    }

    @Override
    public int readUShortx() {
        return readUByte() | (readUByte() << 8);
    }

    @Override
    public int readUIntx() {
        return readUByte() | (readUByte() << 8) | (readUByte() << 16) | (readUByte() << 24);

    }

    @Override
    public int readIntx() {
        return readUIntx();
    }

    @Override
    public int readShortx() {
        return (short) readUShortx();
    }

}
