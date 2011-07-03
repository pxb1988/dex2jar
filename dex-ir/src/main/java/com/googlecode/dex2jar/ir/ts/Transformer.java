package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;

public interface Transformer {
    public void transform(IrMethod irMethod);
}
