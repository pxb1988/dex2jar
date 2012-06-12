package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.ACC_PUBLIC;
import static com.googlecode.dex2jar.DexOpcodes.ACC_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_ARRAY_LENGTH;
import static com.googlecode.dex2jar.DexOpcodes.OP_CONST;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_VIRTUAL;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN_VOID;
import static com.googlecode.dex2jar.DexOpcodes.TYPE_INT;
import static com.googlecode.dex2jar.DexOpcodes.TYPE_SINGLE;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.v3.V3;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class I120Test {

    public static void a(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitArguments(3, new int[] {});
        code.visitConstStmt(OP_CONST, 0, Integer.valueOf(0), TYPE_SINGLE);
        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 0 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitConstStmt(OP_CONST, 1, Integer.valueOf(0), TYPE_SINGLE);
        code.visitUnopStmt(OP_ARRAY_LENGTH, 2, 1, TYPE_INT);
        code.visitReturnStmt(OP_RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public void test() throws IllegalArgumentException, IllegalAccessException, AnalyzerException {
        TestDexClassV cv = new TestDexClassV("Lt", V3.OPTIMIZE_SYNCHRONIZED | V3.TOPOLOGICAL_SORT);
        a(cv);
        ClassReader cr = new ClassReader(cv.toByteArray());
        TestUtils.verify(cr);
    }
}
