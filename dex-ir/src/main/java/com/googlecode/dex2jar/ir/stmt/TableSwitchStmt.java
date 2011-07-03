package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

public class TableSwitchStmt extends E1Stmt {

    public LabelStmt defaultTarget;
    public int lowIndex, highIndex;
    public LabelStmt[] targets;

    public TableSwitchStmt() {
        super(ST.TABLE_SWITCH, null);
    }

    public TableSwitchStmt(Value key, int lowIndex, int highIndex, LabelStmt[] targets, LabelStmt defaultTarget) {
        super(ST.TABLE_SWITCH, new ValueBox(key));
        this.lowIndex = lowIndex;
        this.highIndex = highIndex;
        this.targets = targets;
        this.defaultTarget = defaultTarget;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("switch(").append(op).append(") {");

        for (int i = 0; i < targets.length; i++) {
            sb.append("case ").append(lowIndex + i).append(": GOTO ").append(targets[i].label).append(";");
        }
        sb.append("default : GOTO ").append(defaultTarget.label).append(";");
        sb.append("}");
        return sb.toString();
    }

}
