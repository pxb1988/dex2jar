package com.googlecode.dex2jar.test;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.googlecode.d2j.reader.Op.ADD_LONG;
import static com.googlecode.d2j.reader.Op.CONST_WIDE;
import static com.googlecode.d2j.reader.Op.RETURN_VOID;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
@ExtendWith(DexTranslatorRunner.class)
public class Issue71Test implements DexConstants {

    @Test
    public static void i71(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_STATIC, new Method("La;", "test", new String[]{}, "V"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitRegister(2);
                code.visitConstStmt(CONST_WIDE, 0, 0L);
                code.visitConstStmt(CONST_WIDE, 1, 2L);
                code.visitStmt3R(ADD_LONG, 0, 0, 1);
                code.visitStmt0R(RETURN_VOID);
                code.visitEnd();
            }
            mv.visitEnd();
        }
        cv.visitEnd();
    }

}
