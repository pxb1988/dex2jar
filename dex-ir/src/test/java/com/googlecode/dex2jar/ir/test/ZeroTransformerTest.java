package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.ts.RemoveConstantFromSSA;
import com.googlecode.dex2jar.ir.ts.ZeroTransformer;
import org.junit.Assert;
import org.junit.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeStatic;
import static com.googlecode.dex2jar.ir.expr.Exprs.nPhi;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;

public class ZeroTransformerTest extends BaseTransformerTest<ZeroTransformer> {
    @Test
    public void t001() {

        Local a = addLocal("a");
        Local c = addLocal("c");
        Local p = addLocal("p");
        Local q = addLocal("q");

        addStmt(nAssign(a, nInt(0)));
        addStmt(nAssign(c, nInvokeStatic(new Value[0], "La;", "a", new String[0], "I")));
        LabelStmt L1 = newLabel();
        addStmt(L1);
        Stmt sa = attachPhi(L1, nAssign(q, nPhi(a, c)));
        Stmt sb = attachPhi(L1, nAssign(p, nPhi(a, c)));
        addStmt(nReturn(p));
        transform();
        Assert.assertNotEquals("a is split to 2 local", sb.getOp2().getOps()[0], sa.getOp2().getOps()[0]);
        Assert.assertEquals("c is keep same", sb.getOp2().getOps()[1], sa.getOp2().getOps()[1]);
    }
}
