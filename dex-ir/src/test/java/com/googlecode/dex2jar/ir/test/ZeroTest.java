package com.googlecode.dex2jar.ir.test;

import static com.googlecode.dex2jar.ir.Constant.nInt;
import static com.googlecode.dex2jar.ir.Constant.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nGoto;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIf;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nLabel;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import junit.framework.Assert;

import org.junit.Test;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.ts.ZeroTransformer;

public class ZeroTest {
    @Test
    public void test() {
        IrMethod jm = new IrMethod();

        Local a = nLocal("a");
        Local b = nLocal("b");
        Local c = nLocal("c");
        jm.locals.add(a);
        jm.locals.add(b);
        jm.locals.add(c);

        StmtList list = jm.stmts;

        LabelStmt L1 = nLabel();
        L1.displayName = "L1";
        LabelStmt L2 = nLabel();
        L2.displayName = "L2";
        LabelStmt L3 = nLabel();
        L3.displayName = "L3";
        LabelStmt L4 = nLabel();
        L4.displayName = "L4";

        AssignStmt st1 = nAssign(a, nInt(0));
        list.add(st1);
        list.add(nIf(nInt(1), L4));

        list.add(L1);
        list.add(nIf(nInt(0), L3));

        list.add(L2);
        AssignStmt st2 = nAssign(c, a);
        list.add(st2);
        list.add(nGoto(L1));

        list.add(L3);
        AssignStmt st3 = nAssign(b, a);
        list.add(st3);
        list.add(nAssign(a, nString("aaaa")));

        list.add(L4);
        list.add(nReturn(a));

        new ZeroTransformer().transform(jm);

        Assert.assertEquals(((Constant) st2.op2.value).value, Integer.valueOf(0));
        Assert.assertEquals(((Constant) st3.op2.value).value, Integer.valueOf(0));
    }
}
