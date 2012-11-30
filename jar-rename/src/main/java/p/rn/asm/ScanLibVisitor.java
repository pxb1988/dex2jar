package p.rn.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import p.rn.ClassInfo;
import p.rn.ClassInfo.MemberInfo;

public class ScanLibVisitor extends EmptyVisitor {

    private ClassInfo clz;

    private Map<String, ClassInfo> map = new HashMap<String, ClassInfo>();

    public Map<String, ClassInfo> getClassMap() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        clz = new ClassInfo();
        clz.access = access;
        clz.name = name;

        if (superName != null) {
            clz.parent.add(superName);
        }
        if (interfaces != null) {
            clz.parent.addAll(Arrays.asList(interfaces));
        }
        map.put(name, clz);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        MemberInfo f = new MemberInfo();
        List<MemberInfo> fs = clz.members.get(name);
        if (fs == null) {
            fs = new ArrayList<MemberInfo>();
            clz.members.put(name, fs);
        }

        f.name = name;
        f.desc = desc;
        f.access = access;
        f.value = value;
        fs.add(f);
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("<init>".equals(name) || "<cinit>".equals(name))
            return null;
        MemberInfo m = new MemberInfo();
        int index = desc.lastIndexOf(')');
        String key = name + desc.substring(0, index + 1);
        List<MemberInfo> ms = clz.members.get(key);
        if (ms == null) {
            ms = new ArrayList<MemberInfo>();
            clz.members.put(key, ms);
        }

        m.name = name;
        m.desc = desc;
        m.access = access;
        ms.add(m);
        return null;
    }

}
