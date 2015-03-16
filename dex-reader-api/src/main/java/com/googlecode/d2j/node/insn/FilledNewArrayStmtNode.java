package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class FilledNewArrayStmtNode extends DexStmtNode {

    public final int[] args;
    public final String type;

    public FilledNewArrayStmtNode(Op op, int[] args, String type) {
        super(op);
        this.args = args;
        this.type = type;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitFilledNewArrayStmt(op, args, type);
    }
}
