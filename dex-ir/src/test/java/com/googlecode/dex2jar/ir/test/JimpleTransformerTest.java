package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.JimpleTransformer;
import org.junit.jupiter.api.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nAdd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static org.junit.jupiter.api.Assertions.*;

public class JimpleTransformerTest extends BaseTransformerTest<JimpleTransformer> {

    /**
     * test for return 1+2+3;
     */
    @Test
    public void test00Base() {
        initMethod(true, "I");
        addStmt(nReturn(nAdd(nAdd(nInt(1), nInt(2), "I"), nInt(3), "I")));
        transform();

        assertEquals(3, method.stmts.getSize(), "should expends to 3 stmts");
        assertEquals(2, method.locals.size(), "should expends to 2 locals");

        // System.out.println(super.method);
    }

    /**
     * test for System.out.print("Hello JNI");
     */
    @Test
    public void test01HelloWord() {
        initMethod(true, "V");
        addStmt(Stmts.nVoidInvoke(Exprs.nInvokeVirtual(
                new Value[]{//
                        Exprs.nStaticField("Ljava/lang/System;", "out", "Ljava/io/PrintStream;"),//
                        Exprs.nString("Hello JNI")}, "Ljava/io/PrintStream;", "println",
                new String[]{"Ljava/lang/String;"}, "V")));
        transform();
        assertEquals(3, method.stmts.getSize(), "should expends to 3 stmts");
        assertEquals(2, method.locals.size(), "should expends to 2 locals");

        // System.out.println(super.method);
    }
}
