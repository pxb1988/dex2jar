package com.googlecode.dex2jar.ir.stmt;

import java.util.Map;

import org.objectweb.asm.Label;

public class LabelStmt extends Stmt {

    public LabelStmt(Label label) {
        super(ST.LABEL);
        this.label = label;
    }

    public Label label;

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        LabelStmt clone = map.get(this);
        if (clone == null) {
            clone = new LabelStmt(new Label());
            map.put(this, clone);
        }
        return clone;
    }

    public String toString() {
        return label + ":";
    }

}
