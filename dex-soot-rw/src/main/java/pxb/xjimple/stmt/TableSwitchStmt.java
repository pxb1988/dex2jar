package pxb.xjimple.stmt;

import java.util.Map;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class TableSwitchStmt extends Stmt {

    public LabelStmt defaultTarget;
    public ValueBox key;
    public int lowIndex, highIndex;
    public LabelStmt[] targets;

    public TableSwitchStmt() {
        super(ST.TABLE_SWITCH);
    }

    public TableSwitchStmt(Value key, int lowIndex, int highIndex, LabelStmt[] targets, LabelStmt defaultTarget) {
        super(ST.TABLE_SWITCH);
        this.key = new ValueBox(key);
        this.lowIndex = lowIndex;
        this.highIndex = highIndex;
        this.targets = targets;
        this.defaultTarget = defaultTarget;
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        LabelStmt[] cloneTargets;
        if (targets != null) {
            cloneTargets = new LabelStmt[targets.length];
            for (int i = 0; i < targets.length; i++) {
                cloneTargets[i] = (LabelStmt) targets[i].clone(map);

            }
        } else {
            cloneTargets = null;
        }
        return new TableSwitchStmt(key.value, lowIndex, highIndex, cloneTargets, (LabelStmt) defaultTarget.clone(map));

    }

    public String toString() {
        StringBuilder sb = new StringBuilder("switch(").append(key).append(") {");

        for (int i = 0; i < targets.length; i++) {
            sb.append("case ").append(lowIndex + i).append(": GOTO ").append(targets[i].label).append(";");
        }
        sb.append("default : GOTO ").append(defaultTarget.label).append(";");
        sb.append("}");
        return sb.toString();
    }

}
