package com.googlecode.dex2jar.ir.test;

import static com.googlecode.dex2jar.ir.Constant.n;
import static com.googlecode.dex2jar.ir.Constant.nInt;
import static com.googlecode.dex2jar.ir.Constant.nNull;
import static com.googlecode.dex2jar.ir.Constant.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.nAdd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nExceptionRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nGt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeVirtual;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNewArray;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nGoto;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIdentity;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIf;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nLabel;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturnVoid;
import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.LocalRemove;
import com.googlecode.dex2jar.ir.ts.LocalSplit;

public class LocalRemoverTest {
    @Test
    public void t1() {
        IrMethod jm = new IrMethod();
        StmtList list = jm.stmts;
        {
            Local a = nLocal("a");
            Local b = nLocal("b");
            Local c = nLocal("c");
            jm.locals.add(a);
            jm.locals.add(b);
            jm.locals.add(c);

            list.add(nAssign(a, nNewArray(Type.INT_TYPE, nInt(5))));
            list.add(nAssign(b, a));
            list.add(nAssign(c, b));
            list.add(nReturn(c));
        }
        new LocalSplit().transform(jm);
        new LocalRemove().transform(jm);
        Assert.assertEquals("only `return new int[5]` should left.", 1, list.getSize());
        Assert.assertEquals("no local should left", 0, jm.locals.size());
    };

    @Test
    public void t2() {
        IrMethod jm = new IrMethod();
        StmtList list = jm.stmts;
        {
            Local a = nLocal("a");
            Local b = nLocal("b");
            Local c = nLocal("c");

            jm.locals.add(a);
            jm.locals.add(b);
            jm.locals.add(c);

            list.add(nAssign(a, nInt(5)));
            list.add(nAssign(b, a));
            list.add(nAssign(c, b));
            list.add(nReturn(c));
        }
        new LocalSplit().transform(jm);
        Assert.assertTrue(jm.locals.size() == 3);
        new LocalRemove().transform(jm);
        Assert.assertTrue(jm.locals.size() == 0);
    };

    @Test
    public void t3() {
        IrMethod jm = new IrMethod();
        StmtList list = jm.stmts;
        {
            Local a = nLocal("a");
            Local b = nLocal("b");
            Local c = nLocal("c");

            jm.locals.add(a);
            jm.locals.add(b);
            jm.locals.add(c);
            LabelStmt L1 = nLabel();
            LabelStmt L2 = nLabel();
            list.add(nAssign(a, nInt(5)));
            list.add(nAssign(b, nInt(6)));
            list.add(nIf(nGt(a, b, Type.INT_TYPE), L1));
            list.add(nAssign(c, a));
            list.add(nGoto(L2));
            list.add(L1);
            list.add(nAssign(c, b));
            list.add(L2);
            list.add(nReturn(c));
        }
        new LocalSplit().transform(jm);
        Assert.assertTrue(jm.locals.size() == 3);
        new LocalRemove().transform(jm);
        Assert.assertTrue(jm.locals.size() == 1);
    };

    @Test
    public void t4() {
        IrMethod jm = new IrMethod();
        StmtList list = jm.stmts;
        {
            Local a = nLocal("a");
            Local b = nLocal("b");
            Local c = nLocal("c");

            jm.locals.add(a);
            jm.locals.add(b);
            jm.locals.add(c);

            list.add(nAssign(a, nNewArray(Type.INT_TYPE, nInt(5))));
            list.add(nAssign(b, nInt(2)));
            list.add(nAssign(c, nArray(a, b)));
            list.add(nReturn(c));
        }
        new LocalSplit().transform(jm);
        Assert.assertTrue(jm.locals.size() == 3);
        new LocalRemove().transform(jm);
        Assert.assertTrue(jm.locals.size() == 0);
    };

    @Test
    public void t5() {

        IrMethod jm = new IrMethod();

        Local b = nLocal("a");

        StmtList list = jm.stmts;
        jm.locals.add(b);

        AssignStmt st1 = nAssign(b, nString("123"));
        list.add(st1);
        AssignStmt st2 = nAssign(b, n(null, null));
        list.add(st2);
        UnopStmt st3 = nReturn(b);
        list.add(st3);

        new LocalSplit().transform(jm);
        new LocalRemove().transform(jm);
        Assert.assertEquals(0, jm.locals.size());
        Assert.assertEquals(1, list.getSize());
    }

    @Test
    public void t6() {

        IrMethod jm = new IrMethod();

        Local b = nLocal("a");

        StmtList list = jm.stmts;
        jm.locals.add(b);

        LabelStmt L1 = nLabel();
        LabelStmt L2 = nLabel();
        list.add(nIf(nGt(nInt(100), nInt(0), Type.INT_TYPE), L1));
        list.add(nAssign(b, nString("123")));
        list.add(nGoto(L2));
        list.add(L1);
        list.add(nAssign(b, n(null, null)));
        list.add(L2);

        list.add(nReturn(b));

        new LocalSplit().transform(jm);
        new LocalRemove().transform(jm);
        Assert.assertEquals(1, jm.locals.size());
    }

