package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.ACC_PUBLIC;
import static com.googlecode.dex2jar.DexOpcodes.ACC_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_CONST_STRING;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_VIRTUAL;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_EXCEPTION;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN_VOID;

import org.junit.Test;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class I101Test {

    public static void a(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitArguments(2, new int[] {});
        DexLabel L0 = new DexLabel();
        DexLabel L1 = new DexLabel();
        DexLabel L2 = new DexLabel();
        code.visitTryCatch(L0, L1, new DexLabel[] { L2 }, new String[] { "Lsome/Exception;" });

        code.visitLabel(L0);
        code.visitConstStmt(OP_CONST_STRING, 0, "abc", 2);
        code.visitLabel(L1);

        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 0 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitReturnStmt(OP_RETURN_VOID);
        code.visitLabel(L2);
        code.visitMoveStmt(OP_MOVE_EXCEPTION, 1, 2);
        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitReturnStmt(OP_RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public void test() throws Exception {
        byte[] data = TestUtils.testDexASMifier(getClass(), "a", "Lt");
        TestUtils.defineClass("Lt", data);
    }
}
