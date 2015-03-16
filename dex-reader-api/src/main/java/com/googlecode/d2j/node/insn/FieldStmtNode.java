package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class FieldStmtNode extends DexStmtNode {

    public final int a;
    public final int b;
    public final Field field;

    public FieldStmtNode(Op op, int a, int b, Field field) {
        super(op);
        this.a = a;
        this.b = b;
        this.field = field;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitFieldStmt(op, a, b, field);
    }
}
