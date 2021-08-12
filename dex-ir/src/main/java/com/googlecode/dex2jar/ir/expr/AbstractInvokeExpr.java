package com.googlecode.dex2jar.ir.expr;


import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;

public abstract class AbstractInvokeExpr extends EnExpr {

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
    }

    public abstract Proto getProto();

    public AbstractInvokeExpr(VT type, Value[] args) {
        super(type, args);
    }

}
