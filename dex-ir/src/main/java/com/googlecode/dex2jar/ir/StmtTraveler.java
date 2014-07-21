package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;

import java.util.Iterator;

public class StmtTraveler {
    public void travel(StmtList stmts) {
        for (Iterator<Stmt> it = stmts.iterator(); it.hasNext(); ) {
            Stmt stmt = it.next();
            Stmt n = travel(stmt);
            if (n != stmt) {
                stmts.insertBefore(stmt, n);
                it.remove();
            }
        }
    }

    public Stmt travel(Stmt stmt) {
        switch (stmt.et) {
            case E0:
                break;
            case E1:
                stmt.setOp(travel(stmt.getOp()));
                break;
            case E2:
                stmt.setOp1(travel(stmt.getOp1()));
                stmt.setOp2(travel(stmt.getOp2()));
                break;
            case En:
                Value[] ops = stmt.getOps();
                for (int i = 0; i < ops.length; i++) {
                    ops[i] = travel(ops[i]);
                }
                break;
        }
        return stmt;
    }

    public Value travel(Value op) {
        switch (op.et) {
            case E0:
                break;
            case E1:
                op.setOp(travel(op.getOp()));
                break;
            case E2:
                op.setOp1(travel(op.getOp1()));
                op.setOp2(travel(op.getOp2()));
                break;
            case En:
                Value[] ops = op.getOps();
                for (int i = 0; i < ops.length; i++) {
                    ops[i] = travel(ops[i]);
                }
                break;
        }

        return op;
    }
}
