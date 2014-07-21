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
import com.googlecode.d2j.dex.writer.item.StringDataItem.Buffer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SectionItem<T extends BaseItem> extends BaseItem {
    final public SectionType sectionType;
    public final List<T> items = new ArrayList<>();
    int size = 0;

    public SectionItem(SectionType typeCode) {
        super();
        this.sectionType = typeCode;

    }

    public SectionItem(SectionType typeCode, Collection<T> itms) {
        super();
        this.sectionType = typeCode;
        this.items.addAll(itms);
    }

    public static void main(String... strings) throws IllegalArgumentException, IllegalAccessException {
        for (Field f : SectionItem.class.getFields()) {
            if (f.getType().equals(int.class)) {
                if (0 != (f.getModifiers() & Modifier.STATIC)) {
                    System.out.printf("%s(0x%04x,0,0),//\n", f.getName(), f.get(null));
                }
            }
        }
    }

    public int size() {
        return size;
    }

    public int place(int offset) {
        final int startOffset = offset;
        int index = 0;
        for (T t : items) {
            offset = padding(offset, sectionType.alignment);
            t.offset = offset;
            t.index = index;
            index++;
            offset = t.place(offset);
        }
        size = offset - startOffset;
        return offset;
    }

    public void write(DataOut out) {
        out.begin("Section:" + sectionType);
        List<T> items = this.items;
        if (sectionType == SectionType.TYPE_STRING_DATA_ITEM) {
            Buffer buff = new Buffer();

            for (int i = 0; i < items.size(); i++) {
                T t = items.get(i);
                items.set(i, null);
                addPadding(out, sectionType.alignment);
                if (out.offset() != t.offset) {
                    throw new RuntimeException();
                }
                StringDataItem stringDataItem = (StringDataItem) t;
                stringDataItem.write(out, buff);
                buff.reset();
            }
        } else {
            for (int i = 0; i < items.size(); i++) {
                T t = items.get(i);
                items.set(i, null);
                addPadding(out, sectionType.alignment);
                if (out.offset() != t.offset) {
                    System.err.println("Error for type:" + this.sectionType + ", " + t.index);
                    throw new RuntimeException();
                }
                t.write(out);
            }
        }
        out.end();
    }

    public enum SectionType {
        TYPE_HEADER_ITEM(0x0000, 1, 0), //
        TYPE_STRING_ID_ITEM(0x0001, 4, 0), //
        TYPE_TYPE_ID_ITEM(0x0002, 4, 0), //
        TYPE_PROTO_ID_ITEM(0x0003, 4, 0), //
        TYPE_FIELD_ID_ITEM(0x0004, 4, 0), //
        TYPE_METHOD_ID_ITEM(0x0005, 1, 0), //
        TYPE_CLASS_DEF_ITEM(0x0006, 4, 0), //
        TYPE_MAP_LIST(0x1000, 4, 0), //
        TYPE_TYPE_LIST(0x1001, 4, 0), //
        TYPE_ANNOTATION_SET_REF_LIST(0x1002, 4, 0), //
        TYPE_ANNOTATION_SET_ITEM(0x1003, 4, 0), //
        TYPE_CLASS_DATA_ITEM(0x2000, 1, 0), //
        TYPE_CODE_ITEM(0x2001, 4, 0), //
        TYPE_STRING_DATA_ITEM(0x2002, 1, 0), //
        TYPE_DEBUG_INFO_ITEM(0x2003, 1, 0), //
        TYPE_ANNOTATION_ITEM(0x2004, 1, 0), //
        TYPE_ENCODED_ARRAY_ITEM(0x2005, 1, 0), //
        TYPE_ANNOTATIONS_DIRECTORY_ITEM(0x2006, 4, 0), //
        ;
        public int code;
        public int alignment;

        SectionType(int typeCode, int alignment, int size) {
            this.code = typeCode;
            this.alignment = alignment;
        }
    }

}
