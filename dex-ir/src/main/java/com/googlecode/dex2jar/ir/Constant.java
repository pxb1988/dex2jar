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
package com.googlecode.dex2jar.ir;

import org.objectweb.asm.Type;
import org.objectweb.asm.util.AbstractVisitor;

import com.googlecode.dex2jar.ir.Value.E0Expr;

/**
 * Represent a constant, number/string/type
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class Constant extends E0Expr {

    public static final Object Null = new Object();
    public static Type STRING = Type.getType(String.class);

    public static Constant n(Type type, Object value) {
        return new Constant(type, value);
    }

    public static Constant nByte(byte i) {
        return new Constant(Type.BYTE_TYPE, i);
    }

    public static Constant nChar(char i) {
        return new Constant(Type.CHAR_TYPE, i);
    }

    public static Constant nClass(Type clz) {
        return new Constant(Type.getType("Ljava/lang/Class;"), clz);
    }

    public static Constant nDouble(double i) {
        return new Constant(Type.DOUBLE_TYPE, i);
    }

    public static Constant nFloat(float i) {
        return new Constant(Type.FLOAT_TYPE, i);
    }

    public static Constant nInt(int i) {
        return new Constant(Type.INT_TYPE, i);
    }

    public static Constant nLong(long i) {
        return new Constant(Type.LONG_TYPE, i);
    }

    public static Constant nNull() {
        return new Constant(Constant.Null);
    }

    public static Constant nShort(short i) {
        return new Constant(Type.SHORT_TYPE, i);
    }

    public static Constant nString(String i) {
        return new Constant(STRING, i);
    }

    public Type type;
    public Object value;

    public Constant(Object value) {
        super(VT.CONSTANT);
        this.value = value;
    }

    public Constant(Type type, Object value) {
        super(VT.CONSTANT);
        this.value = value;
        this.type = type;
    }

    @Override
    public Value clone() {
        return new Constant(type, value);
    }

    public String toString() {
        if (Null == value) {
            return "null";
        }
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            if (value instanceof Float) {
                return value.toString() + "F";
            }
            if (value instanceof Long) {
                return value.toString() + "L";
            }
            return value.toString();
        }
        if (value instanceof String) {
            StringBuffer buf = new StringBuffer();
            AbstractVisitor.appendString(buf, (String) value);
            return buf.toString();
        }
        if (type.equals(Type.getType(Class.class))) {
            return ToStringUtil.toShortClassName((Type) value) + ".class";
        }
        return "" + value;
    }
}
