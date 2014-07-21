package com.googlecode.dex2jar.ir.test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nAdd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.JimpleTransformer;

public class JimpleTransformerTest extends BaseTransformerTest<JimpleTransformer> {

    /**
     * test for return 1+2+3;
     */
    @Test
    public void test00Base() {
        initMethod(true, "I");
        addStmt(nReturn(nAdd(nAdd(nInt(1), nInt(2), "I"), nInt(3), "I")));
        transform();

        Assert.assertEquals("should expends to 3 stmts", 3, method.stmts.getSize());
        Assert.assertEquals("should expends to 2 locals", 2, method.locals.size());

        // System.out.println(super.method);
    }

    /**
     * test for System.out.print("Hello JNI");
     */
    @Test
    public void test01HelloWord() {
        initMethod(true, "V");
        addStmt(Stmts.nVoidInvoke(Exprs.nInvokeVirtual(
                new Value[] {//
                Exprs.nStaticField("Ljava/lang/System;", "out", "Ljava/io/PrintStream;"),//
                        Exprs.nString("Hello JNI") }, "Ljava/io/PrintStream;", "println",
                new String[] { "Ljava/lang/String;" }, "V")));
        transform();
        Assert.assertEquals("should expends to 3 stmts", 3, method.stmts.getSize());
        Assert.assertEquals("should expends to 2 locals", 2, method.locals.size());

        // System.out.println(super.method);
    }
}
