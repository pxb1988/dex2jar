package pxb.xjimple.expr;

import org.objectweb.asm.Type;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class CastExpr extends Value {

    public Type castType;
    public ValueBox op;

    public CastExpr(Value value, Type type) {
        super(VT.CAST);
        this.castType = type;
        this.op = new ValueBox(value);
    }

    public String toString() {
        return "((" + castType.getClassName() + ")" + op + ")";
    }

}
