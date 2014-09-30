package com.googlecode.dex2jar.ir.test;

import static com.googlecode.dex2jar.ir.expr.Exprs.*;
import static com.googlecode.dex2jar.ir.stmt.Stmts.*;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.ts.AggTransformer;
import com.googlecode.dex2jar.ir.ts.SSATransformer;

public class AggTransformerTest extends BaseTransformerTest<AggTransformer> {
    @Test
    public void t001() {
        Local a = addLocal("a");
        addStmt(nAssign(a, nNewIntArray(nInt(5))));
        addStmt(nReturn(a));
        transform();
        Assert.assertEquals("only `return new int[5]` should left.", 1, stmts.getSize());
        Assert.assertEquals("no local should left", 0, locals.size());
    }

    @Test
    public void t002() {

        Local a = addLocal("a");
        Local b = addLocal("b");
        Local c = addLocal("c");

        addStmt(nAssign(a, nNewIntArray(nInt(5))));
        addStmt(nAssign(b, nInt(2)));
        addStmt(nAssign(c, nArray(a, b, "I")));
        addStmt(nReturn(c));
        transform();
        Assert.assertTrue(stmts.getSize() == 1);
        Assert.assertTrue(locals.size() == 0);
    }

    @Test
    public void test04() {

        Local array = addLocal("array");
        Local index = addLocal("index");
        Local value = addLocal("value");

        addStmt(nAssign(array, nNewIntArray(nInt(5))));
        addStmt(nAssign(index, niAdd(nInt(1999), nInt(3))));
        addStmt(nAssign(value, niAdd(index, nInt(4))));
        addStmt(nAssign(nArray(array, index, "I"), value));
        addStmt(nReturnVoid());

        transform();

        Assert.assertTrue(method.locals.size() >= 2);
    }

    @Test
    public void test05() {
        String sbType = "Ljava/lang/StringBuilder;";
        String sType = "Ljava/lang/String;";

        Local b = addLocal("b");
        Local ex = addLocal("ex");
        Local c = addLocal("c");
        Local d = addLocal("d");
        Local e = addLocal("e");
        Local cst = addLocal("cst");

        addStmt(nAssign(b, nString("123")));
        addStmt(nAssign(c, Exprs.nInvokeNew(new Value[0], new String[0], sbType)));
        addStmt(nAssign(d, c));
        addStmt(nAssign(cst, nString("p1")));
        addStmt(nAssign(c,
                Exprs.nInvokeVirtual(new Value[] { d, cst }, sbType, "append", new String[] { sType }, sbType)));
        addStmt(nAssign(e, c));
        addStmt(nAssign(cst, nString("p2")));
        addStmt(nAssign(c,
                Exprs.nInvokeVirtual(new Value[] { e, cst }, sbType, "append", new String[] { sType }, sbType)));
        addStmt(nAssign(c, Exprs.nInvokeVirtual(new Value[] { c }, sbType, "toString", new String[0], sType)));

        addStmt(nReturn(c));
        new SSATransformer().transform(method);
        transform();
        Assert.assertTrue(stmts.getSize() == 1);
        Assert.assertTrue(locals.size() == 0);
    }
}
