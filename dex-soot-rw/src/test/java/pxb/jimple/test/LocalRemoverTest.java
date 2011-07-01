package pxb.jimple.test;

import static pxb.xjimple.Constant.nInt;
import static pxb.xjimple.expr.Exprs.nGt;
import static pxb.xjimple.expr.Exprs.*;
import static pxb.xjimple.expr.Exprs.nNewArray;
import static pxb.xjimple.stmt.Stmts.nAssign;
import static pxb.xjimple.stmt.Stmts.nGoto;
import static pxb.xjimple.stmt.Stmts.*;
import static pxb.xjimple.stmt.Stmts.nLabel;
import static pxb.xjimple.stmt.Stmts.nReturn;
import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.asm.Type;

import pxb.xjimple.JimpleMethod;
import pxb.xjimple.Local;
import pxb.xjimple.stmt.LabelStmt;
import pxb.xjimple.stmt.StmtList;
import pxb.xjimple.ts.LocalRemover;
import pxb.xjimple.ts.LocalSpliter;

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

}
