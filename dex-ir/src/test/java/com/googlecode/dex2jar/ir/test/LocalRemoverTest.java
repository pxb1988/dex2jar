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

import com.googlecode.dex2jar.ir.JimpleMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.LocalRemover;
import com.googlecode.dex2jar.ir.ts.LocalSpliter;

public class LocalRemoverTest {
    @Test
    public void t1() {
        JimpleMethod jm = new JimpleMethod();
        StmtList list = jm.stmts;
        {
            Local a = nLocal("a", null);
            Local b = nLocal("b", null);
            Local c = nLocal("c", null);
            jm.locals.add(a);
            jm.locals.add(b);
            jm.locals.add(c);

            list.add(nAssign(a, nNewArray(Type.INT_TYPE, nInt(5))));
            list.add(nAssign(b, a));
            list.add(nAssign(c, b));
            list.add(nReturn(c));
        }
        new LocalSpliter().transform(jm);
        new LocalRemover().transform(jm);
        Assert.assertEquals("only `return new int[5]` should left.", 1, list.getSize());
        Assert.assertEquals("no local should left", 0, jm.locals.size());
    };

    @Test
    public void t2() {
        JimpleMethod jm = new JimpleMethod();
        StmtList list = jm.stmts;
        {
            Local a = nLocal("a", null);
            Local b = nLocal("b", null);
            Local c = nLocal("c", null);

            jm.locals.add(a);
            jm.locals.add(b);
            jm.locals.add(c);

            list.add(nAssign(a, nInt(5)));
            list.add(nAssign(b, a));
            list.add(nAssign(c, b));
            list.add(nReturn(c));
        }
        new LocalSpliter().transform(jm);
        Assert.assertTrue(jm.locals.size() == 3);
        new LocalRemover().transform(jm);
        Assert.assertTrue(jm.locals.size() == 0);
    };

    @Test
    public void t3() {
        JimpleMethod jm = new JimpleMethod();
        StmtList list = jm.stmts;
        {
            Local a = nLocal("a", null);
            Local b = nLocal("b", null);
            Local c = nLocal("c", null);

            jm.locals.add(a);
            jm.locals.add(b);
            jm.locals.add(c);
            LabelStmt L1 = nLabel();
            LabelStmt L2 = nLabel();
            list.add(nAssign(a, nInt(5)));
            list.add(nAssign(b, nInt(6)));
            list.add(nIf(nGt(a, b), L1));
            list.add(nAssign(c, a));
            list.add(nGoto(L2));
            list.add(L1);
            list.add(nAssign(c, b));
            list.add(L2);
            list.add(nReturn(c));
        }
        new LocalSpliter().transform(jm);
        Assert.assertTrue(jm.locals.size() == 3);
        new LocalRemover().transform(jm);
        Assert.assertTrue(jm.locals.size() == 1);
    };

    @Test
    public void t4() {
        JimpleMethod jm = new JimpleMethod();
        StmtList list = jm.stmts;
        {
            Local a = nLocal("a", null);
            Local b = nLocal("b", null);
            Local c = nLocal("c", null);

            jm.locals.add(a);
            jm.locals.add(b);
            jm.locals.add(c);

            list.add(nAssign(a, nNewArray(Type.INT_TYPE, nInt(5))));
            list.add(nAssign(b, nInt(2)));
            list.add(nAssign(c, nArray(a, b)));
            list.add(nReturn(c));
        }
        new LocalSpliter().transform(jm);
        Assert.assertTrue(jm.locals.size() == 3);
        new LocalRemover().transform(jm);
        Assert.assertTrue(jm.locals.size() == 0);
    };

    @Test
    public void t5() {

        JimpleMethod jm = new JimpleMethod();

        Local b = nLocal("a", null);

        StmtList list = jm.stmts;
        jm.locals.add(b);

        AssignStmt st1 = nAssign(b, nString("123"));
        list.add(st1);
        AssignStmt st2 = nAssign(b, n(null, null));
        list.add(st2);
        UnopStmt st3 = nReturn(b);
        list.add(st3);

        new LocalSpliter().transform(jm);
        new LocalRemover().transform(jm);
        Assert.assertEquals(0, jm.locals.size());
        Assert.assertEquals(1, list.getSize());
    }

    @Test
    public void t6() {

        JimpleMethod jm = new JimpleMethod();

        Local b = nLocal("a", null);

        StmtList list = jm.stmts;
        jm.locals.add(b);

        LabelStmt L1 = nLabel();
        LabelStmt L2 = nLabel();
        list.add(nIf(nGt(nInt(100), nInt(0)), L1));
        list.add(nAssign(b, nString("123")));
        list.add(nGoto(L2));
        list.add(L1);
        list.add(nAssign(b, n(null, null)));
        list.add(L2);

        list.add(nReturn(b));

        new LocalSpliter().transform(jm);
        new LocalRemover().transform(jm);
        Assert.assertEquals(1, jm.locals.size());
    }

    @Test
    public void t7() {
        Type exType = Type.getType("Ljava/lang/Exception;");
        JimpleMethod jm = new JimpleMethod();

        LabelStmt L1 = nLabel();
        LabelStmt L2 = nLabel();
        LabelStmt L3 = nLabel();
        LabelStmt L4 = nLabel();
        jm.traps.add(new Trap(L1, L2, L3, exType));

        Local b = nLocal("a", null);
        Local ex = nLocal("ex", exType);
        StmtList list = jm.stmts;
        jm.locals.add(b);
        jm.locals.add(ex);

        list.add(L1);
        list.add(nAssign(b, nString("123")));
        // list.add(nThrow(nInvokeNew(nNull, new Type[0], exType)));
        list.add(L2);
        list.add(nGoto(L4));
        list.add(L3);
        list.add(nIdentity(ex, nExceptionRef(exType)));
        list.add(nAssign(ex,
                nInvokeVirtual(new Value[] { ex }, exType, "toString", new Type[0], Type.getType(String.class))));
        list.add(nAssign(b, nNull()));
        list.add(L4);
        list.add(nReturn(b));

        new LocalSpliter().transform(jm);
        new LocalRemover().transform(jm);
        Assert.assertTrue(jm.locals.size() == 3);
    }

    @Test
    public void t8() {
        JimpleMethod jm = new JimpleMethod();

        Local array = nLocal("array", null);
        Local index = nLocal("index", null);
        Local value = nLocal("value", null);
        StmtList list = jm.stmts;
        jm.locals.add(array);
        jm.locals.add(index);
        jm.locals.add(value);

        list.add(nAssign(array, nNewArray(Type.INT_TYPE, nInt(5))));
        list.add(nAssign(index, nAdd(nInt(1999), nInt(3))));
        list.add(nAssign(value, nAdd(index, nInt(4))));
        list.add(nAssign(nArray(array, index), value));
        list.add(nReturnVoid());

        new LocalSpliter().transform(jm);
        new LocalRemover().transform(jm);
        Assert.assertTrue(jm.locals.size() == 2);
    }
}
