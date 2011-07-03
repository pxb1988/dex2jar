package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

public class UnopStmt extends E1Stmt {

    public UnopStmt(ST type, ValueBox op) {
        super(type, op);
    }

    public String toString() {
        switch (super.st) {
        case LOCK:
            return "lock " + op;
        case UNLOCK:
            return "unlock " + op;
        case THROW:
            return "throw " + op;
        case RETURN:
            return "return " + op;
        }
        return super.toString();
    }

}
