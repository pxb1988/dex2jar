package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;

/**
 * Represent a NEW_MUTI_ARRAY expression.
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 * @see VT#NEW_MUTI_ARRAY
 */
public class NewMutiArrayExpr extends EnExpr {

    /**
     * the basic type, ZBSCIFDJL, no [
     */
    public String baseType;

    /**
     * the dimension of the array,
     * <p/>
     * for baseType: I, dimension 4, the result type is int[][][][];
     * <p/>
     * NOTICE, not all dimension are init in ops, so ops.length <= dimension
     */
    public int dimension;

    public NewMutiArrayExpr(String base, int dimension, Value[] sizes) {
        super(VT.NEW_MUTI_ARRAY, sizes);
        this.baseType = base;
        this.dimension = dimension;
    }

    @Override
    protected void releaseMemory() {
        baseType = null;
        super.releaseMemory();
    }

    @Override
    public Value clone() {
        return new NewMutiArrayExpr(baseType, dimension, cloneOps());
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new NewMutiArrayExpr(baseType, dimension, cloneOps(mapper));
    }

    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(Util.toShortClassName(baseType));
        for (Value op : ops) {
            sb.append('[').append(op).append(']');
        }
        for (int i = ops.length; i < dimension; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

}
