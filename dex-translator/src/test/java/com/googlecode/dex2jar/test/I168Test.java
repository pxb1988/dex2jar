package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.ACC_PUBLIC;
import static com.googlecode.dex2jar.DexOpcodes.ACC_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_CONST;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN_VOID;
import static com.googlecode.dex2jar.DexOpcodes.TYPE_SINGLE;

import org.junit.Ignore;
import org.junit.Test;

import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * test for huge insn 20000 and locals 2000
 * 
 * @author bob
 * 
 */
@Ignore("waiting for fix")
public class I168Test {

    @Test
    public void test() throws Exception {
        TestUtils.testDexASMifier(getClass(), "a");
    }

    public static void a(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "a", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitArguments(2000, new int[] {}); // 2000 locals
        for (int i = 0; i < 2000; i++) {// 2000 insns
            code.visitConstStmt(OP_CONST, i, i, TYPE_SINGLE);
        }
        for (int i = 0; i < 18000; i++) {// 18000 insns
            code.visitConstStmt(OP_CONST, 25, i, TYPE_SINGLE);
        }
        code.visitReturnStmt(OP_RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }
}
