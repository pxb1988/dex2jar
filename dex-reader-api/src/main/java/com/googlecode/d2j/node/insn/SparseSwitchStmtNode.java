package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class SparseSwitchStmtNode extends BaseSwitchStmtNode {

    public final int[] cases;

    public SparseSwitchStmtNode(Op op, int a, int[] cases, DexLabel[] labels) {
        super(op, a, labels);
        this.cases = cases;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitSparseSwitchStmt(op, a, cases, labels);
    }
}
