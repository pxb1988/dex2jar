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

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.util.Mapper;
import com.googlecode.dex2jar.ir.ts.UniqueQueue;

import java.util.*;

public class InheritanceTree implements Mapper {
    Map<String, Clz> clzMap = new HashMap<>();
    String from;
    boolean isLibrary;

    public InheritanceTree() {
    }

    public static void main(String... args) {
        InheritanceTree tree = new InheritanceTree();
        Clz a = tree.addClz(0, "La;");
        a.addMethod(0, "abc", new String[0], "V");
        Clz b = tree.addClz(0, "Lb;");
        b.addMethod(0, "abc", new String[0], "V");
        Clz c = tree.addClz(0, "Lc;");
        c.relateSuper("Ljava/lang/Object;");
        c.relateInterface("La;");
        c.relateInterface("Lb;");
        tree.link();
    }

    private static boolean isPrivate(int accessFlags) {
        return ((DexConstants.ACC_PRIVATE) & accessFlags) != 0;
    }

    private static boolean isStaticOrPrivate(int accessFlags) {
        return ((DexConstants.ACC_PRIVATE | DexConstants.ACC_STATIC) & accessFlags) != 0;
    }

    private static boolean isStaticOrPrivateOrFinal(int accessFlags) {
        return ((DexConstants.ACC_PRIVATE | DexConstants.ACC_STATIC | DexConstants.ACC_FINAL) & accessFlags) != 0;
    }

    public void updateFrom(String from, boolean isLibrary) {
        this.from = from;
        this.isLibrary = isLibrary;
    }

    public String mapClassName(String name) {
        Clz clz = clzMap.get(name);
        if (clz == null) {
            return null;
        }
        return clz.name.newValue;
    }

    public void recordClassRenameTo(String old, String newName) {
        Clz clz = clzMap.get(old);
        if (clz == null) {
            WARN("WARN: cant find class %s", old);
            return;
        }
        if (clz.name.noRename) {
            WARN("WARN: cant rename class %s");
            return;
        }
        clz.name.newValue = newName;
    }

    public void recordMethodRenameTo(String owner, String oldName, String[] args, String ret, String newName) {
        Clz clz = clzMap.get(owner);
        if (clz == null) {
            WARN("WARN: cant find class %s", owner);
            return;
        }
        String key = toMethodKey(oldName, args, ret);
        Mtd mtd = clz.methods.get(key);
        if (mtd == null) {
            WARN("WARN: cant find method %s->%s", owner, key);
            return;
        }
        if (mtd.name.noRename && !oldName.equals(newName)) {
            WARN("WARN: cant rename method %s->%s to %s", owner, key, newName);
            return;
        }
        if (mtd.name.newValue == null) {
            mtd.name.newValue = newName;
        } else if (!newName.equals(mtd.name.newValue)) {
            WARN("WARN: cant rename method %s->%s to %s, pre rename to %s", owner, key, newName, mtd.name.newValue);
        }
    }

    public void recordFieldRenameTo(String owner, String oldName, String type, String newName) {
        Clz clz = clzMap.get(owner);
        if (clz == null) {
            WARN("WARN: cant find class %s", owner);
            return;
        }
        String key = toFieldKey(oldName, type);
        Fld fld = clz.fields.get(key);
        if (fld == null) {
            WARN("WARN: cant find field %s->%s", owner, key);
            return;
        }
        if (fld.name.noRename && !oldName.equals(newName)) {
            WARN("WARN: cant rename field %s->%s to %s", owner, key, newName);
            return;
        }
        if (fld.name.newValue == null) {
            fld.name.newValue = newName;
        } else if (!newName.equals(fld.name.newValue)) {
            WARN("WARN: cant rename field %s->%s to %s, pre rename to %s", owner, key, newName, fld.name.newValue);
        }
    }

    public String mapFieldName(String owner, String name, String type) {
        Clz clz = clzMap.get(owner);
        if (clz == null) {
            return null;
        }
        Fld fld = clz.fields.get(toFieldKey(name, type));
        if (fld == null) {
            return null;
        }
        return fld.name.newValue;
    }

