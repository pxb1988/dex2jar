package pxb.xjimple;

import org.objectweb.asm.Type;

import pxb.xjimple.stmt.LabelStmt;

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
