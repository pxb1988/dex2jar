package com.googlecode.dex2jar.test;

import org.junit.Ignore;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.v3.V3MethodAdapter;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

@Ignore
public class TestDexClassV extends EmptyVisitor {
    private int config;
    private ClassWriter cw;

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
        return new V3MethodAdapter(accessFlags, method, null, config) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                methodNode.accept(cw);
            }
        };
    }
}
