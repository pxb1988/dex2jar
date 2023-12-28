package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

public class UnopStmt extends E1Stmt {

    public UnopStmt(ST type, Value op) {
        super(type, op);
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        return new UnopStmt(st, op.clone(mapper));
    }

    @Override
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
        case LOCAL_END:
            return op + " ::END";
        default:
        }
        return super.toString();
    }

}
