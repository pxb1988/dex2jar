package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;

public class AssignStmt extends E2Stmt {

    public AssignStmt(ST type, ValueBox left, ValueBox right) {
        super(type, left, right);
    }

    public String toString() {
        switch (st) {
        case ASSIGN:
            return op1 + " = " + op2;
        case IDENTITY:
            return op1 + " := " + op2;
        }
        return super.toString();
    }

}
