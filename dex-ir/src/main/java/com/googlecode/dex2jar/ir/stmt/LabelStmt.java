package com.googlecode.dex2jar.ir.stmt;

import org.objectweb.asm.Label;

import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

public class LabelStmt extends E0Stmt {

    public LabelStmt(Label label) {
        super(ST.LABEL);
        this.label = label;
    }

    public Label label;

    public String toString() {
        return label + ":";
    }

}
