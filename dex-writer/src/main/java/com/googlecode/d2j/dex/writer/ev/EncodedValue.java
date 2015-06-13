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
import com.googlecode.d2j.dex.writer.item.*;

public class EncodedValue {

    public final static int VALUE_ANNOTATION = 0x1d;
    public final static int VALUE_ARRAY = 0x1c;
    public final static int VALUE_BOOLEAN = 0x1f;
    public final static int VALUE_BYTE = 0x00;
    public final static int VALUE_CHAR = 0x03;
    public final static int VALUE_DOUBLE = 0x11;
    public final static int VALUE_ENUM = 0x1b;
    public final static int VALUE_FIELD = 0x19;
    public final static int VALUE_FLOAT = 0x10;
    public final static int VALUE_INT = 0x04;
    public final static int VALUE_LONG = 0x06;
    public final static int VALUE_METHOD = 0x1a;
    public final static int VALUE_NULL = 0x1e;
    public final static int VALUE_SHORT = 0x02;
    public final static int VALUE_STRING = 0x17;
    public final static int VALUE_TYPE = 0x18;
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

    public final static int lengthOfUint(int val) {
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
                return new EncodedValue(VALUE_INT, (int) 0);
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
                data[0] = (byte) (value >> 0);
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
                data[0] = (byte) (value >> 0);
                break;
            default:
                throw new RuntimeException();
        }
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncodedValue that = (EncodedValue) o;

        if (valueType != that.valueType) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
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
                return c.charValue() == 0;
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
            default:
                return offset + getValueArg() + 1;
        }
    }

    protected int getValueArg() {
        switch (valueType) {
            case VALUE_NULL:
            case VALUE_ANNOTATION:
            case VALUE_ARRAY:
                return 0;
            case VALUE_BOOLEAN:
                return Boolean.TRUE.equals(value) ? 1 : 0;
            case VALUE_BYTE:
                return 0;
            case VALUE_SHORT:
            case VALUE_INT:
                return lengthOfSint(((Number) value).intValue()) - 1;
            case VALUE_CHAR:
                return lengthOfUint(((Character) value).charValue()) - 1;
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
                BaseItem bi = (BaseItem) value;
                return lengthOfUint(bi.index) - 1;
        }
        return 0;
    }

    final public int place(int offset) {
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
                out.bytes("value_double", writeRightZeroExtendedValue(valueArg+1,Double.doubleToLongBits( ((Number) value).doubleValue())));
                break;
            case VALUE_FLOAT:
                out.bytes("value_float", writeRightZeroExtendedValue(valueArg+1,((long)Float.floatToIntBits((((Number) value).floatValue())))<<32));
                break;
            case VALUE_STRING:
            case VALUE_TYPE:
            case VALUE_FIELD:
            case VALUE_METHOD:
            case VALUE_ENUM:
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
        value >>= 64 - (requiredBytes * 8);
        byte[] s = new byte[requiredBytes];
        for (int i = 0; i < requiredBytes; i++) {
            s[i] = ((byte) value);
            value >>= 8;
        }
        return s;
    }

}
