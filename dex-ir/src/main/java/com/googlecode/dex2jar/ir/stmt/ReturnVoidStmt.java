package com.googlecode.dex2jar.ir.stmt;

import java.util.Map;

public class ReturnVoidStmt extends Stmt {

    public ReturnVoidStmt() {
        super(ST.RETURN_VOID);
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        return new ReturnVoidStmt();
    }

    @Override
    public String toString() {
        return "return";
    }
}
