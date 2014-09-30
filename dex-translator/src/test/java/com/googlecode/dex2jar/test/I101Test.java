package com.googlecode.dex2jar.test;

import static com.googlecode.d2j.DexConstants.ACC_PUBLIC;
import static com.googlecode.d2j.DexConstants.ACC_STATIC;
import static com.googlecode.d2j.reader.Op.CONST_STRING;
import static com.googlecode.d2j.reader.Op.INVOKE_VIRTUAL;
import static com.googlecode.d2j.reader.Op.MOVE_EXCEPTION;
import static com.googlecode.d2j.reader.Op.RETURN_VOID;

import org.junit.Test;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

public class I101Test {

    public static void a(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitRegister(2);
        DexLabel L0 = new DexLabel();
        DexLabel L1 = new DexLabel();
        DexLabel L2 = new DexLabel();
        code.visitTryCatch(L0, L1, new DexLabel[] { L2 }, new String[] { "Lsome/Exception;" });

        code.visitLabel(L0);
        code.visitConstStmt(CONST_STRING, 0, "abc");
        code.visitLabel(L1);

        code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 0 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitStmt0R(RETURN_VOID);
        code.visitLabel(L2);
        code.visitStmt1R(MOVE_EXCEPTION, 1);
        code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitStmt0R(RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public void test() throws Exception {
        byte[] data = TestUtils.testDexASMifier(getClass(), "a", "Lt");
        TestUtils.defineClass("Lt", data);
    }
}
