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
public class MethodIdItem extends BaseItem implements Comparable<MethodIdItem> {

    public MethodIdItem(TypeIdItem typeItem, StringIdItem nameItem, ProtoIdItem protoIdItem) {
        super();
        this.clazz = typeItem;
        this.name = nameItem;
        this.proto = protoIdItem;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((proto == null) ? 0 : proto.hashCode());
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
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
        MethodIdItem other = (MethodIdItem) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (proto == null) {
            if (other.proto != null)
                return false;
        } else if (!proto.equals(other.proto))
            return false;
        if (clazz == null) {
            if (other.clazz != null)
                return false;
        } else if (!clazz.equals(other.clazz))
            return false;
        return true;
    }

    @Idx
    public final StringIdItem name;
    @Idx
    public final TypeIdItem clazz;
    @Idx
    public final ProtoIdItem proto;

    @Override
    public int place(int offset) {
        return offset + 0x08;
    }

    @Override
    public int compareTo(MethodIdItem o) {
        int x = clazz.compareTo(o.clazz);
        if (x != 0) {
            return x;
        }
        x = name.compareTo(o.name);
        if (x != 0) {
            return x;
        }
        return proto.compareTo(o.proto);
    }

    @Override
    public void write(DataOut out) {
        out.ushort("class_idx", clazz.index);
        out.ushort("proto_idx", proto.index);
        out.uint("name_idx", name.index);
    }

}
