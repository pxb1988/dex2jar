package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.ValueBox;

public class ArrayExpr extends E2Expr {

    public ArrayExpr() {
        super(VT.ARRAY, null, null);
    }

    public ArrayExpr(Value base, Value index) {
        super(VT.ARRAY, new ValueBox(base), new ValueBox(index));
    }

    public String toString() {
        return op1 + "[" + op2 + "]";
    }
}
