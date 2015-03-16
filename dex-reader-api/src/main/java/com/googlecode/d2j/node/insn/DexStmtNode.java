package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;


public abstract class DexStmtNode {
    public final Op op;

    public int __index;

    protected DexStmtNode(Op op) {
        this.op = op;
    }

    public abstract void accept(DexCodeVisitor cv);
}
