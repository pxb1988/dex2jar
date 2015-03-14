package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;


public class ConstStmtNode extends DexStmtNode {
    public final int a;
    public final Object value;

    public ConstStmtNode(Op op, int a, Object value) {
        super(op);
        this.a = a;
        this.value = value;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitConstStmt(op, a, value);
    }
}
