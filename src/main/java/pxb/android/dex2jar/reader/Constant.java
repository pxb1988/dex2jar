/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package pxb.android.dex2jar.reader;

import org.objectweb.asm.Type;

import pxb.android.dex2jar.DataIn;
import pxb.android.dex2jar.Dex;

/**
 * 读取静态常量
 * 
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class Constant {

    /**
     * 读取静态常量
     * 
     * @param dex
     * @param in
     * @return
     */
    public static Object ReadConstant(Dex dex, DataIn in) {
        int b = in.readByte();
        int type = b & 0x1f;
        switch (type) {
        case 0:
            return new Byte((byte) x0246(in, b));
        case 2:
            return new Short((short) x0246(in, b));
        case 4:
            return new Integer((int) x0246(in, b));
        case 6:
            return new Long(x0246(in, b));
        case 3:
            return new Character((char) x3(in, b));
        case 23:
            return dex.getString((int) x3(in, b));
        case 16:
            return Float.intBitsToFloat((int) (xf(in, b) >> 32));
        case 17:
            return Double.longBitsToDouble(xf(in, b));
        case 30:
            return null;// null
        case 28: {
            int size = in.readByte();
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = ReadConstant(dex, in);
            }
            return array;
        }
        case 24: {
            int type_id = (int) x3(in, b);
            return Type.getType(dex.getType(type_id));
        }
        case 31: {
            return new Boolean(((b >> 5) & 0x3) != 0);
        }
        default:
            throw new RuntimeException("Not support yet.");
        }
    }

    public static long x0246(DataIn in, int before) {
        int length = ((before >> 5) & 0x7) + 1;
        long value = 0;
        for (int j = 0; j < length; j++) {
            value |= ((long) in.readByte()) << (j * 8);
        }
        return value;
    }

    public static long x3(DataIn in, int before) {
        int length = ((before >> 5) & 0x7) + 1;
        long value = 0;
        for (int j = 0; j < length; j++) {
            value |= in.readByte() << (j * 8);
        }
        return value;
    }

    public static long xf(DataIn in, int before) {
        int bytes = ((before >> 5) & 0x7) + 1;
        long result = 0L;
        int bitpos = 0;
        for (int i = 0; i < bytes; ++i, bitpos += 8) {
            int b = in.readByte();
            result |= (long) b << bitpos;
        }
        result <<= (8 - bytes) * 8;
        return result;
    }
}
