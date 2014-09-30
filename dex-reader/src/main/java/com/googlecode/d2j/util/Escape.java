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
package com.googlecode.d2j.util;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Escape implements DexConstants {

    static boolean contain(int a, int b) {
        return (a & b) != 0;
    }

    public static String classAcc(int acc) {
        if (acc == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        if (contain(acc, ACC_PUBLIC)) {
            sb.append("ACC_PUBLIC|");
        }
        if (contain(acc, ACC_PRIVATE)) {
            sb.append("ACC_PRIVATE|");
        }
        if (contain(acc, ACC_PROTECTED)) {
            sb.append("ACC_PROTECTED|");
        }
        if (contain(acc, ACC_STATIC)) {
            sb.append("ACC_STATIC|");
        }
        if (contain(acc, ACC_FINAL)) {
            sb.append("ACC_FINAL|");
        }
        if (contain(acc, ACC_INTERFACE)) {
            sb.append("ACC_INTERFACE|");
        }
        if (contain(acc, ACC_ABSTRACT)) {
            sb.append("ACC_ABSTRACT|");
        }
        if (contain(acc, ACC_SYNTHETIC)) {
            sb.append("ACC_SYNTHETIC|");
        }
        if (contain(acc, ACC_ANNOTATION)) {
            sb.append("ACC_ANNOTATION|");
        }
        if (contain(acc, ACC_ENUM)) {
            sb.append("ACC_ENUM|");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String methodAcc(int acc) {
        if (acc == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        if (contain(acc, ACC_PUBLIC)) {
            sb.append("ACC_PUBLIC|");
        }
        if (contain(acc, ACC_PRIVATE)) {
            sb.append("ACC_PRIVATE|");
        }
        if (contain(acc, ACC_PROTECTED)) {
            sb.append("ACC_PROTECTED|");
        }
        if (contain(acc, ACC_STATIC)) {
            sb.append("ACC_STATIC|");
        }
        if (contain(acc, ACC_FINAL)) {
            sb.append("ACC_FINAL|");
        }
        if (contain(acc, ACC_BRIDGE)) {
            sb.append("ACC_BRIDGE|");
        }
        if (contain(acc, ACC_VARARGS)) {
            sb.append("ACC_VARARGS|");
        }
        if (contain(acc, ACC_NATIVE)) {
            sb.append("ACC_NATIVE|");
        }
        if (contain(acc, ACC_ABSTRACT)) {
            sb.append("ACC_ABSTRACT|");
        }
        if (contain(acc, ACC_STRICT)) {
            sb.append("ACC_STRICT|");
        }
        if (contain(acc, ACC_SYNTHETIC)) {
            sb.append("ACC_SYNTHETIC|");
        }
        if (contain(acc, ACC_CONSTRUCTOR)) {
            sb.append("ACC_CONSTRUCTOR|");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String fieldAcc(int acc) {
        if (acc == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        if (contain(acc, ACC_PUBLIC)) {
            sb.append("ACC_PUBLIC|");
        }
        if (contain(acc, ACC_PRIVATE)) {
            sb.append("ACC_PRIVATE|");
        }
        if (contain(acc, ACC_PROTECTED)) {
            sb.append("ACC_PROTECTED|");
        }
        if (contain(acc, ACC_STATIC)) {
            sb.append("ACC_STATIC|");
        }
        if (contain(acc, ACC_FINAL)) {
            sb.append("ACC_FINAL|");
        }
        if (contain(acc, ACC_VOLATILE)) {
            sb.append("ACC_VOLATILE|");
        }
        if (contain(acc, ACC_TRANSIENT)) {
            sb.append("ACC_TRANSIENT|");
        }

        if (contain(acc, ACC_SYNTHETIC)) {
            sb.append("ACC_SYNTHETIC|");
        }
        if (contain(acc, ACC_ENUM)) {
            sb.append("ACC_ENUM|");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String v(Field f) {
        return String.format("new Field(%s,%s,%s)", v(f.getOwner()), v(f.getName()), v(f.getType()));
    }

    public static String v(Method m) {
        return String.format("new Method(%s,%s,%s,%s)", v(m.getOwner()), v(m.getName()), v(m.getParameterTypes()),
                v(m.getReturnType()));
    }

    public static String v(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + Utf8Utils.escapeString(s) + "\"";
    }

    public static String v(DexType t) {
        return "new DexType(" + v(t.desc) + ")";

    }

    public static String v(int[] vs) {
        StringBuilder sb = new StringBuilder("new int[]{ ");
        boolean first = true;
        for (int obj : vs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(obj);
        }
        return sb.append("}").toString();
    }

    public static String v(byte[] vs) {
        StringBuilder sb = new StringBuilder("new byte[]{ ");
        boolean first = true;
        for (byte obj : vs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("(byte)").append(obj);
        }
        return sb.append("}").toString();
    }

    public static String v(String[] vs) {
        if (vs == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("new String[]{ ");
        boolean first = true;
        for (String obj : vs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(v(obj));
        }
        return sb.append("}").toString();
    }

    public static String v(Object[] vs) {
        StringBuilder sb = new StringBuilder("new Object[]{ ");
        boolean first = true;
        for (Object obj : vs) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(v(obj));
        }
        return sb.append("}").toString();
    }

    public static String v(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return v((String) obj);
        }

        if (obj instanceof DexType) {
            return v((DexType) obj);
        }

        if (obj instanceof Method) {
            return v((Method) obj);
        }
        if (obj instanceof Field) {
            return v((Field) obj);
        }

        if (obj instanceof Integer) {
            return " Integer.valueOf(" + obj + ")";
        }
        if (obj instanceof Long) {
            return "Long.valueOf(" + obj + "L)";
        }
        if (obj instanceof Float) {
            return "Float.valueOf(" + obj + "F)";
        }
        if (obj instanceof Double) {
            return "Double.valueOf(" + obj + "D)";
        }
        if (obj instanceof Short) {
            return "Short.valueOf((short)" + obj + ")";
        }
        if (obj instanceof Byte) {
            return "Byte.valueOf((byte)" + obj + ")";
        }
        if (obj instanceof Character) {
            return "Character.valueOf('" + obj + "')";
        }
        if (obj instanceof Boolean) {
            return "Boolean.valueOf(" + obj + ")";
        }
        if (obj instanceof int[]) {
            StringBuilder sb = new StringBuilder("new int[]{ ");
            boolean first = true;
            for (int i : (int[]) obj) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(i);
            }
            return sb.append("}").toString();
        }
        if (obj instanceof short[]) {
            StringBuilder sb = new StringBuilder("new short[]{ ");
            boolean first = true;
            for (int i : (short[]) obj) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append("(short)").append(i);
            }
            return sb.append("}").toString();
        }
        if (obj instanceof long[]) {
            StringBuilder sb = new StringBuilder("new long[]{ ");
            boolean first = true;
            for (long i : (long[]) obj) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(i).append("L");
            }
            return sb.append("}").toString();
        }
        if (obj instanceof float[]) {
            StringBuilder sb = new StringBuilder("new float[]{ ");
            boolean first = true;
            for (float i : (float[]) obj) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(i).append("F");
            }
            return sb.append("}").toString();
        }
        return null;
    }
}
