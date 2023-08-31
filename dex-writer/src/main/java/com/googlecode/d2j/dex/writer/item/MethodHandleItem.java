package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

import java.util.Objects;

public class MethodHandleItem extends BaseItem implements Comparable<MethodHandleItem> {
    public int type;
    public FieldIdItem field;
    public MethodIdItem method;

    @Override
    public void write(DataOut out) {
        out.ushort("method_handle_type", type);
        out.ushort("unused", 0);
        out.ushort("field_or_method_id", field != null ? field.index : method.index);
        out.ushort("unused", 0);
    }

    @Override
    public int place(int offset) {
        return offset + 8;
    }

    @Override
    public int compareTo(MethodHandleItem o) {
        if (o == null) {
            return 1;
        }
        int x = Integer.compare(type, o.type);
        if (x != 0) {
            return x;
        }
        if (field != null) {
            return field.compareTo(o.field);
        } else if (method != null) {
            return method.compareTo(o.method);
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodHandleItem that = (MethodHandleItem) o;
        return type == that.type && Objects.equals(field, that.field) && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, field, method);
    }
}
