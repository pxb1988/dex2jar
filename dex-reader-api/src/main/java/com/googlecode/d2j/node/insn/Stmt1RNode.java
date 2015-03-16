package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class Stmt1RNode extends DexStmtNode {

    public final int a;

    public Stmt1RNode(Op op, int a) {
        super(op);
        this.a = a;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitStmt1R(op, a);
    }
}
