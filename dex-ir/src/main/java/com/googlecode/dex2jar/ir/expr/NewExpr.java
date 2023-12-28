package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.E0Expr;

/**
 * @author Panxiaobo
 */
public class NewExpr extends E0Expr {

    public String type;

    public NewExpr(String type) {
        super(VT.NEW);
        this.type = type;
    }

    @Override
    public Value clone() {
        return new NewExpr(type);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new NewExpr(type);
    }

    @Override
    protected void releaseMemory() {
        type = null;
        super.releaseMemory();
    }

    @Override
    public String toString0() {
        return "NEW " + Util.toShortClassName(type);
    }

}
