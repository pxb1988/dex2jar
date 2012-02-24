package com.googlecode.dex2jar.ir;

import java.util.Map;

import com.googlecode.dex2jar.ir.stmt.LabelStmt;

public class LocalVar {

    public LabelStmt start, end;
    public String name, type, signature;
    public ValueBox reg;

    public LocalVar(String name, String type, String signature, LabelStmt start, LabelStmt end, ValueBox reg) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.type = type;
        this.signature = signature;
        this.reg = reg;
    }

    public LocalVar clone(Map<LabelStmt, LabelStmt> map) {
        return new LocalVar(name, type, signature, start.clone(map), end.clone(map), new ValueBox(reg.value.clone()));
    }

    @Override
    public String toString() {
        return String.format(".var %s ~ %s %s -> %s //%s", start.getDisplayName(), end.getDisplayName(), reg, name,
                type);
    }
}
