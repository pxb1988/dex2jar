package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;

public class ArrayExpr extends Value {

    public ValueBox base;
    public ValueBox index;

    public ArrayExpr() {
        super(VT.ARRAY);
    }

    public ArrayExpr(Value base, Value index) {
        super(VT.ARRAY);
        this.base = new ValueBox(base);
        this.index = new ValueBox(index);
    }

    public String toString() {
        return base + "[" + index + "]";
    }
}
