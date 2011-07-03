package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

public class LookupSwitchStmt extends E1Stmt {

    public LabelStmt defaultTarget;
    public int[] lookupValues;
    public LabelStmt[] targets;

    public LookupSwitchStmt(ValueBox key, int[] lookupValues, LabelStmt[] targets, LabelStmt defaultTarget) {
        super(ST.LOOKUP_SWITCH, key);
        this.lookupValues = lookupValues;
        this.targets = targets;
        this.defaultTarget = defaultTarget;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("switch(").append(op).append(") {");

        for (int i = 0; i < lookupValues.length; i++) {
            sb.append("case ").append(lookupValues[i]).append(": GOTO ").append(targets[i].label).append(";");
        }
        sb.append("default : GOTO ").append(defaultTarget.label).append(";");
        sb.append("}");
        return sb.toString();
    }
}
