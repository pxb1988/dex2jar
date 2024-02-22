package com.googlecode.d2j.tools.jar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClassInfo {

    public final String name;

    public List<MemberInfo> members = new ArrayList<>(5);

    public Set<String> parent = new HashSet<>();

    public ClassInfo(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassInfo)) {
            return false;
        }
        ClassInfo classInfo = (ClassInfo) o;
        return Objects.equals(name, classInfo.name);
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
