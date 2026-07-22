package com.googlecode.d2j.dex.writer.ev;

import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.BaseItem;
import com.googlecode.d2j.dex.writer.item.FieldIdItem;
import com.googlecode.d2j.dex.writer.item.MethodHandleItem;
import com.googlecode.d2j.dex.writer.item.MethodIdItem;
import com.googlecode.d2j.dex.writer.item.ProtoIdItem;
import com.googlecode.d2j.dex.writer.item.StringIdItem;
import com.googlecode.d2j.dex.writer.item.TypeIdItem;
import java.util.Objects;

public class EncodedValue implements Comparable<EncodedValue> {

    public static final int VALUE_ANNOTATION = 0x1d;

    public static final int VALUE_ARRAY = 0x1c;

    public static final int VALUE_BOOLEAN = 0x1f;

    public static final int VALUE_BYTE = 0x00;

    public static final int VALUE_CHAR = 0x03;

    public static final int VALUE_DOUBLE = 0x11;

    public static final int VALUE_ENUM = 0x1b;

    public static final int VALUE_FIELD = 0x19;

    public static final int VALUE_FLOAT = 0x10;

    public static final int VALUE_INT = 0x04;

    public static final int VALUE_LONG = 0x06;

    public static final int VALUE_METHOD_TYPE = 0x15;

    public static final int VALUE_METHOD_HANDLE = 0x16;

    public static final int VALUE_METHOD = 0x1a;

    public static final int VALUE_NULL = 0x1e;

    public static final int VALUE_SHORT = 0x02;

    public static final int VALUE_STRING = 0x17;

    public static final int VALUE_TYPE = 0x18;

    public final int valueType;

    public Object value;

    public EncodedValue(int valueType, Object value) {
        this.valueType = valueType;
        this.value = value;
    }

    public static int lengthOfDouble(double value) {
        int requiredBits = 64 - Long.numberOfTrailingZeros(Double.doubleToRawLongBits(value));
        if (requiredBits == 0) {
            requiredBits = 1;
        }
        return (requiredBits + 0x07) >> 3;
    }

    public static int lengthOfFloat(float value) {
        int requiredBits = 64 - Long.numberOfTrailingZeros(((long) (Float.floatToRawIntBits(value))) << 32);
        if (requiredBits == 0) {
            requiredBits = 1;
        }
        return (requiredBits + 0x07) >> 3;
    }

    public static int lengthOfSint(int value) {
        int nbBits = 33 - Integer.numberOfLeadingZeros(value ^ (value >> 31));
        return (nbBits + 0x07) >> 3;
    }

    public static int lengthOfSint(long value) {
        int nbBits = 65 - Long.numberOfLeadingZeros(value ^ (value >> 63));
        return (nbBits + 0x07) >> 3;
    }

    public static int lengthOfUint(int val) {
        int size = 1;
        if (val != 0) {
            val = val >>> 8;
            if (val != 0) {
                size++;
                val = val >>> 8;
                if (val != 0) {
                    size++;
                    val = val >>> 8;
                    if (val != 0) {
                        size++;
                    }
                }
            }
        }
        return size;
    }

    public static EncodedValue wrap(Object v) {
        if (v == null) {
            return new EncodedValue(VALUE_NULL, null);
        }
        if (v instanceof Integer) {
            return new EncodedValue(VALUE_INT, v);
        } else if (v instanceof Short) {
            return new EncodedValue(VALUE_SHORT, v);
        } else if (v instanceof Character) {
            return new EncodedValue(VALUE_CHAR, v);
        } else if (v instanceof Long) {
            return new EncodedValue(VALUE_LONG, v);
        } else if (v instanceof Float) {
            return new EncodedValue(VALUE_FLOAT, v);
        } else if (v instanceof Double) {
            return new EncodedValue(VALUE_DOUBLE, v);
        } else if (v instanceof Boolean) {
            return new EncodedValue(VALUE_BOOLEAN, v);
        } else if (v instanceof Byte) {
            return new EncodedValue(VALUE_BYTE, v);
        } else if (v instanceof TypeIdItem) {
            return new EncodedValue(VALUE_TYPE, v);
        } else if (v instanceof StringIdItem) {
            return new EncodedValue(VALUE_STRING, v);
        } else if (v instanceof FieldIdItem) {
            return new EncodedValue(VALUE_FIELD, v);
        } else if (v instanceof MethodIdItem) {
            return new EncodedValue(VALUE_METHOD, v);
        } else if (v instanceof MethodHandleItem) {
            return new EncodedValue(VALUE_METHOD_HANDLE, v);
        } else if (v instanceof ProtoIdItem) {
            return new EncodedValue(VALUE_METHOD_TYPE, v);
        }


        throw new RuntimeException("not support");
    }

