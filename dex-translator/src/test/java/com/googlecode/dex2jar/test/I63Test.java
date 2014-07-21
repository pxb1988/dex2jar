package com.googlecode.dex2jar.test;

import com.googlecode.d2j.DexConstants;
import org.junit.Test;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.runner.RunWith;

/**
 * test case for issue 63
 */
@RunWith(DexTranslatorRunner.class)
public class I63Test implements DexConstants {

    @Test
    public static void i63(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitRegister(1);
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                code.visitLabel(L1);
                code.visitFieldStmt(Op.SGET, 0, -1, new Field("La;", "f", "J"));
                code.visitLabel(L2);
                code.visitStmt0R(Op.RETURN_VOID);
                code.visitEnd();
                code.visitTryCatch(L1, L2, new DexLabel[] { L2 }, new String[] { "La;" });
            }
            mv.visitEnd();
        }
    }
}
