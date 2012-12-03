package p.rn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassInfo {

    public static class MemberInfo {
        public int access;
        public String desc;
        public String name;
        public Object value;
    }

    public int access;

    public Map<String, List<MemberInfo>> members = new HashMap<String, List<MemberInfo>>();

    public String name;

    public Set<String> parent = new HashSet<String>();

    public boolean equals(Object o) {
        return name.equals(((ClassInfo) o).name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }
}