    public String mapFieldOwner(String owner, String name, String type) {
        Clz clz = clzMap.get(owner);
        if (clz == null) {
            return null;
        }
        Fld fld = clz.fields.get(toFieldKey(name, type));
        if (fld == null) {
            return clz.name.newValue;
        }
        Name fldName = fld.owner.name;
        if (fldName.newValue == null) {
            return fldName.oldValue;
        } else {
            return fldName.newValue;
        }
    }

    public String mapMethodName(String owner, String name, String[] args, String ret) {
        Clz clz = clzMap.get(owner);
        if (clz == null) {
            return null;
        }
        Mtd mtd = null;
        if (args == null || ret == null) {
            for (Mtd m : clz.methods.values()) {
                if (m.args.length == 0 && m.name.oldValue.equals(name)) {
                    mtd = m;
                    break;
                }
            }
        } else {
            mtd = clz.methods.get(toMethodKey(name, args, ret));
        }
        if (mtd == null) {
            return null;
        }
        return mtd.name.newValue;
    }

    public String mapMethodOwner(String owner, String name, String[] args, String ret) {
        Clz clz = clzMap.get(owner);
        if (clz == null) {
            return null;
        }
        Mtd mtd = null;
        if (args == null || ret == null) {
            for (Mtd m : clz.methods.values()) {
                if (m.args.length == 0 && m.name.oldValue.equals(name)) {
                    mtd = m;
                    break;
                }
            }
        } else {
            mtd = clz.methods.get(toMethodKey(name, args, ret));
        }
        if (mtd == null) {
            return clz.name.newValue;
        }
        Name mtdName = mtd.owner.name;
        if (mtdName.newValue == null) {
            return mtdName.oldValue;
        } else {
            return mtdName.newValue;
        }
    }

    /**
     * @param accessFlags
     *            the access flags of the class
     * @param name
     *            the descriptor of the class
     * @return return null if the class is redefined
     */
    Clz addClz(int accessFlags, String name) {
        Clz clz = getOrCreateClz(name);
        if (clz.stat != Stat.UNKNOWN) {
            if (clz.stat == Stat.LIBRARY && !isLibrary) {
                WARN("app class %s redefined, org %s, new %s, skiping.", name, clz.from, from);
                return null;
            } else {
                WARN("class %s is defined in %s, skip redefine in %s", name, clz.from, from);
                return null;
            }
        }

        clz.stat = isLibrary ? Stat.LIBRARY : Stat.APP;
        clz.accessFlags = accessFlags;
        clz.from = from;
        return clz;
    }

    public void link() {
        Queue<Clz> q = new UniqueQueue<>();
        q.addAll(clzMap.values());

        while (!q.isEmpty()) {
            Clz clz = q.poll();
            for (Map.Entry<String, Mtd> e : clz.methods.entrySet()) {
                String key = e.getKey();
                Mtd mtd = e.getValue();
                Name name = mtd.name.trim();
                mtd.name = name;
                if (name.oldValue.startsWith("<")) {
                    // constructor, skip
                    continue;
                }
                if (isPrivate(mtd.accessFlags)) {
                    continue;
                }
                if (clz.children.size() > 0) {
                    for (Clz child : clz.children) {
                        Mtd childMtd = child.methods.get(key);
                        if (childMtd != null) {
                            if (isStaticOrPrivateOrFinal(childMtd.accessFlags)) {
                                continue;
                            }
                            childMtd.name = merge(name, childMtd.name);
                        } else {
                            child.methods.put(key, mtd);
                            q.add(child);
                        }
                    }
                }
                if (clz.impls.size() > 0) {
                    for (Clz child : clz.impls) {
                        Mtd childMtd = child.methods.get(key);
                        if (childMtd != null) {
                            childMtd.name = merge(name, childMtd.name);
                        } else {
                            child.methods.put(key, mtd);
                            q.add(child);
                        }
                    }
                }
            }
        }

        q.addAll(clzMap.values());

        while (!q.isEmpty()) {
            Clz clz = q.poll();
            if (clz.fields.size() > 0) {
                for (Map.Entry<String, Fld> e : clz.fields.entrySet()) {
                    String key = e.getKey();
                    Fld fld = e.getValue();
                    if (isPrivate(fld.accessFlags)) { // also copy static method to sub
                        continue;
                    }
                    if (clz.children.size() > 0) {
                        for (Clz child : clz.children) {
                            if (!child.fields.containsKey(key)) {
                                child.fields.put(key, fld);
                                q.add(child);
                            }
                        }
                    }
                }
            }
        }

        for (Clz clz : clzMap.values()) {
            if (clz.stat == Stat.UNKNOWN) {
                WARN("clz %s is unknow", clz.name);
            }

            boolean noRename = clz.stat == Stat.UNKNOWN || clz.stat == Stat.LIBRARY;
            clz.name.noRename = noRename;
            if (clz.methods.size() > 0) {
                for (Mtd mtd : clz.methods.values()) {
                    Name name = mtd.name.trim();
                    mtd.name = name;
                    if (noRename) {
                        name.noRename = true;
                    }
                }
            }
            if (noRename && clz.fields.size() > 0) {
                for (Fld fld : clz.fields.values()) {
                    fld.name.noRename = true;
                }
            }

            // relationship is useless now
            clz.children = null;
            clz.impls = null;
            clz.superClz = null;
            clz.interfaces = null;
        }
    }

