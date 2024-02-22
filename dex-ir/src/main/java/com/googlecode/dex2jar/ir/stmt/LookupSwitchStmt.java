package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;

/**
 * Represent a LOOKUP_SWITCH statement
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 * @see ST#LOOKUP_SWITCH
 */
public class LookupSwitchStmt extends BaseSwitchStmt {

    public int[] lookupValues;

    public LookupSwitchStmt(Value key, int[] lookupValues, LabelStmt[] targets, LabelStmt defaultTarget) {
        super(ST.LOOKUP_SWITCH, key);
        this.lookupValues = lookupValues;
        this.targets = targets;
        this.defaultTarget = defaultTarget;
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        LabelStmt[] nTargets = new LabelStmt[targets.length];
        for (int i = 0; i < nTargets.length; i++) {
            nTargets[i] = mapper.map(targets[i]);
        }
        int[] nLookupValues = new int[lookupValues.length];
        System.arraycopy(lookupValues, 0, nLookupValues, 0, nLookupValues.length);

        return new LookupSwitchStmt(op.clone(mapper), nLookupValues, nTargets, mapper.map(defaultTarget));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("switch(").append(op).append(") {");

        for (int i = 0; i < lookupValues.length; i++) {
            sb.append("\n case ").append(lookupValues[i]).append(": GOTO ").append(targets[i].getDisplayName())
                    .append(";");
        }
        sb.append("\n default : GOTO ").append(defaultTarget.getDisplayName()).append(";");
        sb.append("\n}");
        return sb.toString();
    }

}
