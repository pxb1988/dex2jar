package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ev.EncodedArray;
import com.googlecode.d2j.dex.writer.io.DataOut;

public class EncodedArrayItem extends BaseItem {

    public EncodedArray value = new EncodedArray();

    @Override
    public int place(int offset) {
        return value.place(offset);
    }

    @Override
    public void write(DataOut out) {
        value.write(out);
    }

}
