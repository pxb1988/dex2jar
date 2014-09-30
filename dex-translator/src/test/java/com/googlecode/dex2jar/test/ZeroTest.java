package com.googlecode.dex2jar.test;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.googlecode.d2j.reader.Op.*;

@RunWith(DexTranslatorRunner.class)
public class ZeroTest implements DexConstants {
    @Test
    public static void testZero(DexClassVisitor cv) {
        DexMethodVisitor mv = cv
                .visitMethod(ACC_STATIC, new Method("La;", "b", new String[] {}, "[Ljava/lang/Object;"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                int v0 = 0;
                code.visitRegister(1);
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                DexLabel L3 = new DexLabel();
                code.visitConstStmt(CONST, v0, 0);
                code.visitJumpStmt(IF_EQ, v0, v0, L2);
                code.visitLabel(L1);
                code.visitStmt1R(RETURN_OBJECT, v0);
                code.visitLabel(L2);
                code.visitJumpStmt(IF_EQ, v0, v0, L3);
                code.visitMethodStmt(INVOKE_STATIC, new int[0], new Method("La;", "getBytes", new String[0], "[B"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, v0);
                code.visitLabel(L3);
                code.visitMethodStmt(INVOKE_STATIC, new int[] { v0 }, new Method("La;", "useBytes",
                        new String[] { "[B" }, "V"));
                code.visitMethodStmt(INVOKE_STATIC, new int[0], new Method("La;", "getObjects", new String[0],
                        "[Ljava/lang/Object;"));
                code.visitStmt1R(MOVE_RESULT, v0);
                code.visitJumpStmt(GOTO, -1, -1, L1);
                code.visitEnd();
            }
            mv.visitEnd();
        }
    }
}
