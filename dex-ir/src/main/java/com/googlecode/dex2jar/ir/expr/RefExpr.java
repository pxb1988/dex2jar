package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value.E0Expr;

public class RefExpr extends E0Expr {

    public RefExpr(VT vt, Type refType, int index) {
        super(vt);
        this.type = refType;
        this.parameterIndex = index;
    }

    public int parameterIndex;
    public Type type;

    public String toString() {
        switch (vt) {
        case THIS_REF:
            return "@this";
        case PARAMETER_REF:
            return "@parameter_" + parameterIndex;
        case EXCEPTION_REF:
            return "@Exception";
        }
        return super.toString();
    }

}
