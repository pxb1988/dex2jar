package com.googlecode.dex2jar.ir.stmt;

import java.util.Map;


import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

public class LocVarStmt extends E1Stmt {

    public LabelStmt start, end;
    public String name, type, signature;

    public LocVarStmt(String name, String type, String signature, LabelStmt start,
            LabelStmt end, ValueBox reg) {
        super(ST.LOCALVARIABLE, reg);
        this.name = name;
        this.start = start;
        this.end = end;
        this.type = type;
        this.signature = signature;
    }


    @Override
    public String toString() {
        return new StringBuilder().append(start.getDisplayName()).append(" ~ ").append(end.getDisplayName())
                .append(" ").append(op.value).append(" -> ").append(name).append(" // ").append(type).toString();
    }


    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        return new LocVarStmt(name, type, signature, start.clone(map),
                end.clone(map), new ValueBox(op.value.clone()));
    }
}
