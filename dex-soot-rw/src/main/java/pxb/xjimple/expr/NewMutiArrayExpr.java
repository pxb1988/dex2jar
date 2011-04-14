package pxb.xjimple.expr;

import org.objectweb.asm.Type;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class NewMutiArrayExpr extends Value {

    public Type baseType;
    public int dimension;
    public ValueBox[] sizes;

    public NewMutiArrayExpr(Type base, int dimension, Value[] sizes) {
        super(VT.NEW_MUTI_ARRAY);
        this.baseType = base;
        this.dimension = dimension;
        this.sizes = new ValueBox[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            this.sizes[i] = new ValueBox(sizes[i]);
        }
    }
}
