package pxb.xjimple.expr;

import org.objectweb.asm.Type;

import pxb.xjimple.Value;

public class NewExpr extends Value {

    public Type type;

    public NewExpr(Type type) {
        super(VT.NEW);
        this.type = type;
    }

    public String toString() {
        return "new " + type.getClassName();
    }
}
