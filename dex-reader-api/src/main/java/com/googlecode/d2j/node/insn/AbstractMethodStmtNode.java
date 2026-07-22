package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.Proto;
import com.googlecode.d2j.reader.Op;

public abstract class AbstractMethodStmtNode extends DexStmtNode {

    public final int[] args;

    public AbstractMethodStmtNode(Op op, int[] args) {
        super(op);
        this.args = args;
    }

    public abstract Proto getProto();

}
