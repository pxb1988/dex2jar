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

import com.googlecode.d2j.dex.writer.ann.Alignment;
import com.googlecode.d2j.dex.writer.ann.Idx;
import com.googlecode.d2j.dex.writer.io.DataOut;

@Alignment(4)

public class FieldIdItem extends BaseItem implements Comparable<FieldIdItem> {
    @Idx
    public final TypeIdItem clazz;
    @Idx
    public final TypeIdItem type;
    @Idx
    public final StringIdItem name;

    public String getTypeString() {
        return type.descriptor.stringData.string;
    }

    public FieldIdItem(TypeIdItem clazz, StringIdItem name, TypeIdItem type) {
        super();
        this.clazz = clazz;
        this.name = name;
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldIdItem other = (FieldIdItem) obj;
        if (clazz == null) {
            if (other.clazz != null)
                return false;
        } else if (!clazz.equals(other.clazz))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public int place(int offset) {
        return offset + 8;
    }

    @Override
    public int compareTo(FieldIdItem o) {
        int x = clazz.compareTo(o.clazz);
        if (x != 0) {
            return x;
        }
        x = name.compareTo(o.name);
        if (x != 0) {
            return x;
        }
        return type.compareTo(o.type);
    }

    @Override
    public void write(DataOut out) {
        out.ushort("class_idx", clazz.index);
        out.ushort("proto_idx", type.index);
        out.uint("name_idx", name.index);
    }

}