    private Name merge(Name name, Name childMtd) {
        childMtd = childMtd.trim();
        if (childMtd != name) {
            childMtd.next = name;
        }
        return name;
    }

    private boolean isPrivateOrFinal(int accessFlags) {
        return ((DexConstants.ACC_PRIVATE | DexConstants.ACC_FINAL) & accessFlags) != 0;
    }

    private void WARN(String s, Object... args) {
        System.err.println(String.format(s, args));
    }

    Clz getOrCreateClz(String name) {
        Clz clz = clzMap.get(name);
        if (clz == null) {
            clz = new Clz(name);
            clzMap.put(name, clz);
        }
        return clz;
    }

    public static String toFieldKey(String name, String type) {
        return name + ":" + type;
    }

    public static String toMethodKey(String name, String args[], String ret) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (String arg : args) {
            sb.append(arg);
        }
        sb.append(")").append(ret);
        return sb.toString();
    }

    public enum Stat {
        UNKNOWN, LIBRARY, APP
    }

    public static class Name {
        final String oldValue;
        boolean noRename = false;
        String newValue;
        Name next;

        public Name(String name) {
            this.oldValue = name;
        }

        public String toString() {
            return oldValue;
        }

        public Name trim() {
            Name n = this;
            while (n.next != null) {
                n = n.next;
            }
            return n;
        }

    }

    public static class Fld {
        public int accessFlags;
        public Name name;
        public Clz owner;
        public String type;
    }

    public static class Mtd {
        public int accessFlags;
        public Name name;
        public Clz owner;
        public String ret;
        public String[] args;
    }

    public class Clz {
        public final Name name;
        public Stat stat = Stat.UNKNOWN;
        public String from;
        public int accessFlags;
        public Clz superClz;
        public Set<Clz> interfaces = new HashSet<>();
        public Set<Clz> children = new HashSet<>();
        public Set<Clz> impls = new HashSet<>();
        public Map<String, Mtd> methods = new HashMap<>();
        public Map<String, Fld> fields = new HashMap<>();

        Clz(String name) {
            this.name = new Name(name);
        }

        public String toString() {
            return name.toString();
        }

        public void relateSuper(String name) {
            Clz s = getOrCreateClz(name);
            superClz = s;
            s.children.add(this);
        }

        public void relateInterface(String itf) {
            Clz s = getOrCreateClz(itf);
            interfaces.add(s);
            s.impls.add(this);
        }

        public void addMethod(int accessFlags, String name, String args[], String ret) {
            String key = toMethodKey(name, args, ret);
            if (methods.containsKey(key)) {
                WARN("DUP method: %s in class %s, skiping.", key, this.name);
                return;
            }
            Mtd mtd = new Mtd();
            mtd.owner = this;
            mtd.accessFlags = accessFlags;
            mtd.name = new Name(name);
            mtd.ret = ret;
            mtd.args = args;
            methods.put(key, mtd);
        }

        public void addField(int accessFlags, String name, String type) {
            String key = toFieldKey(name, type);
            if (fields.containsKey(key)) {
                WARN("DUP field: %s in class %s, skiping.", key, this.name);
                return;
            }
            Fld fld = new Fld();
            fld.owner = this;
            fld.name = new Name(name);
            fld.accessFlags = accessFlags;
            fld.type = type;
            fields.put(key, fld);
        }

    }
}
