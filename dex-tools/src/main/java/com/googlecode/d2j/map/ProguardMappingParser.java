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
package com.googlecode.d2j.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class ProguardMappingParser {
    private String j2d(String type) {
        StringBuilder sb = new StringBuilder();
        while (type.endsWith("[]")) {
            sb.append("[");
            type = type.substring(0, type.length() - 2);
        }
        switch (type) {
            case "boolean":
                sb.append("Z");
                break;
            case "byte":
                sb.append("B");
                break;
            case "short":
                sb.append("S");
                break;
            case "char":
                sb.append("C");
                break;
            case "int":
                sb.append("I");
                break;
            case "float":
                sb.append("F");
                break;
            case "long":
                sb.append("J");
                break;
            case "double":
                sb.append("D");
                break;
            case "void":
                sb.append("V");
                break;
            default:
                sb.append("L").append(type.replace('.', '/')).append(";");
                break;
        }
        return sb.toString();
    }

    public void parse(Reader in, InheritanceTree tree) throws IOException {
        BufferedReader r = new BufferedReader(in);
        String currentClz = null;
        for (String ln = r.readLine(); ln != null; ln = r.readLine()) {
            if (ln.startsWith(" ")) { // member mapping
                // java.lang.String data -> a
                // boolean equals(java.lang.Object) -> equals
                ln = ln.trim();
                String as[] = ln.split(" ");
                String fieldDescOrMethodRet = j2d(as[0]);
                String fieldNameOrMethodNameDesc = as[1];
                String newName = as[3];

                String key;
                if (fieldNameOrMethodNameDesc.contains("(")) { // a method

                    int idx = fieldNameOrMethodNameDesc.indexOf('(');
                    String mName = fieldNameOrMethodNameDesc.substring(0, idx);
                    String args = fieldNameOrMethodNameDesc.substring(idx + 1,
                            fieldNameOrMethodNameDesc.length() - 1);
                    String[] ps;
                    if (args.length() != 0) {
                        ps = args.split(",");
                        for (int i = 0; i < ps.length; i++) {
                            ps[i] = j2d(ps[i]);
                        }
                    } else {
                        ps = new String[0];
                    }
                    tree.recordMethodRenameTo(currentClz, mName, ps, fieldDescOrMethodRet, newName);
                } else {
                    tree.recordFieldRenameTo(currentClz, fieldNameOrMethodNameDesc, fieldDescOrMethodRet, newName);
                }

            } else { // clz mapping
                // clz -> n:
                String as[] = ln.split(" ");
                currentClz = j2d(as[0]);
                String newName = as[2];
                if (newName.endsWith(":")) {
                    newName = newName.substring(0, newName.length() - 1);
                }
                tree.recordClassRenameTo(currentClz, j2d(newName));
            }
        }
    }
}
