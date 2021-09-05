package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class InvokeCustomExpr extends InvokeExpr {

    public String name;

    public Proto proto;

    public MethodHandle handle;

    public Object[] bsmArgs;

    @Override
    protected void releaseMemory() {
        name = null;
        proto = null;
        handle = null;
        bsmArgs = null;
        super.releaseMemory();
    }

    @Override
    public Proto getProto() {
        return proto;
    }

    public InvokeCustomExpr(VT type, Value[] args, String methodName, Proto proto, MethodHandle handle,
                            Object[] bsmArgs) {
        super(type, args, handle == null ? null : handle.getMethod());
        this.proto = proto;
        this.name = methodName;
        this.handle = handle;
        this.bsmArgs = bsmArgs;
    }

    @Override
    public InvokeCustomExpr clone() {
        return new InvokeCustomExpr(vt, cloneOps(), name, proto, handle, bsmArgs);
    }

    @Override
    public InvokeCustomExpr clone(LabelAndLocalMapper mapper) {
        return new InvokeCustomExpr(vt, cloneOps(mapper), name, proto, handle, bsmArgs);
    }

    @Override
    public String toString0() {
        return "InvokeCustomExpr(....)";
    }

}
