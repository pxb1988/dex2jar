package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;


public class CastExpr extends Value {

    public Type castType;
    public ValueBox op;

    public CastExpr(Value value, Type type) {
        super(VT.CAST);
        this.castType = type;
        this.op = new ValueBox(value);
    }

    public String toString() {
        return "((" + castType.getClassName() + ")" + op + ")";
    }

}
