package com.googlecode.dex2jar.ir.test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;

import org.junit.Test;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.TopologicalSort;

public class TopologicalSortTest {
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

        // System.out.println("before========");
        // System.out.println(jm);
        new TopologicalSort().transform(jm);
        // System.out.println("after======");
        // System.out.println(jm);
    }

    @Test
    public void testIF() {
        IrMethod jm = new IrMethod();

        Local b = nLocal("a");

        StmtList list = jm.stmts;
        jm.locals.add(b);

        list.add(Stmts.nAssign(b, Constant.nInt(123)));
        LabelStmt ls = Stmts.nLabel();
        list.add(Stmts.nIf(Exprs.nGt(b, Constant.nInt(22), Type.INT_TYPE), ls));
        list.add(Stmts.nAssign(b, Constant.nInt(456)));
        list.add(ls);
        list.add(Stmts.nReturnVoid());

        // System.out.println("before========");
        // System.out.println(jm);
        new TopologicalSort().transform(jm);
        // System.out.println("after======");
        // System.out.println(jm);
    }

    @Test
    public void testStartGoto() {
        IrMethod jm = new IrMethod();

        StmtList list = jm.stmts;

        LabelStmt ls = Stmts.nLabel();
        list.add(Stmts.nGoto(ls));

        list.add(ls);
        list.add(Stmts.nReturnVoid());

        new TopologicalSort().transform(jm);
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
        list.add(Stmts.nIf(Exprs.nGt(b, Constant.nInt(22), Type.INT_TYPE), L1));
        list.add(Stmts.nAssign(b, Constant.nInt(456)));
        list.add(L2);
        list.add(Stmts.nReturnVoid());
        list.add(L1);
        list.add(Stmts.nAssign(b, Constant.nInt(789)));
        list.add(Stmts.nGoto(L2));
        // System.out.println("before========");
        // System.out.println(jm);
        new TopologicalSort().transform(jm);
        // System.out.println("after======");
        // System.out.println(jm);
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
        list.add(Stmts.nIf(Exprs.nGt(b, Constant.nInt(22), Type.INT_TYPE), L1));
        list.add(L2);
        list.add(Stmts.nAssign(b, Constant.nInt(456)));
        list.add(Stmts.nReturnVoid());
        list.add(L1);
        list.add(Stmts.nAssign(b, Constant.nInt(789)));
        list.add(Stmts.nGoto(L2));
        // System.out.println("before========");
        // System.out.println(jm);
        new TopologicalSort().transform(jm);
        // System.out.println("after======");
        // System.out.println(jm);
    }

    @Test
    public void testHugeStmt() {
        IrMethod jm = new IrMethod();
        StmtList list = jm.stmts;
        Local b = nLocal("a");
        jm.locals.add(b);
        for (int i = 0; i < 100000; i++) {
            list.add(Stmts.nAssign(b, Constant.nInt(i)));
        }
        new TopologicalSort().transform(jm);
    }

}
