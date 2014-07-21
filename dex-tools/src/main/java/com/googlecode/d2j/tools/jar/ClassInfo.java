package com.googlecode.d2j.tools.jar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassInfo {

    final public String name;
    public List<MemberInfo> members = new ArrayList<MemberInfo>(5);
    public Set<String> parent = new HashSet<String>();

    public ClassInfo(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        return name.equals(((ClassInfo) o).name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public static class MemberInfo {
        public int access;
        public String desc;
        public String name;
    }
}
