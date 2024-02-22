package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ann.Alignment;
import com.googlecode.d2j.dex.writer.ann.Off;
import com.googlecode.d2j.dex.writer.io.DataOut;

@Alignment(4)
public class StringIdItem extends BaseItem implements Comparable<StringIdItem> {

    public StringIdItem(StringDataItem stringDataItem) {
        this.stringData = stringDataItem;
    }

    @Override
    public String toString() {
        return "StringIdItem [stringData=" + stringData + "]";
    }

    @Off
    public final StringDataItem stringData;

    @Override
    public int place(int offset) {
        return offset + 4;
    }

    @Override
    public int compareTo(StringIdItem o) {
        return stringData.compareTo(o.stringData);
    }

    @Override
    public void write(DataOut out) {
        out.uint("string_data_off", stringData.offset);
    }

}
