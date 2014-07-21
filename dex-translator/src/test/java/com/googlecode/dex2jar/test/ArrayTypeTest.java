package com.googlecode.dex2jar.test;

import static com.googlecode.d2j.DexConstants.ACC_PUBLIC;
import static com.googlecode.d2j.DexConstants.ACC_STATIC;
import static com.googlecode.d2j.reader.Op.AGET;
import static com.googlecode.d2j.reader.Op.APUT;
import static com.googlecode.d2j.reader.Op.ARRAY_LENGTH;
import static com.googlecode.d2j.reader.Op.CONST;
import static com.googlecode.d2j.reader.Op.GOTO;
import static com.googlecode.d2j.reader.Op.INVOKE_VIRTUAL;
import static com.googlecode.d2j.reader.Op.NEW_ARRAY;
import static com.googlecode.d2j.reader.Op.RETURN_VOID;

import org.junit.Test;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.runner.RunWith;

@RunWith(DexTranslatorRunner.class)
public class ArrayTypeTest {

    @Test
    public static void a120(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitRegister(3);
        code.visitConstStmt(CONST, 0, Integer.valueOf(0));
        code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 0 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitConstStmt(CONST, 1, Integer.valueOf(0));
        code.visitStmt2R(ARRAY_LENGTH, 2, 1);
        code.visitStmt0R(RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public static void a122(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitRegister(3);
        code.visitConstStmt(CONST, 0, Integer.valueOf(0));
        code.visitConstStmt(CONST, 2, Integer.valueOf(1));
        code.visitStmt3R(AGET, 1, 0, 2);
        code.visitStmt0R(RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public static void a123(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitRegister(3);
        code.visitConstStmt(CONST, 0, 0);
        code.visitConstStmt(CONST, 1, 1);
        code.visitConstStmt(CONST, 2, 0x63);
        code.visitStmt3R(APUT, 2, 0, 1);
        code.visitStmt0R(RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public static void merge1(DexClassVisitor cv) {// obj = array
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        DexLabel L0 = new DexLabel();
        DexLabel L1 = new DexLabel();
        code.visitRegister(3);
        code.visitConstStmt(CONST, 0, 0);
        code.visitJumpStmt(GOTO, -1, -1, L1);
        code.visitLabel(L0);
        code.visitStmt2R(ARRAY_LENGTH, 1, 0);
        code.visitConstStmt(CONST, 1, 0);
        code.visitStmt3R(AGET, 2, 0, 1);
        code.visitStmt0R(RETURN_VOID);
        code.visitLabel(L1);
        code.visitConstStmt(CONST, 1, 1);
        code.visitTypeStmt(NEW_ARRAY, 0, 1, "[Ljava/security/cert/X509Certificate;");
        code.visitJumpStmt(GOTO, -1, -1, L0);
        code.visitEnd();
        mv.visitEnd();
    }
}
