package pxb.jimple.test;

import static pxb.xjimple.Constant.n;
import static pxb.xjimple.Constant.nInt;
import static pxb.xjimple.Constant.nNull;
import static pxb.xjimple.Constant.nString;
import static pxb.xjimple.expr.Exprs.nAdd;
import static pxb.xjimple.expr.Exprs.nArray;
import static pxb.xjimple.expr.Exprs.nExceptionRef;
import static pxb.xjimple.expr.Exprs.nGt;
import static pxb.xjimple.expr.Exprs.nInvokeVirtual;
import static pxb.xjimple.expr.Exprs.nLocal;
import static pxb.xjimple.expr.Exprs.nNewArray;
import static pxb.xjimple.stmt.Stmts.nAssign;
import static pxb.xjimple.stmt.Stmts.nGoto;
import static pxb.xjimple.stmt.Stmts.nIf;
import static pxb.xjimple.stmt.Stmts.nLabel;
import static pxb.xjimple.stmt.Stmts.nReturn;
import static pxb.xjimple.stmt.Stmts.nReturnVoid;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import pxb.xjimple.JimpleMethod;
import pxb.xjimple.Local;
import pxb.xjimple.Trap;
import pxb.xjimple.Value;
import pxb.xjimple.stmt.AssignStmt;
import pxb.xjimple.stmt.LabelStmt;
import pxb.xjimple.stmt.StmtList;
import pxb.xjimple.stmt.UnopStmt;
import pxb.xjimple.ts.LocalSpliter;

public class LocalSplitTest {

    @Test
    public void test() {

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

        Assert.assertTrue(jm.locals.size() == 2);
        Assert.assertEquals(st2.left.value, st3.op.value);
    }

    @Test
    public void test2() {

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

        Assert.assertTrue(jm.locals.size() == 1);
    }

    @Test
    public void test3() {
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
        list.add(nAssign(ex, nExceptionRef(exType)));
        list.add(nAssign(ex,
                nInvokeVirtual(ex, new Value[0], exType, "toString", new Type[0], Type.getType(String.class))));
        list.add(nAssign(b, nNull()));
        list.add(L4);
        list.add(nReturn(b));

        new LocalSpliter().transform(jm);

        Assert.assertTrue(jm.locals.size() == 3);
    }

    @Test
    public void test4() {
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

        Assert.assertTrue(jm.locals.size() == 3);
    }
}
