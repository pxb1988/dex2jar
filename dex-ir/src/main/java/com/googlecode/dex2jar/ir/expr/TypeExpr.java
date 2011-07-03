package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.ToStringUtil;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.ValueBox;

public class TypeExpr extends E1Expr {

    public Type type;

    public TypeExpr(Value value, Type type) {
        super(VT.CAST, new ValueBox(value));
        this.type = type;

    }

    public String toString() {
        switch (super.vt) {
        case CAST:
            return "((" + ToStringUtil.toShortClassName(type) + ")" + op + ")";
        case INSTANCEOF:
            return "(" + op + " instanceof " + ToStringUtil.toShortClassName(type) + ")";
        case NEW_ARRAY:
            return "new " + ToStringUtil.toShortClassName(type) + "[" + op + "]";
        }
        return "UNKNOW";
    }

}
