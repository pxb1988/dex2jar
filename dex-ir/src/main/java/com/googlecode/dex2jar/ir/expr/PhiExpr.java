package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;

public class PhiExpr extends EnExpr {

    public PhiExpr(Value[] ops) {
        super(VT.PHI, ops);
    }

    @Override
    public Value clone() {
        return new PhiExpr(cloneOps());
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new PhiExpr(cloneOps(mapper));
    }

    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder("φ(");
        boolean first = true;
        for (Value vb : ops) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(vb);
        }
        sb.append(")");
        return sb.toString();
    }

}
