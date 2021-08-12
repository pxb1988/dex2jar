package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value.E1Expr;

/**
 * Represent a non-static Field expression.
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 * @see VT#FIELD
 */
public class FieldExpr extends E1Expr {

    /**
     * Field name
     */
    public String name;

    /**
     * Field owner type descriptor
     */
    public String owner;

    /**
     * Field type descriptor
     */
    public String type;

    public FieldExpr(Value object, String ownerType, String fieldName, String fieldType) {
        super(VT.FIELD, object);
        this.name = fieldName;
        this.owner = ownerType;
        this.type = fieldType;
    }

    @Override
    protected void releaseMemory() {
        name = null;
        owner = null;
        type = null;
        super.releaseMemory();
    }

    @Override
    public Value clone() {
        return new FieldExpr(op.trim().clone(), owner, name, type);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new FieldExpr(op.clone(mapper), owner, name, type);
    }

    @Override
    public String toString0() {
        return op + "." + name;
    }

}
