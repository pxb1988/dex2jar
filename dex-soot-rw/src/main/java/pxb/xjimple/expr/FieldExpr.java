package pxb.xjimple.expr;

import org.objectweb.asm.Type;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class FieldExpr extends Value {

    public String fieldName;
    public Type fieldType;
    public Type fieldOwnerType;
    public ValueBox object;

    public FieldExpr(Value object, Type ownerType, String fieldName, Type fieldType) {
        super(VT.FIELD);
        this.object = new ValueBox(object);
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.fieldOwnerType = ownerType;
    }

    public String toString() {
        return (object == null ? fieldOwnerType.getClassName() : object) + "." + fieldName;
    }

}
