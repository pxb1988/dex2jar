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
package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.E0Expr;

import java.lang.reflect.Array;

/**
 * Represent a constant, number/string/type
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Constant extends E0Expr {
    static public class Type {
        public Type(String desc) {
            this.desc = desc;
        }

        /**
         * type descriptor, in TypeDescriptor format
         */
        final public String desc;

        @Override
        public String toString() {
            return desc;
        }
    }

    public static final Object Null = new Object();

    public Object value;

    public Constant(Object value) {
        super(VT.CONSTANT);
        this.value = value;
    }

    @Override
    public Value clone() {
        return new Constant(value);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new Constant(value);
    }

    @Override
    public String toString0() {
        if (Null == value) {
            return "null";
        } else if (value == null) {
            return "NULL";
        } else if (value instanceof Number) {
            if (value instanceof Float) {
                return value.toString() + "F";
            }
            if (value instanceof Long) {
                return value.toString() + "L";
            }
            return value.toString();
        } else if (value instanceof String) {
            StringBuffer buf = new StringBuffer();
            Util.appendString(buf, (String) value);
            return buf.toString();
        } else if (value instanceof Type) {
            return Util.toShortClassName(((Type) value).desc) + ".class";
        } else if (value.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            int size = Array.getLength(value);
            for (int i = 0; i < size; i++) {
                sb.append(Array.get(value, i)).append(",");
            }
            if (size > 0) {
                sb.setLength(sb.length() - 1);
            }
            sb.append("]");
            return sb.toString();
        }
        return "" + value;
    }
}
