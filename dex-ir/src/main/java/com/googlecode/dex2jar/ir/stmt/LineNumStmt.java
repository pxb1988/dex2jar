package com.googlecode.dex2jar.ir.stmt;

import java.util.Map;

import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

public class LineNumStmt extends E0Stmt {

    public String displayName;
    public LabelStmt label;
    public int line;

    public LineNumStmt(int line, LabelStmt label) {
        super(ST.LINENUMBER);
        this.label = label;
        this.line = line;
    }

    @Override
    public LineNumStmt clone(Map<LabelStmt, LabelStmt> map) {
        return new LineNumStmt(line, label.clone(map));
    }

    public String getDisplayName() {
        return displayName == null ? label.toString() : displayName;
    }

    @Override
    public String toString() {
        return label.getDisplayName() + " -> line " + line;
    }

}
