package com.googlecode.dex2jar.ir.test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;

import org.junit.Test;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Reorder;

public class ReorderTest {
    @Test
    public void testGOTO() {
        IrMethod jm = new IrMethod();

        Local b = nLocal("a");

        StmtList list = jm.stmts;
        jm.locals.add(b);

        list.add(Stmts.nAssign(b, Constant.nInt(123)));
        LabelStmt ls = Stmts.nLabel();
        list.add(Stmts.nGoto(ls));
        list.add(Stmts.nAssign(b, Constant.nInt(456)));
        list.add(ls);
        list.add(Stmts.nReturnVoid());

        new Reorder().transform(jm);
    }

    @Test
    public void testIF() {
        IrMethod jm = new IrMethod();

        Local b = nLocal("a");

        StmtList list = jm.stmts;
        jm.locals.add(b);

        list.add(Stmts.nAssign(b, Constant.nInt(123)));
        LabelStmt ls = Stmts.nLabel();
        list.add(Stmts.nIf(Exprs.nGt(b, Constant.nInt(22)), ls));
        list.add(Stmts.nAssign(b, Constant.nInt(456)));
        list.add(ls);
        list.add(Stmts.nReturnVoid());

        new Reorder().transform(jm);
    }

    @Test
    public void testIF_ELSE() {
        IrMethod jm = new IrMethod();

        Local b = nLocal("a");

        StmtList list = jm.stmts;
        jm.locals.add(b);

        list.add(Stmts.nAssign(b, Constant.nInt(123)));
        LabelStmt L1 = Stmts.nLabel();
        LabelStmt L2 = Stmts.nLabel();
        list.add(Stmts.nIf(Exprs.nGt(b, Constant.nInt(22)), L1));
        list.add(Stmts.nAssign(b, Constant.nInt(456)));
        list.add(L2);
        list.add(Stmts.nReturnVoid());
        list.add(L1);
        list.add(Stmts.nAssign(b, Constant.nInt(789)));
        list.add(Stmts.nGoto(L2));
        new Reorder().transform(jm);
    }

    @Test
    public void testIF_ELSE2() {
        IrMethod jm = new IrMethod();

        Local b = nLocal("a");

        StmtList list = jm.stmts;
        jm.locals.add(b);

        list.add(Stmts.nAssign(b, Constant.nInt(123)));
        LabelStmt L1 = Stmts.nLabel();
        LabelStmt L2 = Stmts.nLabel();
        list.add(Stmts.nIf(Exprs.nGt(b, Constant.nInt(22)), L1));
        list.add(L2);
        list.add(Stmts.nAssign(b, Constant.nInt(456)));
        list.add(Stmts.nReturnVoid());
        list.add(L1);
        list.add(Stmts.nAssign(b, Constant.nInt(789)));
        list.add(Stmts.nGoto(L2));
        new Reorder().transform(jm);
    }
}
