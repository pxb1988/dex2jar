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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MethodIdItem other = (MethodIdItem) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (proto == null) {
            if (other.proto != null) {
                return false;
            }
        } else if (!proto.equals(other.proto)) {
            return false;
        }
        if (clazz == null) {
            return other.clazz == null;
        } else {
            return clazz.equals(other.clazz);
        }
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
        if (o == null) {
            return 1;
        }
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
