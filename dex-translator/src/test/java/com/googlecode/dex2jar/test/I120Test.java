package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.*;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.v3.V3;
import com.googlecode.dex2jar.v3.V3MethodAdapter;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

public class I120Test {

    public static void a(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitArguments(3, new int[] {});
        code.visitConstStmt(OP_CONST, 0, Integer.valueOf(0), TYPE_SINGLE); // int: 0xffffffff float:NaN
        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 0 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitConstStmt(OP_CONST, 1, Integer.valueOf(0), TYPE_SINGLE); // int: 0xffffffff float:NaN
        code.visitUnopStmt(OP_ARRAY_LENGTH, 2, 1, TYPE_INT);
        code.visitReturnStmt(OP_RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public void test() throws IllegalArgumentException, IllegalAccessException, AnalyzerException {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "Lt", null, "java/lang/Object", null);
        a(new EmptyVisitor() {
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
