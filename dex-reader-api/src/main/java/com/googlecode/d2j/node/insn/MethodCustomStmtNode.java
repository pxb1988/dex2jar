package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.CallSite;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class MethodCustomStmtNode extends AbstractMethodStmtNode {
    public final CallSite callSite;

    public MethodCustomStmtNode(Op op, int[] args, CallSite callSite) {
        super(op, args);
        this.callSite = callSite;
    }

    @Override
    public void accept(DexCodeVisitor cv) {
        cv.visitMethodStmt(op, args, callSite);
    }

    @Override
    public Proto getProto() {
        return callSite.getMethodProto();
    }

}
