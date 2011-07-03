package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

public class NopStmt extends E0Stmt {

    public NopStmt() {
        super(ST.NOP);
    }

    public String toString() {
        return "NOP";
    }
}
