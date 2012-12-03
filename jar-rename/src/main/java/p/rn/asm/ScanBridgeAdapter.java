package p.rn.asm;

import static p.rn.util.AccUtils.isBridge;
import static p.rn.util.AccUtils.isSynthetic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import p.rn.ClassInfo.MemberInfo;

public class ScanBridgeAdapter extends ClassAdapter {

    private Map<String, MemberInfo> bridge = new HashMap<String, MemberInfo>();

    public ScanBridgeAdapter(ClassVisitor cv) {
        super(cv);
    }

    // private String currentName;
    //
    // @Override
    // public void visit(int version, int access, String name, String signature,
    // String superName, String[] interfaces) {
    // super.visit(version, access, name, signature, superName, interfaces);
    // this.currentName = name;
    // }

    public Map<String, MemberInfo> getBridge() {
        return Collections.unmodifiableMap(bridge);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (isBridge(access) && isSynthetic(access)) {
            if (mv == null) {
                mv = new EmptyVisitor();
            }
            final MemberInfo member = new MemberInfo();
            member.access = access;
            member.desc = desc;
            member.name = name;
            mv = new MethodAdapter(mv) {

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc) {
                    super.visitMethodInsn(opcode, owner, name, desc);
                    if (!name.equals(member.name)) {
                        bridge.put(owner + '.' + name + desc.substring(0, desc.lastIndexOf(')') + 1), member);
                    }
                }
            };
        }
        return mv;
    }

}
