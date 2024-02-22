package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value.E1Expr;

/**
 * Represent a LENGTH,NEG expression
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 * @see VT#LENGTH
 * @see VT#NEG
 * @see VT#NOT
 */
public class UnopExpr extends E1Expr {

    public String type;

    @Override
    protected void releaseMemory() {
        type = null;
        super.releaseMemory();
    }

    public UnopExpr(VT vt, Value value, String type) {
        super(vt, value);
        this.type = type;
    }

    @Override
    public Value clone() {
        return new UnopExpr(vt, op.trim().clone(), type);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new UnopExpr(vt, op.clone(mapper), type);
    }

    @Override
    public String toString0() {
        switch (vt) {
        case LENGTH:
            return op + ".length";
        case NEG:
            return "(-" + op + ")";
        case NOT:
            return "(!" + op + ")";
        default:
        }
        return super.toString();
    }

}
