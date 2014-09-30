package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;

public class LocalVar {

    public LabelStmt start, end;
    public String name, type, signature;
    public Local reg;

    public LocalVar(String name, String type, String signature, LabelStmt start, LabelStmt end, Local reg) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.type = type;
        this.signature = signature;
        this.reg = reg;
    }

    public LocalVar clone(LabelAndLocalMapper map) {
        return new LocalVar(name, type, signature, start.clone(map), end.clone(map), (Local) reg.clone());
    }

    @Override
    public String toString() {
        return String.format(".var %s ~ %s %s -> %s //%s", start.getDisplayName(), end.getDisplayName(), reg, name,
                type);
    }
}
