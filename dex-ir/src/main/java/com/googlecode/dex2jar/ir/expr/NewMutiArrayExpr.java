package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;


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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(baseType);
        for (int i = 0; i < dimension; i++) {
            sb.append('[').append(sizes[i]).append(']');
        }
        return sb.toString();
    }

}
