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
package com.googlecode.dex2jar.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Util {
    public static List<String> listDesc(String desc) {
        List<String> list = new ArrayList<String>(5);
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
                i += count + 1;
                break;
            }
            case 'L': {
                int count = 1;
                while (chars[i + count] != ';') {
                    ++count;
                }
                count++;
                list.add(new String(chars, i, count));
                i += count + 1;
                break;
            }
            default:
            }
        }
        return list;
    }

    /**
     * Appends a quoted string to a given buffer.
     * 
     * @param buf
     *            the buffer where the string must be added.
     * @param s
     *            the string to be added.
     */
    public static void appendString(final StringBuffer buf, final String s) {
        buf.append('\"');
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\n') {
                buf.append("\\n");
            } else if (c == '\r') {
                buf.append("\\r");
            } else if (c == '\\') {
                buf.append("\\\\");
            } else if (c == '"') {
                buf.append("\\\"");
            } else if (c < 0x20 || c > 0x7f) {
                buf.append("\\u");
                if (c < 0x10) {
                    buf.append("000");
                } else if (c < 0x100) {
                    buf.append("00");
                } else if (c < 0x1000) {
                    buf.append('0');
                }
                buf.append(Integer.toString(c, 16));
            } else {
                buf.append(c);
            }
        }
        buf.append('\"');
    }

    public static String toShortClassName(String desc) {
        switch (desc.charAt(0)) {
        case 'Z':
            return "boolean";
        case 'B':
            return "byte";
        case 'C':
            return "char";
        case 'S':
            return "short";
        case 'I':
            return "int";
        case 'J':
            return "long";
        case 'F':
            return "float";
        case 'D':
            return "double";
        case 'V':
            return "void";
        case 'L': {
            int i = desc.lastIndexOf('/');
            return desc.substring(i < 0 ? 1 : i + 1, desc.length() - 1);
        }
        case '[':
            int d = 1;
            for (; d < desc.length(); d++) {
                if (desc.charAt(d) != '[') {
                    break;
                }
            }
            StringBuilder sb = new StringBuilder().append(toShortClassName(desc.substring(d)));
            for (int t = 0; t < d; t++) {
                sb.append("[]");
            }
            return sb.toString();
        }
        throw new UnsupportedOperationException();
    }
}
