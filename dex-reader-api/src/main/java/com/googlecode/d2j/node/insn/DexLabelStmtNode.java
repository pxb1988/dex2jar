package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class DexLabelStmtNode extends DexStmtNode {
    public DexLabel label;

    public DexLabelStmtNode(DexLabel label) {
        super(null);
        this.label = label;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitLabel(label);
    }
}
