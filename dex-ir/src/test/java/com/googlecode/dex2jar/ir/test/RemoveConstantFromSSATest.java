package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.ts.RemoveConstantFromSSA;
import org.junit.jupiter.api.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNewIntArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNull;
import static com.googlecode.dex2jar.ir.expr.Exprs.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.niGt;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nGoto;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIf;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static org.junit.jupiter.api.Assertions.*;

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
        assertFalse(method.stmts.contains(sa), "SA should remove from method");
        assertFalse(method.stmts.contains(sb), "SB should remove from method");
    }

    @Test
    public void t002() {
        Local a = addLocal("a");
        addStmt(nAssign(a, nInt(5)));
        addStmt(nReturn(a));

        transform();
        assertEquals(0, locals.size(), "no local should kept");
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
        assertFalse(locals.contains(b), "local b should removed");
        assertEquals(2, locals.size());
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
        assertEquals(3, locals.size(), "all local should kept");
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
        assertSame(ret.getOp().vt, Value.VT.CONSTANT, "should return '123'");
    }

}
