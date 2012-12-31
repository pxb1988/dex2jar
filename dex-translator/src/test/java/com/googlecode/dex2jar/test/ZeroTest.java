package com.googlecode.dex2jar.test;

import org.junit.Test;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.OdexOpcodes;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class ZeroTest implements OdexOpcodes {
    public static void z(DexClassVisitor cv) {
        DexMethodVisitor mv = cv
                .visitMethod(ACC_STATIC, new Method("La;", "b", new String[] {}, "[Ljava/lang/Object;"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                int v0 = 0;
                code.visitArguments(1, new int[] {});
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                DexLabel L3 = new DexLabel();
                code.visitConstStmt(OP_CONST, v0, 0, TYPE_SINGLE);
                code.visitJumpStmt(OP_IF_EQ, v0, v0, L2);
                code.visitLabel(L1);
                code.visitReturnStmt(OP_RETURN, v0, TYPE_OBJECT);
                code.visitLabel(L2);
                code.visitJumpStmt(OP_IF_EQ, v0, v0, L3);
                code.visitMethodStmt(OP_INVOKE_STATIC, new int[0], new Method("La;", "getBytes", new String[0], "[B"));
                code.visitMoveStmt(OP_MOVE_RESULT, v0, TYPE_SINGLE);
                code.visitLabel(L3);
                code.visitMethodStmt(OP_INVOKE_STATIC, new int[] { v0 }, new Method("La;", "useBytes",
                        new String[] { "[B" }, "V"));
                code.visitMethodStmt(OP_INVOKE_STATIC, new int[0], new Method("La;", "getObjects", new String[0],
                        "[Ljava/lang/Object;"));
                code.visitMoveStmt(OP_MOVE_RESULT, v0, TYPE_SINGLE);
                code.visitJumpStmt(OP_GOTO, L1);
                code.visitEnd();
            }
            mv.visitEnd();
        }
    }

    @Test
    public void test() throws Exception {
        TestUtils.testDexASMifier(getClass(), "z");
    }

}