    public static EncodedValue defaultValueForType(String typeString) {
        switch (typeString.charAt(0)) {
        case '[':
        case 'L':
            return new EncodedValue(VALUE_NULL, null);
        case 'B':
            return new EncodedValue(VALUE_BYTE, (byte) 0);
        case 'Z':
            return new EncodedValue(VALUE_BOOLEAN, false);
        case 'S':
            return new EncodedValue(VALUE_SHORT, (short) 0);
        case 'C':
            return new EncodedValue(VALUE_CHAR, (char) 0);
        case 'I':
            return new EncodedValue(VALUE_INT, 0);
        case 'F':
            return new EncodedValue(VALUE_FLOAT, (float) 0);
        case 'D':
            return new EncodedValue(VALUE_DOUBLE, (double) 0);
        case 'J':
            return new EncodedValue(VALUE_LONG, (long) 0);
        default:
            throw new RuntimeException();
        }
    }

    static byte[] encodeLong(int length, long value) {
        byte[] data = new byte[length];
        switch (length) {
        case 8:
            data[7] = (byte) (value >> 56);
        case 7:
            data[6] = (byte) (value >> 48);
        case 6:
            data[5] = (byte) (value >> 40);
        case 5:
            data[4] = (byte) (value >> 32);
        case 4:
            data[3] = (byte) (value >> 24);
        case 3:
            data[2] = (byte) (value >> 16);
        case 2:
            data[1] = (byte) (value >> 8);
        case 1:
            data[0] = (byte) (value);
            break;
        default:
            throw new RuntimeException();

        }
        return data;
    }

