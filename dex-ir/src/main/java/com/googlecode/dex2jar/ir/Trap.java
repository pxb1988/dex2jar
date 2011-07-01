package com.googlecode.dex2jar.ir;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.stmt.LabelStmt;

public class Trap {
    public LabelStmt start, end, handler;
    public Type type;

    public Trap(LabelStmt start, LabelStmt end, LabelStmt handler, Type type) {
        super();
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

    public Trap() {
        super();
    }

}
