/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2015 Panxiaobo
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
package com.googlecode.d2j.tools.jar;

import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class BaseWeaver {
    protected String invocationInterfaceDesc = "Lcom/googlecode/d2j/tools/jar/MethodInvocation;";
    protected String invocationTypePrefix = "d2j/gen/MI_";

    protected static final String DEFAULT_RET_TYPE = "L888;";
    protected static final String DEFAULT_DESC = "(L;)" + DEFAULT_RET_TYPE;
    protected List<Callback> callbacks = new ArrayList<Callback>();
    protected int currentInvocationIdx = 0;
    protected int seqIndex = 1;
    protected MtdInfo key = new MtdInfo();
    protected Set<String> ignores = new HashSet<String>();
    protected Map<String, String> clzDescMap = new HashMap<String, String>();
    protected Map<MtdInfo, MtdInfo> mtdMap = new HashMap<MtdInfo, MtdInfo>();
    protected Map<MtdInfo, MtdInfo> defMap = new HashMap<MtdInfo, MtdInfo>();

    protected String buildMethodAName(String oldName) {
        return String.format("%s_A%03d", oldName, seqIndex++);
    }

    protected String buildCallbackMethodName(String oldName) {
        return String.format("%s_CB%03d", oldName, seqIndex++);
    }

    protected MtdInfo findDefinedTargetMethod(String owner, String name, String desc) {
        return findTargetMethod0(defMap, owner, name, desc);
    }

    protected MtdInfo findTargetMethod(String owner, String name, String desc) {
        return findTargetMethod0(mtdMap, owner, name, desc);
    }

    protected MtdInfo findTargetMethod0(Map<MtdInfo, MtdInfo> map, String owner, String name, String desc) {
        MtdInfo v = map.get(buildKey(owner, name, desc));
        if (v != null) {
            return v;
        }

        // try with default ret
        key.desc = Type.getMethodDescriptor(Type.getType(DEFAULT_RET_TYPE), Type.getArgumentTypes(desc));
        v = map.get(key);
        if (v != null) {
            return v;
        }
        // try with default desc
        key.desc = DEFAULT_DESC;
        v = map.get(key);
        if (v != null) {
            return v;
        }
        if (!name.equals("*")) {
            return findTargetMethod0(map, owner, "*", desc);
        }
        return v;
    }

    protected MtdInfo buildKey(String owner, String name, String desc) {
        key.name = name;
        key.owner = owner;
        key.desc = desc;
        return key;
    }

    public BaseWeaver withConfig(Path is) throws IOException {
        return withConfig(Files.readAllLines(is, StandardCharsets.UTF_8));
    }

    public BaseWeaver withConfig(InputStream is) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> list = new ArrayList<>();
            for (String ln = r.readLine(); ln != null; ln = r.readLine()) {
                list.add(ln);
            }
            return withConfig(list);
        }
    }

    public BaseWeaver withConfig(List<String> lines) {
        for (String ln : lines) {
            withConfig(ln);
        }
        return this;
    }

    public void withConfig(String ln) {
        if ("".equals(ln) || ln.startsWith("#")) {
            return;
        }
        switch (Character.toLowerCase(ln.charAt(0))) {
            case 'i':
                ignores.add(ln.substring(2));
                break;
            case 'c':
                int index = ln.lastIndexOf('=');
                if (index > 0) {
                    String key = toInternal(ln.substring(2, index));
                    String value = toInternal(ln.substring(index + 1));
                    clzDescMap.put(key, value);
                    ignores.add(value);
                }
                break;
            case 'r':
                index = ln.lastIndexOf('=');
                if (index > 0) {
                    String key = ln.substring(2, index);
                    String value = ln.substring(index + 1);
                    MtdInfo mi = buildMethodInfo(key);

                    index = value.indexOf('.');
                    MtdInfo mtdValue = new MtdInfo();
                    mtdValue.owner = value.substring(0, index);

                    int index2 = value.indexOf('(', index);
                    mtdValue.name = value.substring(index + 1, index2);
                    mtdValue.desc = value.substring(index2);

                    mtdMap.put(mi, mtdValue);

                }
                break;
            case 'd':
                index = ln.lastIndexOf('=');
                if (index > 0) {
                    String key = ln.substring(2, index);
                    String value = ln.substring(index + 1);
                    MtdInfo mi = buildMethodInfo(key);

                    index = value.indexOf('.');
                    MtdInfo mtdValue = new MtdInfo();
                    mtdValue.owner = value.substring(0, index);

                    int index2 = value.indexOf('(', index);
                    mtdValue.name = value.substring(index + 1, index2);
                    mtdValue.desc = value.substring(index2);

                    defMap.put(mi, mtdValue);
                }
                break;

            case 'o':
                setInvocationInterfaceDesc(ln.substring(2));
                break;
            case 'p':
                invocationTypePrefix = ln.substring(2);
                break;
        }
    }

    public void setInvocationInterfaceDesc(String invocationInterfaceDesc) {
        this.invocationInterfaceDesc = invocationInterfaceDesc;
    }

    protected static String toInternal(String key) {
        if (key.endsWith(";")) {
            key = key.substring(1, key.length() - 1);
        }
        return key;
    }

    protected MtdInfo buildMethodInfo(String value) {
        int index = value.indexOf('.');
        MtdInfo mtdValue = new MtdInfo();
        mtdValue.owner = value.substring(0, index);
        int index2 = value.indexOf('(', index);
        if (index2 >= 0) {
            mtdValue.name = value.substring(index + 1, index2);
            int index3 = value.indexOf(')');
            if (index3 == value.length() - 1) {
                mtdValue.desc = value.substring(index2) + DEFAULT_RET_TYPE;
            } else {
                mtdValue.desc = value.substring(index2);
            }
        } else {
            mtdValue.name = value.substring(index + 1);
            mtdValue.desc = DEFAULT_DESC;
        }
        return mtdValue;
    }

    // internal name
    public String getCurrentInvocationName() {
        return String.format("%s_%03d", invocationTypePrefix, currentInvocationIdx);
    }

    protected void nextInvocationName() {
        currentInvocationIdx++;
        callbacks.clear();
    }

    public static class Callback {
        int idx;
        Object callback;
        Object target;
        boolean isSpecial;
        boolean isStatic;
    }

    public static class MtdInfo {
        public String desc;
        public String name;
        public String owner;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            MtdInfo mtdInfo = (MtdInfo) o;

            if (!desc.equals(mtdInfo.desc))
                return false;
            if (!name.equals(mtdInfo.name))
                return false;
            if (!owner.equals(mtdInfo.owner))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = desc.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + owner.hashCode();
            return result;
        }
    }

}
