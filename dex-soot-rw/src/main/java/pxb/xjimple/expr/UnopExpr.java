package pxb.xjimple.expr;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class UnopExpr extends Value {

    public ValueBox op;

    /**
     * @param type
     * @param value
     */
    public UnopExpr(VT type, Value value) {
        super(type);
        this.op = new ValueBox(value);
    }

    public String toString() {
        switch (vt) {
        case LENGTH:
            return op + ".length";
        case NEG:
            return "(-" + op + ")";
        }
        return super.toString();
    }

}