    @Test
    public void t7() {
        Type exType = Type.getType("Ljava/lang/Exception;");
        IrMethod jm = new IrMethod();

        LabelStmt L1 = nLabel();
        LabelStmt L2 = nLabel();
        LabelStmt L3 = nLabel();
        LabelStmt L4 = nLabel();
        jm.traps.add(new Trap(L1, L2, new LabelStmt[] { L3 }, new Type[] { exType }));

        Local b = nLocal("a");
        Local ex = nLocal("ex");
        StmtList list = jm.stmts;
        jm.locals.add(b);
        jm.locals.add(ex);

        list.add(L1);
        list.add(nAssign(b, nString("123")));
        list.add(nAssign(ex, nInt(5)));
        list.add(Stmts.nLock(nInt(0)));
        list.add(L2);
        list.add(nGoto(L4));
        list.add(L3);
        list.add(nIdentity(ex, nExceptionRef(exType)));
        list.add(nAssign(ex,
                nInvokeVirtual(new Value[] { ex }, exType, "toString", new Type[0], Type.getType(String.class))));
        list.add(nAssign(b, nNull()));
        list.add(L4);
        list.add(nReturn(b));

        new LocalSplit().transform(jm);
        new LocalRemove().transform(jm);
        Assert.assertTrue(jm.locals.size() == 3);
    }

    @Test
    public void t8() {
        IrMethod jm = new IrMethod();

        Local array = nLocal("array");
        Local index = nLocal("index");
        Local value = nLocal("value");
        StmtList list = jm.stmts;
        jm.locals.add(array);
        jm.locals.add(index);
        jm.locals.add(value);

        list.add(nAssign(array, nNewArray(Type.INT_TYPE, nInt(5))));
        list.add(nAssign(index, nAdd(nInt(1999), nInt(3), Type.INT_TYPE)));
        list.add(nAssign(value, nAdd(index, nInt(4), Type.INT_TYPE)));
        list.add(nAssign(nArray(array, index), value));
        list.add(nReturnVoid());

        new LocalSplit().transform(jm);
        new LocalRemove().transform(jm);
        Assert.assertTrue(jm.locals.size() == 2);
    }

    @Test
    public void t9() {
        Type exType = Type.getType("Ljava/lang/Exception;");
        IrMethod jm = new IrMethod();

        LabelStmt L1 = nLabel();
        LabelStmt L2 = nLabel();
        LabelStmt L3 = nLabel();
        LabelStmt L4 = nLabel();
        jm.traps.add(new Trap(L1, L2, new LabelStmt[] { L3 }, new Type[] { exType }));

        Local b = nLocal("b");
        Local ex = nLocal("ex");
        Local c = nLocal("c");
        Local d = nLocal("d");
        Local e = nLocal("e");
        Local cst = nLocal("cst");
        StmtList list = jm.stmts;
        jm.locals.add(b);
        jm.locals.add(ex);
        jm.locals.add(c);
        jm.locals.add(d);
        jm.locals.add(e);
        jm.locals.add(cst);

        list.add(L1);
        list.add(nAssign(b, nString("123")));
        list.add(nAssign(ex, nString("test ex")));

        list.add(nAssign(c, Exprs.nInvokeNew(new Value[0], new Type[0], Type.getType(StringBuilder.class))));
        list.add(nAssign(d, c));
        list.add(nAssign(cst, nString("p1")));
        list.add(nAssign(c, Exprs.nInvokeVirtual(new Value[] { d, cst }, Type.getType(StringBuilder.class), "append",
                new Type[] { Type.getType(String.class) }, Type.getType(StringBuilder.class))));
        list.add(nAssign(e, c));
        list.add(nAssign(cst, nString("p2")));
        list.add(nAssign(c, Exprs.nInvokeVirtual(new Value[] { e, cst }, Type.getType(StringBuilder.class), "append",
                new Type[] { Type.getType(String.class) }, Type.getType(StringBuilder.class))));
        list.add(nAssign(c, Exprs.nInvokeVirtual(new Value[] { c }, Type.getType(StringBuilder.class), "toString",
                new Type[0], Type.getType(String.class))));

        list.add(L2);
        list.add(nGoto(L4));
        list.add(L3);
        list.add(nIdentity(ex, nExceptionRef(exType)));
        list.add(nAssign(ex,
                nInvokeVirtual(new Value[] { ex }, exType, "toString", new Type[0], Type.getType(String.class))));
        list.add(nAssign(b, nNull()));
        list.add(L4);
        list.add(nReturn(b));

        new LocalSplit().transform(jm);

        new LocalRemove().transform(jm);
        Assert.assertTrue(jm.locals.size() == 4);
    }
}
