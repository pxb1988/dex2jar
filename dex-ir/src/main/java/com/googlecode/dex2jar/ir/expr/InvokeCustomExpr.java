package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.CallSite;
import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class InvokeCustomExpr extends InvokeExpr {
    public CallSite callSite;

    @Override
    protected void releaseMemory() {
        callSite = null;
        super.releaseMemory();
    }

    @Override
    public Proto getProto() {
        return callSite.getMethodProto();
    }

    public InvokeCustomExpr(VT type, Value[] args, CallSite callSite) {
        super(type, args, callSite.getBootstrapMethodHandler().getMethod());
        this.callSite = callSite;
    }

    @Override
    public InvokeCustomExpr clone() {
        return new InvokeCustomExpr(vt, cloneOps(), callSite);
    }

    @Override
    public InvokeCustomExpr clone(LabelAndLocalMapper mapper) {
        return new InvokeCustomExpr(vt, cloneOps(mapper), callSite);
    }

    @Override
    public String toString0() {
        return "InvokeCustomExpr(....)";
    }

}
