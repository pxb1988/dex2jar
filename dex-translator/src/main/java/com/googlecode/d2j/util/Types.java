package com.googlecode.d2j.util;

import com.googlecode.d2j.DexException;

import java.util.ArrayList;
import java.util.List;

public class Types {
    /**
     * @param desc
     *            a asm method desc, ex: (II)V
     * @return a array of argument types, ex: [I,I]
     */
    public static String[] getParameterTypeDesc(String desc) {

        if (desc.charAt(0) != '(') {
            throw new DexException("not a validate Method Desc %s", desc);
        }
        int x = desc.lastIndexOf(')');
        if (x < 0) {
            throw new DexException("not a validate Method Desc %s", desc);
        }
        List<String> ps = listDesc(desc.substring(1, x - 1));
        return ps.toArray(new String[ps.size()]);
    }

    /**
     * 
     * @param desc
     *            a asm method desc, ex: (II)V
     * @return the desc of return type, ex: V
     */
    public static String getReturnTypeDesc(String desc) {
        int x = desc.lastIndexOf(')');
        if (x < 0) {
            throw new DexException("not a validate Method Desc %s", desc);
        }
        return desc.substring(x + 1);
    }

    public static List<String> listDesc(String desc) {
        List<String> list = new ArrayList<>(5);
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

    public static Object[] buildDexStyleSignature(String signature) {
        int rawLength = signature.length();
        ArrayList<String> pieces = new ArrayList<String>(20);

        for (int at = 0; at < rawLength; /* at */) {
            char c = signature.charAt(at);
            int endAt = at + 1;
            if (c == 'L') {
                // Scan to ';' or '<'. Consume ';' but not '<'.
                while (endAt < rawLength) {
                    c = signature.charAt(endAt);
                    if (c == ';') {
                        endAt++;
                        break;
                    } else if (c == '<') {
                        break;
                    }
                    endAt++;
                }
            } else {
                // Scan to 'L' without consuming it.
                while (endAt < rawLength) {
                    c = signature.charAt(endAt);
                    if (c == 'L') {
                        break;
                    }
                    endAt++;
                }
            }

            pieces.add(signature.substring(at, endAt));
            at = endAt;
        }
        return pieces.toArray(new Object[pieces.size()]);
    }

}
