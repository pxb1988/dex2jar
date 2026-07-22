package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.ArrayList;
import java.util.List;

public class MapListItem extends BaseItem {

    public final List<SectionItem<?>> items = new ArrayList<>();

    public int getSize() {
        return 4 + items.size() * 12;
    }

    public void writeMapItem(DataOut out, int type, int size, int offset) {
        out.begin("map_item");
        out.ushort("type", type);
        out.ushort("unused", 0);
        out.uint("size", size);
        out.uint("offset", offset);
        out.end();
    }

    public void cleanZeroSizeEntry() {
        items.removeIf(i -> i == null || i.items.isEmpty());
    }

    public void write(DataOut out) {
        out.begin("map_list");
        out.uint("size", items.size());
        for (SectionItem<?> t : items) {
            writeMapItem(out, t.sectionType.code, t.items.size(), t.offset);
        }
        out.end();
        items.clear();
    }

    @Override
    public int place(int offset) {
        return offset + 4 + items.size() * 12;
    }

}
