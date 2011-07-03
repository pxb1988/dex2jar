package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.ToStringUtil;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.ValueBox;

public class FieldExpr extends E1Expr {

    public String fieldName;
    public Type fieldType;
    public Type fieldOwnerType;

    public FieldExpr(ValueBox object, Type ownerType, String fieldName, Type fieldType) {
        super(VT.FIELD, object);
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.fieldOwnerType = ownerType;
    }

    public String toString() {
        return (op == null ? ToStringUtil.toShortClassName(fieldOwnerType) : op) + "." + fieldName;
    }

}
