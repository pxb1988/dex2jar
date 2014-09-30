package com.googlecode.dex2jar.ir.test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nCast;
import static com.googlecode.dex2jar.ir.expr.Exprs.nGt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;
import static com.googlecode.dex2jar.ir.expr.Exprs.nParameterRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nThisRef;

import org.junit.Test;

import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;

public class StmtListTest {
    @Test
    public void toStringTest() {

        StmtList list = new StmtList();
        Local a = nLocal("this");
        Local b = nLocal("b");
        Local c = nLocal("c");
        Local d = nLocal("d");
        LabelStmt L1 = Stmts.nLabel();

        list.add(Stmts.nIdentity(a, nThisRef("La/Some;")));
        list.add(Stmts.nIdentity(b, nParameterRef("I", 0)));
        list.add(Stmts.nIdentity(c, nParameterRef("J", 1)));
        list.add(Stmts.nIdentity(d, nParameterRef("F", 2)));
        list.add(Stmts.nIf(nGt(b, nInt(0), "I"), L1));
        list.add(Stmts.nAssign(c, nCast(d, "F", "J")));
        list.add(L1);
        list.toString();
    }
}
