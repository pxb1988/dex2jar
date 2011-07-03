package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

public class ReturnVoidStmt extends E0Stmt {

    public ReturnVoidStmt() {
        super(ST.RETURN_VOID);
    }

    @Override
    public String toString() {
        return "return";
    }
}
