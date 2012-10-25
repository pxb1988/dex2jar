package com.googlecode.dex2jar.test;

import org.junit.Test;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.OdexOpcodes;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * test case for issue 63
 */
public class I63Test implements OdexOpcodes {

    public static void i63(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitArguments(1, new int[] {});
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                code.visitLabel(L1);
                code.visitFieldStmt(OP_SGET, 0, new Field("La;", "f", "J"), TYPE_WIDE);
                code.visitLabel(L2);
                code.visitReturnStmt(OP_RETURN_VOID);
                code.visitEnd();
                code.visitTryCatch(L1, L2, L2, "La;");
            }
            mv.visitEnd();
        }
    }

    @Test
    public void test() throws Exception {
        TestUtils.testDexASMifier(getClass(), "i63", "i63");
    }

}
