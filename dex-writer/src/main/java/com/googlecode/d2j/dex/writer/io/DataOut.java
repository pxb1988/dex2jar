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

public interface DataOut {

    void begin(String s);

    void bytes(String s, byte[] bs);

    void bytes(String string, byte[] buf, int offset, int size);

    void end();

    int offset();

    void sbyte(String s, int b);

    void sint(String s, int i);

    void skip(String s, int n);

    void skip4(String s);

    void sleb128(String s, int i);

    void sshort(String s, int i);

    void ubyte(String s, int b);

    void uint(String s, int i);

    void uleb128(String s, int i);

    void uleb128p1(String s, int i);

    void ushort(String s, int i);
}
