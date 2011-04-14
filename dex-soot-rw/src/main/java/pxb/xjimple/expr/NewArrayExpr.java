package pxb.xjimple.expr;

import org.objectweb.asm.Type;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class NewArrayExpr extends Value {

    public ValueBox size;
    public Type baseType;

    public NewArrayExpr(Type type, Value size) {
        super(VT.NEW_ARRAY);
        this.size = new ValueBox(size);
        this.baseType = type;
    }

    public String toString() {
        return "new " + baseType.getClassName() + "[" + size + "]";
    }
}
