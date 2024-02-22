package com.googlecode.dex2jar.ir.stmt;

public interface JumpStmt {

    LabelStmt getTarget();

    void setTarget(LabelStmt labelStmt);

}
