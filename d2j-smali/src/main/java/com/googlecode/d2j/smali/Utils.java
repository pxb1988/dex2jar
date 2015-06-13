package com.googlecode.d2j.smali;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils implements DexConstants {

    public static void doAccept(DexAnnotationVisitor dexAnnotationVisitor, String k, Object value) {
        if (value instanceof ArrayList) {
            DexAnnotationVisitor a = dexAnnotationVisitor.visitArray(k);
            for (Object o : (ArrayList) value) {
                doAccept(a, null, o);
            }
            a.visitEnd();
        } else if (value instanceof Ann) {
            Ann ann = (Ann) value;
            DexAnnotationVisitor a = dexAnnotationVisitor.visitAnnotation(k, ann.name);
            for (Map.Entry<String, Object> e : ann.elements) {
                doAccept(a, e.getKey(), e.getValue());
            }
            a.visitEnd();
        } else if (value instanceof Field) {
            Field f = (Field) value;
            dexAnnotationVisitor.visitEnum(k, f.getOwner(), f.getName());
        } else {
            dexAnnotationVisitor.visit(k, value);
        }
    }

    public static int getAcc(String name) {
        if (name.equals("public")) {
            return ACC_PUBLIC;
        } else if (name.equals("private")) {
            return ACC_PRIVATE;
        } else if (name.equals("protected")) {
            return ACC_PROTECTED;
        } else if (name.equals("static")) {
            return ACC_STATIC;
        } else if (name.equals("final")) {
            return ACC_FINAL;
        } else if (name.equals("synchronized")) {
            return ACC_SYNCHRONIZED;
        } else if (name.equals("volatile")) {
            return ACC_VOLATILE;
        } else if (name.equals("bridge")) {
            return ACC_BRIDGE;
        } else if (name.equals("varargs")) {
            return ACC_VARARGS;
        } else if (name.equals("transient")) {
            return ACC_TRANSIENT;
        } else if (name.equals("native")) {
            return ACC_NATIVE;
        } else if (name.equals("interface")) {
            return ACC_INTERFACE;
        } else if (name.equals("abstract")) {
            return ACC_ABSTRACT;
        } else if (name.equals("strict")) {
            return ACC_STRICT;
        } else if (name.equals("synthetic")) {
            return ACC_SYNTHETIC;
        } else if (name.equals("annotation")) {
            return ACC_ANNOTATION;
        } else if (name.equals("enum")) {
            return ACC_ENUM;
        } else if (name.equals("constructor")) {
            return ACC_CONSTRUCTOR;
        } else if (name.equals("declared-synchronized")) {
            return ACC_DECLARED_SYNCHRONIZED;
        }
        return 0;
    }

    public static List<String> listDesc(String desc) {
        List<String> list = new ArrayList(5);
        if (desc == null) {
            return list;
        }
        char[] chars = desc.toCharArray();
        int i = 0;
        while (i < chars.length) {
            switch (chars[i]) {
                case 'V':
                case 'Z':
                case 'C':
                case 'B':
                case 'S':
                case 'I':
                case 'F':
                case 'J':
                case 'D':
                    list.add(Character.toString(chars[i]));
                    i++;
                    break;
                case '[': {
                    int count = 1;
                    while (chars[i + count] == '[') {
                        count++;
                    }
                    if (chars[i + count] == 'L') {
                        count++;
                        while (chars[i + count] != ';') {
                            count++;
                        }
                    }
                    count++;
                    list.add(new String(chars, i, count));
                    i += count;
                    break;
                }
                case 'L': {
                    int count = 1;
                    while (chars[i + count] != ';') {
                        ++count;
                    }
                    count++;
                    list.add(new String(chars, i, count));
                    i += count;
                    break;
                }
                default:
                    throw new RuntimeException("can't parse type list: " + desc);
            }
        }
        return list;
    }

    public static String[] toTypeList(String s) {
        return listDesc(s).toArray(new String[0]);
    }

    static public Byte parseByte(String str) {
        return Byte.valueOf((byte) parseInt(str.substring(0, str.length() - 1)));
    }

    static public Short parseShort(String str) {
        return Short.valueOf((short) parseInt(str.substring(0, str.length() - 1)));
    }

    static public Long parseLong(String str) {
        int sof = 0;
        int end = str.length() - 1;
        int x = 1;
        if (str.charAt(sof) == '+') {
            sof++;
        } else if (str.charAt(sof) == '-') {
            sof++;
            x = -1;
        }
        BigInteger v;
        if (str.charAt(sof) == '0') {
            sof++;
            if (sof >= end) {
                return 0L;
            }
            char c = str.charAt(sof);
            if (c == 'x' || c == 'X') {// hex
                sof++;
                v = new BigInteger(str.substring(sof, end), 16);
            } else {// oct
                v = new BigInteger(str.substring(sof, end), 8);
            }
        } else {
            v = new BigInteger(str.substring(sof, end), 10);
        }
        if (x == -1) {
            return v.negate().longValue();
        } else {
            return v.longValue();
        }
    }

    static public float parseFloat(String str) {
        str = str.toLowerCase();
        int s = 0;
        float x = 1f;
        if (str.charAt(s) == '+') {
            s++;
        } else if (str.charAt(s) == '-') {
            s++;
            x = -1;
        }
        int e = str.length() - 1;
        if (str.charAt(e) == 'f') {
            e--;
        }
        str = str.substring(s, e + 1);
        if (str.equals("nan")) {
            return Float.NaN;
        }
        if (str.equals("infinity")) {
            return x < 0 ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        }
        return (float) x * Float.parseFloat(str);
    }

    static public double parseDouble(String str) {
        str = str.toLowerCase();
        int s = 0;
        double x = 1;
        if (str.charAt(s) == '+') {
            s++;
        } else if (str.charAt(s) == '-') {
            s++;
            x = -1;
        }
        int e = str.length() - 1;
        if (str.charAt(e) == 'd') {
            e--;
        }
        str = str.substring(s, e + 1);
        if (str.equals("nan")) {
            return Double.NaN;
        }
        if (str.equals("infinity")) {
            return x < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        return x * Double.parseDouble(str);
    }

    static public int parseInt(String str, int start, int end) {
        int sof = start;
        int x = 1;
        if (str.charAt(sof) == '+') {
            sof++;
        } else if (str.charAt(sof) == '-') {
            sof++;
            x = -1;
        }
        long v;
        if (str.charAt(sof) == '0') {
            sof++;
            if (sof >= end) {
                return 0;
            }
            char c = str.charAt(sof);
            if (c == 'x' || c == 'X') {// hex
                sof++;
                v = Long.parseLong(str.substring(sof, end), 16);
            } else {// oct
                v = Long.parseLong(str.substring(sof, end), 8);
            }
        } else {
            v = Long.parseLong(str.substring(sof, end), 10);
        }
        return (int) (v * x);
    }

    static public int parseInt(String str) {
        return parseInt(str, 0, str.length());
    }

    public static String unescapeStr(String str) {
        return unEscape(str);
    }

    public static Character unescapeChar(String str) {
        return unEscape(str).charAt(0);
    }

    public static int[] toIntArray(List<String> ss) {
        int vs[] = new int[ss.size()];
        for (int i = 0; i < ss.size(); i++) {
            vs[i] = parseInt(ss.get(i));
        }
        return vs;
    }

    public static byte[] toByteArray(List<Object> ss) {
        byte vs[] = new byte[ss.size()];
        for (int i = 0; i < ss.size(); i++) {
            vs[i] = ((Number) (ss.get(i))).byteValue();
        }
        return vs;
    }

    static Map<String, Op> ops = new HashMap();

    static {
        for (Op op : Op.values()) {
            ops.put(op.displayName, op);
        }
    }

    static public Op getOp(String name) {
        return ops.get(name);
    }

    public static String unEscape(String str) {
        return unEscape0(str, 1, str.length() - 1);
    }

    public static String unEscapeId(String str) {
        return unEscape0(str, 0, str.length());
    }

    public static int findString(String str, int start, int end, char dEnd) {
        for (int i = start; i < end; ) {
            char c = str.charAt(i);
            if (c == '\\') {
                char d = str.charAt(i + 1);
                switch (d) {
                    // ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
                    case 'b':
                    case 't':
                    case 'n':
                    case 'f':
                    case 'r':
                    case '\"':
                    case '\'':
                    case '\\':
                        i += 2;
                        break;
                    case 'u':
                        String sub = str.substring(i + 2, i + 6);
                        i += 6;
                        break;
                    default:
                        int x = 0;
                        while (x < 3) {
                            char e = str.charAt(i + 1 + x);
                            if (e >= '0' && e <= '7') {
                                x++;
                            } else {
                                break;
                            }
                        }
                        if (x == 0) {
                            throw new RuntimeException("can't pase string");
                        }
                        i += 1 + x;
                }

            } else {
                if (c == dEnd) {
                    return i;
                }
                i++;
            }
        }
        return end;
    }

    public static String unEscape0(String str, int start, int end) {

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; ) {
            char c = str.charAt(i);
            if (c == '\\') {
                char d = str.charAt(i + 1);
                switch (d) {
                    // ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
                    case 'b':
                        sb.append('\b');
                        i += 2;
                        break;
                    case 't':
                        sb.append('\t');
                        i += 2;
                        break;
                    case 'n':
                        sb.append('\n');
                        i += 2;
                        break;
                    case 'f':
                        sb.append('\f');
                        i += 2;
                        break;
                    case 'r':
                        sb.append('\r');
                        i += 2;
                        break;
                    case '\"':
                        sb.append('\"');
                        i += 2;
                        break;
                    case '\'':
                        sb.append('\'');
                        i += 2;
                        break;
                    case '\\':
                        sb.append('\\');
                        i += 2;
                        break;
                    case 'u':
                        String sub = str.substring(i + 2, i + 6);
                        sb.append((char) Integer.parseInt(sub, 16));
                        i += 6;
                        break;
                    default:
                        int x = 0;
                        while (x < 3) {
                            char e = str.charAt(i + 1 + x);
                            if (e >= '0' && e <= '7') {
                                x++;
                            } else {
                                break;
                            }
                        }
                        if (x == 0) {
                            throw new RuntimeException("can't pase string");
                        }
                        sb.append((char) Integer.parseInt(str.substring(i + 1, i + 1 + x), 8));
                        i += 1 + x;
                }

            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    public static class Ann {
        public String name;
        public List<Map.Entry<String, Object>> elements = new ArrayList();

        public void put(String name, Object value) {
            elements.add(new java.util.AbstractMap.SimpleEntry(name, value));
        }
    }

    public static Visibility getAnnVisibility(String name) {
        return Visibility.valueOf(name.toUpperCase());
    }

    public static int methodIns(Method m, boolean isStatic) {
        int a = isStatic ? 0 : 1;
        for (String t : m.getParameterTypes()) {
            switch (t.charAt(0)) {
                case 'J':
                case 'D':
                    a += 2;
                    break;
                default:
                    a += 1;
                    break;
            }
        }
        return a;
    }

    public static int reg2ParamIdx(Method m, int reg, int locals, boolean isStatic) {
        int x = reg - locals;
        if (x < 0) {
            return -1;
        }
        int a = isStatic ? 0 : 1;
        String[] parameterTypes = m.getParameterTypes();
        for (int i = 0, parameterTypesLength = parameterTypes.length; i < parameterTypesLength; i++) {

            if (x == a) {
                return i;
            }

            String t = parameterTypes[i];
            switch (t.charAt(0)) {
                case 'J':
                case 'D':
                    a += 2;
                    break;
                default:
                    a += 1;
                    break;
            }
        }
        return -1;
    }

    public static Method parseMethodAndUnescape(String owner, String part) throws RuntimeException {
        int x = part.indexOf('(');
        if (x < 0) {
            throw new RuntimeException();
        }
        int y = part.indexOf(')', x);
        if (y < 0) {
            throw new RuntimeException();
        }

        String methodName = unEscapeId(part.substring(0, x));
        String[] params = toTypeList(part.substring(x + 1, y));
        for (int i = 0; i < params.length; i++) {
            params[i] = unEscapeId(params[i]);
        }
        String ret = unEscapeId(part.substring(y + 1));
        return new Method(owner, methodName, params, ret);
    }

    public static Method parseMethodAndUnescape(String full) throws RuntimeException {

        int x = full.indexOf("->");
        if (x <= 0) {
            throw new RuntimeException();
        }
        return parseMethodAndUnescape(unEscapeId(full.substring(0, x)), full.substring(x + 2));
    }

    public static Field parseFieldAndUnescape(String owner, String part) throws RuntimeException {
        int x = part.indexOf(':');
        if (x < 0) {
            throw new RuntimeException();
        }
        return new Field(owner, unEscapeId(part.substring(0, x)), unEscapeId(part.substring(x + 1)));
    }

    public static Field parseFieldAndUnescape(String full) throws RuntimeException {
        int x = full.indexOf("->");
        if (x <= 0) {
            throw new RuntimeException();
        }
        return parseFieldAndUnescape(unEscapeId(full.substring(0, x)), full.substring(x + 2));
    }
}
