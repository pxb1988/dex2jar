package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.DeadCodeTransformer;
import org.junit.Assert;
import org.junit.Test;

public class DeadCodeTrnasformerTest extends BaseTransformerTest<DeadCodeTransformer> {
    @Test
    public void test09DeadCode() {
        Stmt ret = addStmt(Stmts.nReturnVoid());
        Stmt lb = addStmt(newLabel());
        addStmt(Stmts.nReturnVoid());
        transform();
        Assert.assertSame(ret, method.stmts.getFirst());
        Assert.assertSame(ret, method.stmts.getLast());
    }
}
