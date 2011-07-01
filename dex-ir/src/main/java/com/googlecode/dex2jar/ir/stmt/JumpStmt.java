package com.googlecode.dex2jar.ir.stmt;

import java.util.Map;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;


public class JumpStmt extends Stmt {

    public ValueBox condition;
    public LabelStmt target;

    private JumpStmt(ST type) {
        super(type);
    }

    /**
     * GOTO
     * 
     * @param type
     * @param target
     */
    public JumpStmt(ST type, LabelStmt target) {
        this(type);
        this.target = target;
    }

    /**
     * IF
     * 
     * @param type
     * @param condition
     * @param target
     */
    public JumpStmt(ST type, Value condition, LabelStmt target) {
        this(type, target);
        this.condition = new ValueBox(condition);
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        return new JumpStmt(st, condition.value, (LabelStmt) target.clone(map));
    }

    public String toString() {
        switch (st) {
        case GOTO:
            return "GOTO " + target;
        case IF:
            return "if " + condition + " GOTO " + target;
        }
        return super.toString();
    }
}
