package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;


public class NewArrayExpr extends Value {

    public ValueBox size;
    public Type baseType;

    public NewArrayExpr(Type type, Value size) {
        super(VT.NEW_ARRAY);
        this.size = new ValueBox(size);
        this.baseType = type;
    }

    public String toString() {
        return "new " + baseType.getClassName() + "[" + size + "]";
    }
}
