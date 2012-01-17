/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package com.googlecode.dex2jar.reader;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.DexType;
import com.googlecode.dex2jar.reader.io.DataIn;

/**
 * 读取静态常量
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
/* default */class Constant {
    private static final int VALUE_BYTE = 0;
    private static final int VALUE_SHORT = 2;
    private static final int VALUE_CHAR = 3;
    private static final int VALUE_INT = 4;
    private static final int VALUE_LONG = 6;
    private static final int VALUE_FLOAT = 16;
    private static final int VALUE_DOUBLE = 17;
    private static final int VALUE_STRING = 23;
    private static final int VALUE_TYPE = 24;
    private static final int VALUE_FIELD = 25;
    private static final int VALUE_METHOD = 26;
    private static final int VALUE_ENUM = 27;
    private static final int VALUE_ARRAY = 28;
    private static final int VALUE_ANNOTATION = 29;
    private static final int VALUE_NULL = 30;
    private static final int VALUE_BOOLEAN = 31;

    /**
     * 读取静态常量
     * 
     * @param dex
     * @param in
     * @return
     */
    public static Object ReadConstant(DexFileReader dex, DataIn in) {
        int b = in.readUByte();
        int type = b & 0x1f;
        switch (type) {
        case VALUE_BYTE:
            return new Byte((byte) readIntBits(in, b));

        case VALUE_SHORT:
            return new Short((short) readIntBits(in, b));

        case VALUE_INT:
            return new Integer((int) readIntBits(in, b));

        case VALUE_LONG:
            return new Long(readIntBits(in, b));

        case VALUE_CHAR:
            return new Character((char) readUIntBits(in, b));

        case VALUE_STRING:
            return dex.getString((int) readUIntBits(in, b));

        case VALUE_FLOAT:
            return Float.intBitsToFloat((int) (readFloatBits(in, b) >> 32));

        case VALUE_DOUBLE:
            return Double.longBitsToDouble(readFloatBits(in, b));

        case VALUE_NULL:
            return null;

        case VALUE_BOOLEAN: {
            return new Boolean(((b >> 5) & 0x3) != 0);

        }
        case VALUE_TYPE: {
            int type_id = (int) readUIntBits(in, b);
            return new DexType(dex.getType(type_id));
        }
        case VALUE_ENUM: {
            return dex.getField((int) readUIntBits(in, b));
        }

        case VALUE_METHOD: {
            int method_id = (int) readUIntBits(in, b);
            return dex.getMethod(method_id);

        }
        case VALUE_FIELD: {
            int field_id = (int) readUIntBits(in, b);
            return dex.getField(field_id);
        }
        case VALUE_ARRAY: {
            int size = (int) in.readULeb128();
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = ReadConstant(dex, in);
            }
            return array;
        }
        case VALUE_ANNOTATION: {

            int _type = (int) in.readULeb128();
            String _typeString = dex.getType(_type);
            int size = (int) in.readULeb128();
            Annotation ann = new Annotation(_typeString, true);
            for (int i = 0; i < size; i++) {
                int nameid = (int) in.readULeb128();
                String nameString = dex.getString(nameid);
                Object o = ReadConstant(dex, in);
                ann.items.add(new Annotation.Item(nameString, o));
            }
            return ann;
        }
        default:
            throw new DexException("Not support yet.");
        }
    }

    public static long readIntBits(DataIn in, int before) {
        int length = ((before >> 5) & 0x7) + 1;
        long value = 0;
        for (int j = 0; j < length; j++) {
            value |= ((long) in.readUByte()) << (j * 8);
        }
        int shift = (8 - length) * 8;
        return value << shift >> shift;
    }

    public static long readUIntBits(DataIn in, int before) {
        int length = ((before >> 5) & 0x7) + 1;
        long value = 0;
        for (int j = 0; j < length; j++) {
            value |= ((long) in.readUByte()) << (j * 8);
        }
        return value;
    }

    public static long readFloatBits(DataIn in, int before) {
        int bytes = ((before >> 5) & 0x7) + 1;
        long result = 0L;
        for (int i = 0; i < bytes; ++i) {
            result |= ((long) in.readUByte()) << (i * 8);
        }
        result <<= (8 - bytes) * 8;
        return result;
    }
}
