package pxb.xjimple.expr;

import org.objectweb.asm.Type;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class InstanceOfExpr extends Value {

    public ValueBox op;
    public Type instanceType;

    public InstanceOfExpr(Value op, Type type2) {
        super(VT.INSTANCEOF);
        this.op = new ValueBox(op);
        instanceType = type2;
    }

    public String toString() {
        return "(" + op + " instanceof " + instanceType.getClassName() + ")";
    }
}
