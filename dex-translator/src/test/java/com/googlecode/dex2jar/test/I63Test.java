package com.googlecode.dex2jar.test;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.OdexOpcodes;
import com.googlecode.dex2jar.v3.V3;
import com.googlecode.dex2jar.v3.V3MethodAdapter;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * test case for issue 63
 */
public class I63Test implements OdexOpcodes {

    public static void i63(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitArguments(1, new int[] {});
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                code.visitLabel(L1);
                code.visitFieldStmt(OP_SGET, 0, new Field("La;", "f", "J"), TYPE_WIDE);
                code.visitLabel(L2);
                code.visitReturnStmt(OP_RETURN_VOID);
                code.visitEnd();
                code.visitTryCatch(L1, L2, L2, "La;");
            }
            mv.visitEnd();
        }
    }

    @Test
    public void test() throws IllegalArgumentException, AnalyzerException, IllegalAccessException {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "La", null, "java/lang/Object", null);
        i63(new EmptyVisitor() {
            @Override
            public DexMethodVisitor visitMethod(int accessFlags, Method method) {
                return new V3MethodAdapter(accessFlags, method, null, V3.OPTIMIZE_SYNCHRONIZED | V3.TOPOLOGICAL_SORT) {

                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        methodNode.accept(cw);
                    }
                };
            }
        });
        ClassReader cr = new ClassReader(cw.toByteArray());
        TestUtils.verify(cr);
    }

}
