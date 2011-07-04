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

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.LocalSplit;

public class LocalSplitTest {

    @Test
    public void test() {

        IrMethod jm = new IrMethod();

        Local b = nLocal("a", null);

        StmtList list = jm.stmts;
        jm.locals.add(b);

        AssignStmt st1 = nAssign(b, nString("123"));
        list.add(st1);
        AssignStmt st2 = nAssign(b, n(null, null));
        list.add(st2);
        UnopStmt st3 = nReturn(b);
        list.add(st3);

        new LocalSplit().transform(jm);

        Assert.assertTrue(jm.locals.size() == 2);
        Assert.assertEquals(st2.op1.value, st3.op.value);
    }

    @Test
    public void test2() {

        IrMethod jm = new IrMethod();

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

        new LocalSplit().transform(jm);

        Assert.assertTrue(jm.locals.size() == 1);
    }

    @Test
    public void test3() {
        Type exType = Type.getType("Ljava/lang/Exception;");
        IrMethod jm = new IrMethod();

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
        list.add(nAssign(ex, nString("test ex")));
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

        Assert.assertTrue(jm.locals.size() == 4);
    }

    @Test
    public void test4() {
        IrMethod jm = new IrMethod();

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

        new LocalSplit().transform(jm);

        Assert.assertTrue(jm.locals.size() == 3);
    }
}
