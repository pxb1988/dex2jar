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
package com.googlecode.d2j.dex.writer.ev;

import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.BaseItem;

import java.util.ArrayList;
import java.util.List;

public class EncodedArray {

    public List<EncodedValue> values = new ArrayList<>(5);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncodedArray that = (EncodedArray) o;

        if (!values.equals(that.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    public int place(int offset) {
        offset += BaseItem.lengthOfUleb128(values.size());
        for (EncodedValue ev : values) {
            offset = ev.place(offset);
        }
        return offset;
    }

    public void write(DataOut out) {
        out.uleb128("size", values.size());
        for (EncodedValue ev : values) {
            ev.write(out);
        }
    }
}
