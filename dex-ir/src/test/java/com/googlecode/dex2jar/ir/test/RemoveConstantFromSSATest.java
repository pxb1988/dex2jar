package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.ts.AggTransformer;
import com.googlecode.dex2jar.ir.ts.RemoveConstantFromSSA;
import org.junit.Assert;
import org.junit.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.*;
import static com.googlecode.dex2jar.ir.stmt.Stmts.*;

public class RemoveConstantFromSSATest extends BaseTransformerTest<RemoveConstantFromSSA> {
    @Test
    public void t001() {
        Local a = addLocal("a");
        Local b = addLocal("b");
        Local c1 = addLocal("c1");
        Local c2 = addLocal("c2");
        Local cphi = addLocal("cphi");
        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();

        Stmt sa = addStmt(nAssign(a, nInt(5)));
        Stmt sb = addStmt(nAssign(b, nInt(6)));
        addStmt(nIf(niGt(a, b), L1));
        addStmt(nAssign(c1, a));
        addStmt(nGoto(L2));
        addStmt(L1);
        addStmt(nAssign(c2, b));
        addStmt(L2);
        attachPhi(L2, nAssign(cphi, Exprs.nPhi(c1, c2)));
        addStmt(nReturn(cphi));

        transform();
        Assert.assertFalse("SA should remove from method", method.stmts.contains(sa));
        Assert.assertFalse("SB should remove from method", method.stmts.contains(sb));
    }

    @Test
    public void t002() {
        Local a = addLocal("a");
        addStmt(nAssign(a, nInt(5)));
        addStmt(nReturn(a));

        transform();
        Assert.assertTrue("no local should kept", locals.size() == 0);
    }

    @Test
    public void t003() {

        Local a = addLocal("a");
        Local b = addLocal("b");
        Local c = addLocal("c");

        addStmt(nAssign(a, nNewIntArray(nInt(5))));
        addStmt(nAssign(b, nInt(2)));
        addStmt(nAssign(c, nArray(a, b, "I")));
        addStmt(nReturn(c));
        transform();
        Assert.assertTrue("local b should removed", !locals.contains(b));
        Assert.assertTrue(locals.size() == 2);
    }

    @Test
    public void t004() {

        Local a0 = addLocal("a0");
        Local a1 = addLocal("a1");
        Local ax = addLocal("aX");

        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();
        addStmt(nIf(niGt(nInt(100), nInt(0)), L1));
        addStmt(nAssign(a0, nString("123")));
        addStmt(nGoto(L2));
        addStmt(L1);
        addStmt(nAssign(a1, nNull()));
        addStmt(L2);
        attachPhi(L2, nAssign(ax, Exprs.nPhi(a0, a1)));
        addStmt(nReturn(ax));

        transform();
        Assert.assertEquals("all local should kept", 3, locals.size());
    }

    @Test
    public void t005PhiValueEqual() {

        Local a0 = addLocal("a0");
        Local a1 = addLocal("a1");
        Local ax = addLocal("aX");

        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();
        addStmt(nIf(niGt(nInt(100), nInt(0)), L1));
        addStmt(nAssign(a0, nString("123")));
        addStmt(nGoto(L2));
        addStmt(L1);
        addStmt(nAssign(a1, nString("123")));
        addStmt(L2);
        attachPhi(L2, nAssign(ax, Exprs.nPhi(a0, a1)));
        Stmt ret = addStmt(nReturn(ax));

        transform();
        Assert.assertTrue("should return '123'", ret.getOp().vt == Value.VT.CONSTANT);
    }
}
