package com.googlecode.d2j.tools.jar;

import com.googlecode.d2j.tools.jar.ClassInfo.MemberInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.d2j.util.AccUtils.isBridge;
import static com.googlecode.d2j.util.AccUtils.isSynthetic;

public class ScanBridgeAdapter extends ClassVisitor implements Opcodes {

    private Map<String, MemberInfo> bridge = new HashMap<String, MemberInfo>();

    public ScanBridgeAdapter(ClassVisitor cv) {
        super(ASM4, cv);
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
                mv = new MethodVisitor(ASM4) {
                };
            }
            final MemberInfo member = new MemberInfo();
            member.access = access;
            member.desc = desc;
            member.name = name;
            mv = new MethodVisitor(ASM4, mv) {

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
