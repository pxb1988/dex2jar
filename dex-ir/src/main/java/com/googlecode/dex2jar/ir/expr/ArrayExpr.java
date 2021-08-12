package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value.E2Expr;

/**
 * Represent an Array expression
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 * @see VT#ARRAY
 */
public class ArrayExpr extends E2Expr {

    public ArrayExpr() {
        super(VT.ARRAY, null, null);
    }

    public String elementType;

    public ArrayExpr(Value base, Value index, String elementType) {
        super(VT.ARRAY, base, index);
        this.elementType = elementType;
    }

    @Override
    public Value clone() {
        return new ArrayExpr(op1.trim().clone(), op2.trim().clone(), this.elementType);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new ArrayExpr(op1.clone(mapper), op2.clone(mapper), this.elementType);
    }

    @Override
    public String toString0() {
        return op1 + "[" + op2 + "]";
    }

}
