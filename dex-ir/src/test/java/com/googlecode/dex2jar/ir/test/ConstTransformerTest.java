package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.ConstTransformer;
import org.junit.jupiter.api.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;
import static com.googlecode.dex2jar.ir.expr.Exprs.nString;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static org.junit.jupiter.api.Assertions.*;

public class ConstTransformerTest {

    @Test
    public void test00() {
        IrMethod jm = new IrMethod();

        Local a = nLocal("a");
        jm.locals.add(a);
        jm.stmts.add(nAssign(a, nString("a String")));
        UnopStmt retStmt = nReturn(a);
        jm.stmts.add(retStmt);
        new ConstTransformer().transform(jm);

        assertEquals(1, jm.locals.size());
        assertEquals(2, jm.stmts.getSize());
        assertEquals("a String", ((Constant) retStmt.op.trim()).value);
    }

    @Test
    public void test01() {// local in phi
        IrMethod jm = new IrMethod();

        Local a = nLocal("a");
        Local p = nLocal("p");
        jm.locals.add(a);
        jm.locals.add(p);
        jm.stmts.add(nAssign(a, nString("a String")));
        jm.stmts.add(nAssign(p, Exprs.nPhi(a)));
        UnopStmt retStmt = nReturn(p);
        jm.stmts.add(retStmt);
        new ConstTransformer().transform(jm);

        assertEquals(2, jm.locals.size());
        assertEquals(3, jm.stmts.getSize());
        assertEquals("a String", ((Constant) retStmt.op.trim()).value);
    }

    @Test
    public void test02() {// test local loop
        IrMethod jm = new IrMethod();

        Local a = nLocal("a");
        Local p = nLocal("p");
        Local q = nLocal("q");
        jm.locals.add(a);
        jm.locals.add(p);
        jm.locals.add(q);
        jm.stmts.add(nAssign(a, nString("a String")));
        jm.stmts.add(nAssign(p, Exprs.nPhi(a, q)));
        jm.stmts.add(nAssign(q, Exprs.nPhi(p)));
        UnopStmt retStmt = nReturn(q);
        jm.stmts.add(retStmt);
        new ConstTransformer().transform(jm);

        assertEquals(3, jm.locals.size());
        assertEquals(4, jm.stmts.getSize());
        assertEquals("a String", ((Constant) retStmt.op.trim()).value);
    }

    @Test
    public void test03() {// test local loop
        IrMethod jm = new IrMethod();

        Local a = nLocal("a");
        Local b = nLocal("b");
        Local p = nLocal("p");
        jm.locals.add(a);
        jm.locals.add(b);
        jm.locals.add(p);
        jm.stmts.add(nAssign(a, nString("a String")));
        jm.stmts.add(nAssign(b, nString("b String")));
        jm.stmts.add(nAssign(p, Exprs.nPhi(a, b)));
        UnopStmt retStmt = nReturn(p);
        jm.stmts.add(retStmt);
        new ConstTransformer().transform(jm);

        assertEquals(3, jm.locals.size());
        assertEquals(4, jm.stmts.getSize());
        assertEquals(p, retStmt.op.trim());
    }

    @Test
    public void test04() {
        IrMethod jm = new IrMethod();

        Local a = nLocal("a");
        Local b = nLocal("b");
        Local p = nLocal("p");
        jm.locals.add(a);
        jm.locals.add(b);
        jm.locals.add(p);
        jm.stmts.add(nAssign(a, nString("a String")));
        jm.stmts.add(nAssign(b, nString("a String")));
        jm.stmts.add(nAssign(p, Exprs.nPhi(a, b)));
        UnopStmt retStmt = nReturn(p);
        jm.stmts.add(retStmt);
        new ConstTransformer().transform(jm);

        assertEquals(3, jm.locals.size());
        assertEquals(4, jm.stmts.getSize());
        assertEquals(VT.CONSTANT, retStmt.op.vt);
    }

}
