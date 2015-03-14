package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class Stmt2RNode extends DexStmtNode {
    public final int a;
    public final int b;

    public Stmt2RNode(Op op, int a, int b) {
        super(op);
        this.a = a;
        this.b = b;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitStmt2R(op, a, b);
    }
}
