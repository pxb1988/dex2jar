package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public abstract class BaseItem {

    public static final int NO_INDEX = -1;

    public int index;

    public int offset;

    protected static void addPadding(DataOut out, int alignment) {
        int x = out.offset() % alignment;
        if (x != 0) {
            out.skip("padding", alignment - x); // Padding
        }
    }

    public static void addPadding(DataOut out, int offset, int alignment) {
        int x = offset % alignment;
        if (x != 0) {
            out.skip("padding", alignment - x); // Padding
        }
    }

    public static int padding(int offset, int alignment) {
        int x = offset % alignment;
        if (x != 0) {
            offset += alignment - x; // Padding
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

    public static int lengthOfUleb128(int value) {
        int remaining = value >>> 7;
        int length = 1;
        while (remaining != 0) {
            length++;
            remaining >>>= 7;
        }
        return length;
    }

    public abstract void write(DataOut out);

    public abstract int place(int offset);

}
