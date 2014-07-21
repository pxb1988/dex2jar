package com.googlecode.dex2jar.ir.test;

import static com.googlecode.dex2jar.ir.expr.Exprs.*;
import static com.googlecode.dex2jar.ir.stmt.Stmts.*;

import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.ts.RemoveLocalFromSSA;
import org.junit.Assert;
import org.junit.Test;

import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.ts.RemoveConstantFromSSA;

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
        Assert.assertEquals(sa.getOp1(), sb.getOp());
        Assert.assertEquals("1 local should left", 1, locals.size());
    }


}
