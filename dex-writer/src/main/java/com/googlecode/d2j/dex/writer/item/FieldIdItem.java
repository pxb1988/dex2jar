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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FieldIdItem other = (FieldIdItem) obj;
        if (clazz == null) {
            if (other.clazz != null) {
                return false;
            }
        } else if (!clazz.equals(other.clazz)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            return other.type == null;
        } else {
            return type.equals(other.type);
        }
    }

    @Override
    public int place(int offset) {
        return offset + 8;
    }

    @Override
    public int compareTo(FieldIdItem o) {
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
        return type.compareTo(o.type);
    }

    @Override
    public void write(DataOut out) {
        out.ushort("class_idx", clazz.index);
        out.ushort("proto_idx", type.index);
        out.uint("name_idx", name.index);
    }

}
