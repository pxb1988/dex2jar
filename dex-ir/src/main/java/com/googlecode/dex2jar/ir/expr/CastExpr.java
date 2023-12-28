package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.E1Expr;

/**
 * * @see VT#CAST
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class CastExpr extends E1Expr {

    public String from;

    public String to;

    public CastExpr(Value value, String from, String to) {
        super(VT.CAST, value);
        this.from = from;
        this.to = to;
    }

    @Override
    protected void releaseMemory() {
        from = null;
        to = null;
        super.releaseMemory();
    }

    @Override
    public Value clone() {
        return new CastExpr(op.trim().clone(), from, to);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new CastExpr(op.clone(mapper), from, to);
    }

    @Override
    public String toString0() {
        return "((" + Util.toShortClassName(to) + ")" + op + ")";
    }

}
