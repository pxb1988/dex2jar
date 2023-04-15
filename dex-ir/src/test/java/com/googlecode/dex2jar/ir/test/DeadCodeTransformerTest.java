package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.DeadCodeTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeadCodeTransformerTest extends BaseTransformerTest<DeadCodeTransformer> {

    @Test
    public void test09DeadCode() {
        Stmt ret = addStmt(Stmts.nReturnVoid());
        Stmt lb = addStmt(newLabel());
        addStmt(Stmts.nReturnVoid());
        transform();
        assertSame(ret, method.stmts.getFirst());
        assertSame(ret, method.stmts.getLast());
    }

}
