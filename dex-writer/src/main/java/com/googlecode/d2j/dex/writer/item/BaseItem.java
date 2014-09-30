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

import com.googlecode.d2j.dex.writer.io.DataOut;

public abstract class BaseItem {
    public static final int NO_INDEX = -1;
    public int index;
    public int offset;

    static protected void addPadding(DataOut out, int alignment) {
        int x = out.offset() % alignment;
        if (x != 0) {
            out.skip("padding", alignment - x);// Padding
        }
    }

    static public void addPadding(DataOut out, int offset, int alignment) {
        int x = offset % alignment;
        if (x != 0) {
            out.skip("padding", alignment - x);// Padding
        }
    }

    public static int padding(int offset, int alignment) {
        int x = offset % alignment;
        if (x != 0) {
            offset += alignment - x;// Padding
        }
        return offset;
    }

    public static int lengthOfSleb128(int value) {
        int remaining = value >> 7;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;
        int count = 0;
        while (hasMore) {
            hasMore = (remaining != end)
                    || ((remaining & 1) != ((value >> 6) & 1));
            count++;
            value = remaining;
            remaining >>= 7;
        }
        return count;
    }

    public static int lengthOfUleb128(final int s) {
        int length = 1;
        if (s > 0x7f) {
            length++;
            if (s > (0x3fff)) {
                length++;
                if (s > (0x1fffff)) {
                    length++;
                    if (s > (0xfffffff)) {
                        length++;
                    }
                }
            }
        }
        return length;
    }

    public abstract void write(DataOut out);

    public abstract int place(int offset);
}
