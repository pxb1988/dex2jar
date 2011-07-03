package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

public class JumpStmt extends E1Stmt {

    public LabelStmt target;

    /**
     * GOTO
     * 
     * @param type
     * @param target
     */
    public JumpStmt(ST type, LabelStmt target) {
        this(type, null, target);
    }

    /**
     * IF
     * 
     * @param type
     * @param condition
     * @param target
     */
    public JumpStmt(ST type, ValueBox condition, LabelStmt target) {
        super(type, condition);
        this.target = target;
    }

    public String toString() {
        switch (st) {
        case GOTO:
            return "GOTO " + target;
        case IF:
            return "if " + op + " GOTO " + target;
        }
        return super.toString();
    }
}
