package com.googlecode.dex2jar.test;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.jupiter.api.Disabled;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

@Disabled
public class TestDexClassV extends DexClassVisitor {

    private final int config;

    private final ClassWriter cw;

    public TestDexClassV(String clz, int config) {
        super();
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, clz, null, "java/lang/Object", null);
        this.config = config;
    }

    public byte[] toByteArray() {
        cw.visitEnd();
        return cw.toByteArray();
    }

    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        return new DexMethodNode(accessFlags, method) {
            @Override
            public void visitEnd() {
                super.visitEnd();
                // FIXME impl
                //methodNode.accept(cw);
            }
        };
    }

}