    static byte[] encodeSint(int length, int value) {
        byte[] data = new byte[length];
        switch (length) {
        case 4:
            data[3] = (byte) (value >> 24);
        case 3:
            data[2] = (byte) (value >> 16);
        case 2:
            data[1] = (byte) (value >> 8);
        case 1:
            data[0] = (byte) (value);
            break;
        default:
            throw new RuntimeException();
        }
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EncodedValue that = (EncodedValue) o;

        if (valueType != that.valueType) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = valueType;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public boolean isDefaultValueForType() {
        if (valueType == VALUE_NULL) {
            return true;
        }
        switch (valueType) {
        case VALUE_CHAR:
            Character c = (Character) this.value;
            return c == 0;
        case VALUE_BYTE:
        case VALUE_INT:
        case VALUE_SHORT:
            return ((Number) this.value).intValue() == 0;
        case VALUE_LONG:
            return ((Number) this.value).longValue() == 0;
        case VALUE_FLOAT:
            return ((Number) this.value).floatValue() == 0.0f;
        case VALUE_DOUBLE:
            return ((Number) this.value).doubleValue() == 0.0;
        case VALUE_BOOLEAN:
            Boolean z = (Boolean) this.value;
            return Boolean.FALSE.equals(z);
        default:
            break;
        }
        return false;
    }

    protected int doPlace(int offset) {
        switch (valueType) {
        case VALUE_NULL:
        case VALUE_BOOLEAN:
            return offset;
        case VALUE_ARRAY: {
            EncodedArray ea = (EncodedArray) value;
            return ea.place(offset);
        }
        case VALUE_ANNOTATION: {
            EncodedAnnotation ea = (EncodedAnnotation) value;
            return ea.place(offset);
        }
        case VALUE_STRING:
        case VALUE_TYPE:
        case VALUE_FIELD:
        case VALUE_METHOD:
        case VALUE_ENUM:
        case VALUE_METHOD_HANDLE:
        case VALUE_METHOD_TYPE:
        default:
            return offset + getValueArg() + 1;
        }
    }

    protected int getValueArg() {
        switch (valueType) {
        case VALUE_NULL:
        case VALUE_ANNOTATION:
        case VALUE_ARRAY:
        case VALUE_BYTE:
            return 0;
        case VALUE_BOOLEAN:
            return Boolean.TRUE.equals(value) ? 1 : 0;
        case VALUE_SHORT:
        case VALUE_INT:
            return lengthOfSint(((Number) value).intValue()) - 1;
        case VALUE_CHAR:
            return lengthOfUint((Character) value) - 1;
        case VALUE_LONG:
            return lengthOfSint(((Number) value).longValue()) - 1;
        case VALUE_DOUBLE:
            return lengthOfDouble(((Number) value).doubleValue()) - 1;
        case VALUE_FLOAT:
            return lengthOfFloat(((Number) value).floatValue()) - 1;
        case VALUE_STRING:
        case VALUE_TYPE:
        case VALUE_FIELD:
        case VALUE_METHOD:
        case VALUE_ENUM:
        case VALUE_METHOD_HANDLE:
        case VALUE_METHOD_TYPE:
            BaseItem bi = (BaseItem) value;
            return lengthOfUint(bi.index) - 1;
        default:
            break;
        }
        return 0;
    }

    public final int place(int offset) {
        offset += 1;
        return doPlace(offset);
    }

    public void write(DataOut out) {
        int valueArg = getValueArg();
        out.ubyte("(value_arg << 5 | value_type", valueArg << 5 | valueType);
        switch (valueType) {
        case VALUE_NULL:
        case VALUE_BOOLEAN:
            // nop
            break;
        case VALUE_SHORT:
            out.bytes("value_short", encodeSint(valueArg + 1, (Short) value));
            break;
        case VALUE_CHAR:
            out.bytes("value_char", encodeSint(valueArg + 1, (Character) value));
            break;
        case VALUE_INT:
            out.bytes("value_int", encodeSint(valueArg + 1, (Integer) value));
            break;
        case VALUE_LONG:
            out.bytes("value_long", encodeLong(valueArg + 1, (Long) value));
            break;
        case VALUE_DOUBLE:
            out.bytes("value_double", writeRightZeroExtendedValue(valueArg + 1,
                    Double.doubleToLongBits(((Number) value).doubleValue())));
            break;
        case VALUE_FLOAT:
            out.bytes("value_float", writeRightZeroExtendedValue(valueArg + 1,
                    ((long) Float.floatToIntBits((((Number) value).floatValue()))) << 32));
            break;
        case VALUE_STRING:
        case VALUE_TYPE:
        case VALUE_FIELD:
        case VALUE_METHOD:
        case VALUE_ENUM:
        case VALUE_METHOD_HANDLE:
        case VALUE_METHOD_TYPE:
            out.bytes("value_xidx", encodeLong(valueArg + 1, ((BaseItem) value).index));
            break;
        case VALUE_ARRAY: {
            EncodedArray ea = (EncodedArray) value;
            ea.write(out);
        }
        break;
        case VALUE_ANNOTATION: {
            EncodedAnnotation ea = (EncodedAnnotation) value;
            ea.write(out);
        }
        break;
        case VALUE_BYTE: {
            out.ubyte("value_byte", (Byte) value);
            break;
        }
        default:
            throw new RuntimeException();

        }
    }

    private byte[] writeRightZeroExtendedValue(int requiredBytes, long value) {
        value >>= 64 - (requiredBytes * 8L);
        byte[] s = new byte[requiredBytes];
        for (int i = 0; i < requiredBytes; i++) {
            s[i] = ((byte) value);
            value >>= 8;
        }
        return s;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(EncodedValue o) {
        if (o == null) {
            return 1;
        }
        int x = Integer.compare(valueType, o.valueType);
        if (x != 0) {
            return x;
        }
        switch (valueType) {
        case VALUE_NULL:
            return 0;
        case VALUE_BOOLEAN:
            return Boolean.compare((Boolean) value, (Boolean) o.value);
        case VALUE_SHORT:
            return ((Short) value).compareTo((Short) o.value);
        case VALUE_CHAR:
            return ((Character) value).compareTo((Character) o.value);
        case VALUE_INT:
            return ((Integer) value).compareTo((Integer) o.value);
        case VALUE_LONG:
            return ((Long) value).compareTo((Long) o.value);
        case VALUE_DOUBLE:
            return Long.compare(Double.doubleToLongBits((Double) value), Double.doubleToLongBits((Double) o.value));
        case VALUE_FLOAT:
            return Integer.compare(Float.floatToIntBits((Float) value), Float.floatToIntBits((Float) o.value));
        case VALUE_STRING:
        case VALUE_TYPE:
        case VALUE_FIELD:
        case VALUE_METHOD:
        case VALUE_ENUM:
        case VALUE_METHOD_HANDLE:
        case VALUE_METHOD_TYPE:
            if (value instanceof Comparable) {
                return ((Comparable) value).compareTo(o.value);
            } else {
                throw new RuntimeException();
            }
        case VALUE_ARRAY: {
            return ((EncodedArray) value).compareTo((EncodedArray) o.value);
        }
        case VALUE_ANNOTATION: {
            return ((EncodedAnnotation) value).compareTo((EncodedAnnotation) o.value);
        }
        case VALUE_BYTE: {
            return Byte.compare((Byte) value, (Byte) o.value);
        }
        default:
            throw new RuntimeException();
        }
    }
}
