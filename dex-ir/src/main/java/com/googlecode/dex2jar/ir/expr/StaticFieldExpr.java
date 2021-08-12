package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.E0Expr;

/**
 * Represent a StaticField expression
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 * @see VT#STATIC_FIELD
 */
public class StaticFieldExpr extends E0Expr {

    /**
     * Field name
     */
    public String name;

    /**
     * Field owner type
     */
    public String owner;

    /**
     * Field type
     */
    public String type;

    @Override
    protected void releaseMemory() {
        name = null;
        owner = null;
        type = null;
        super.releaseMemory();
    }

    public StaticFieldExpr(String ownerType, String fieldName, String fieldType) {
        super(VT.STATIC_FIELD);
        this.name = fieldName;
        this.owner = ownerType;
        this.type = fieldType;
    }

    @Override
    public Value clone() {
        return new StaticFieldExpr(owner, name, type);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new StaticFieldExpr(owner, name, type);
    }

    @Override
    public String toString0() {
        return Util.toShortClassName(owner) + "." + name;
    }

}
