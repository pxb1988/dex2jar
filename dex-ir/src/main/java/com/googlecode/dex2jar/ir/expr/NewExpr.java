package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.ToStringUtil;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E0Expr;

public class NewExpr extends E0Expr {

    public Type type;

    public NewExpr(Type type) {
        super(VT.NEW);
        this.type = type;
    }

    @Override
    public Value clone() {
        return new NewExpr(type);
    }

    public String toString() {
        return "NEW " + ToStringUtil.toShortClassName(type);
    }
}
