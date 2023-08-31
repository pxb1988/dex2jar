package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ev.EncodedArray;
import com.googlecode.d2j.dex.writer.io.DataOut;

public class CallSiteIdItem extends BaseItem implements Comparable<CallSiteIdItem> {
    String name;
    EncodedArray encodedArrayItem;

    public CallSiteIdItem(String name, EncodedArray encodedArrayItem) {
        this.name = name;
        this.encodedArrayItem = encodedArrayItem;
    }

    @Override
    public void write(DataOut out) {
        out.uint("call_site_off", encodedArrayItem.offset);
    }

    @Override
    public int place(int offset) {
        return offset + 4;
    }

    @Override
    public int compareTo(CallSiteIdItem o) {
        if (o == null) {
            return 1;
        }
        if (name != null) {
            if (o.name == null) {
                return 1;
            } else {
                int x = name.compareTo(o.name);
                if (x != 0) {
                    return x;
                }
            }
        } else {
            if (o.name != null) {
                return -1;
            }
        }
        return encodedArrayItem.compareTo(o.encodedArrayItem);
    }
}
