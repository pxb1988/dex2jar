package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class PackedSwitchStmtNode extends BaseSwitchStmtNode {

    public final int firstCase;

    public PackedSwitchStmtNode(Op op, int a, int firstCase, DexLabel[] labels) {
        super(op, a, labels);
        this.firstCase = firstCase;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitPackedSwitchStmt(op, a, firstCase, labels);
    }

}
