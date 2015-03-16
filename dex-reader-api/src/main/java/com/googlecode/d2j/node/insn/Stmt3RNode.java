package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class Stmt3RNode extends DexStmtNode {
    public final int a;
    public final int b;
    public final int c;

    public Stmt3RNode(Op op, int a, int b, int c) {
        super(op);
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitStmt3R(op, a, b, c);
    }
}
