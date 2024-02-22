package com.googlecode.d2j.dex.writer.insn;

import com.googlecode.d2j.reader.Op;

public abstract class OpInsn extends Insn {

    public Op op;


    public OpInsn(Op op) {
        this.op = op;
    }

    public final boolean isLabel() {
        return true;
    }

    @Override
    public int getCodeUnitSize() {
        return op.format.size;
    }

}
