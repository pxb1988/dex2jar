package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.ts.RemoveLocalFromSSA;
import org.junit.jupiter.api.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static org.junit.jupiter.api.Assertions.*;

public class RemoveLocalFromSSATest extends BaseTransformerTest<RemoveLocalFromSSA> {

    @Test
    public void t001() {

        Local a = addLocal("a");
        Local b = addLocal("b");
        Local c = addLocal("c");

        Stmt sa = addStmt(nAssign(a, nInt(0)));
        addStmt(nAssign(b, a));
        addStmt(nAssign(c, b));
        Stmt sb = addStmt(nReturn(c));
        transform();
        assertEquals(sa.getOp1(), sb.getOp());
        assertEquals(1, locals.size(), "1 local should left");
    }

}
