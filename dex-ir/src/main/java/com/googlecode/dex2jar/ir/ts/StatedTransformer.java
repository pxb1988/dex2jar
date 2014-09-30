package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;

public abstract class StatedTransformer implements Transformer {
    public abstract boolean transformReportChanged(IrMethod method);

    @Override
    public void transform(IrMethod method) {
        transformReportChanged(method);
    }
}
