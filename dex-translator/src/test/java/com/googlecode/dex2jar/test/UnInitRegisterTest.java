package com.googlecode.dex2jar.test;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import org.junit.Test;

import static com.googlecode.dex2jar.DexOpcodes.*;

public class UnInitRegisterTest {
    public static void i219(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "a",
                new String[] { "Ljava/lang/String;" }, "I"));
        DexCodeVisitor code = mv.visitCode();

        int v0 = 0;
        int v1 = 1;

        code.visitArguments(2, new int[] { v1 });
        code.visitReturnStmt(OP_RETURN, v0, TYPE_INT);
        code.visitEnd();
        mv.visitEnd();
    }

    public static void i220(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC, new Method("La;", "a", new String[] {}, "Ljava/lang/String;"));
        DexCodeVisitor code = mv.visitCode();

        int v0 = 0;
        int v1 = 1;
        int v2 = 2;
        int v6 = 6;

        DexLabel L6 = new DexLabel();
        code.visitArguments(7, new int[] { v6 });
        code.visitJumpStmt(OP_IF_EQZ, v1, L6);
        code.visitMoveStmt(OP_MOVE, v0, v6);
        code.visitLabel(L6);
        code.visitReturnStmt(OP_RETURN, v2, TYPE_OBJECT);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public void test() throws Exception {
        TestUtils.testDexASMifier(getClass(), "i219");
        TestUtils.testDexASMifier(getClass(), "i220");
    }
}
