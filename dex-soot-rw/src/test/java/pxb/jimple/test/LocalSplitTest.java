package pxb.jimple.test;

import org.junit.Assert;
import org.junit.Test;

import pxb.xjimple.Constant;
import pxb.xjimple.JimpleMethod;
import pxb.xjimple.Local;
import pxb.xjimple.expr.Exprs;
import pxb.xjimple.stmt.AssignStmt;
import pxb.xjimple.stmt.LabelStmt;
import pxb.xjimple.stmt.StmtList;
import pxb.xjimple.stmt.Stmts;
import pxb.xjimple.stmt.UnopStmt;
import pxb.xjimple.ts.LocalSpliter;

public class LocalSplitTest {
    @Test
    public void test() {

        JimpleMethod jm = new JimpleMethod();

        Local b = Exprs.nLocal("a", null);

        StmtList list = jm.stmts;
        jm.locals.add(b);
        AssignStmt st1 = Stmts.nAssign(b, Constant.nString("123"));
        list.add(st1);
        AssignStmt st2 = Stmts.nAssign(b, Constant.n(null, null));
        list.add(st2);
        UnopStmt st3 = Stmts.nReturn(b);
        list.add(st3);

        new LocalSpliter().split(jm);

        Assert.assertTrue(jm.locals.size() == 2);
        // Assert.assertEquals(Constant.STRING, ((Local) st1.left.value).type);
        // Assert.assertEquals(Type.INT_TYPE, ((Local) st2.left.value).type);
        Assert.assertEquals(st2.left.value, st3.op.value);
    }

    @Test
    public void test2() {

        JimpleMethod jm = new JimpleMethod();

        Local b = Exprs.nLocal("a", null);

        StmtList list = jm.stmts;
        jm.locals.add(b);

        LabelStmt L1 = Stmts.nLabel();
        LabelStmt L2 = Stmts.nLabel();
        list.add(Stmts.nIf(Exprs.nGt(Constant.nInt(100), Constant.nInt(0)), L1));
        list.add(Stmts.nAssign(b, Constant.nString("123")));
        list.add(Stmts.nGoto(L2));
        list.add(L1);
        list.add(Stmts.nAssign(b, Constant.n(null, null)));
        list.add(L2);

        UnopStmt st3 = Stmts.nReturn(b);
        list.add(st3);

        new LocalSpliter().split(jm);
    }
}
