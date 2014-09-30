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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapListItem extends BaseItem {
    final public List<SectionItem<?>> items = new ArrayList<>();

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
        for (Iterator<SectionItem<?>> it = items.iterator(); it.hasNext(); ) {
            SectionItem<?> i = it.next();
            if (i == null || i.items.size() < 1) {
                it.remove();
            }
        }
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
