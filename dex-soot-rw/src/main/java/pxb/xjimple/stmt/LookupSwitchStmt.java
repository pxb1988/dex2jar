package pxb.xjimple.stmt;

import java.util.Map;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class LookupSwitchStmt extends Stmt {

    public LabelStmt defaultTarget;
    public ValueBox key;
    public Value[] lookupValues;
    public LabelStmt[] targets;

    public LookupSwitchStmt() {
        super(ST.LOOKUP_SWITCH);
    }

    public LookupSwitchStmt(Value key, Value[] lookupValues, LabelStmt[] targets, LabelStmt defaultTarget) {
        super(ST.LOOKUP_SWITCH);
        this.key = new ValueBox(key);
        this.lookupValues = lookupValues;
        this.targets = targets;
        this.defaultTarget = defaultTarget;
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        LabelStmt[] cloneTargets;
        Value[] cloneValue;
        if (targets != null) {
            cloneValue = new Value[lookupValues.length];
            cloneTargets = new LabelStmt[targets.length];
            for (int i = 0; i < targets.length; i++) {
                cloneTargets[i] = (LabelStmt) targets[i].clone(map);
                cloneValue[i] = lookupValues[i];
            }
        } else {
            cloneTargets = null;
            cloneValue = null;
        }
        return new LookupSwitchStmt(key.value, cloneValue, cloneTargets, (LabelStmt) defaultTarget.clone(map));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("switch(").append(key).append(") {");

        for (int i = 0; i < lookupValues.length; i++) {
            sb.append("case ").append(lookupValues[i]).append(": GOTO ").append(targets[i].label).append(";");
        }
        sb.append("default : GOTO ").append(defaultTarget.label).append(";");
        sb.append("}");
        return sb.toString();
    }
}
