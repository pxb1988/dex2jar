package com.googlecode.dex2jar.test;

import static com.googlecode.d2j.DexConstants.ACC_PUBLIC;
import static com.googlecode.d2j.DexConstants.ACC_STATIC;

import org.junit.Test;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.runner.RunWith;

/**
 * test for huge insn 20000 and locals 2000
 * 
 * @author bob
 * 
 */
@RunWith(DexTranslatorRunner.class)
public class I168Test {

    @Test
    public static void i168(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "a", new String[] {}, "I"));
        DexCodeVisitor code = mv.visitCode();
        code.visitRegister(2000); // 2000 locals
        for (int i = 0; i < 2000; i++) {// 2000 insns
            code.visitConstStmt(Op.CONST, i, i);
        }
        for (int i = 0; i < 18000; i++) {// 18000 insns
            code.visitConstStmt(Op.CONST, 25, i);
        }
        code.visitStmt1R(Op.RETURN, 25);
        code.visitEnd();
        mv.visitEnd();
    }
}
