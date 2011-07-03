package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.ValueBox;

public class NewMutiArrayExpr extends EnExpr {

    public Type baseType;
    public int dimension;

    public NewMutiArrayExpr(Type base, int dimension, ValueBox[] sizes) {
        super(VT.NEW_MUTI_ARRAY, sizes);
        this.baseType = base;
        this.dimension = dimension;
        this.ops = new ValueBox[sizes.length];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(baseType);
        for (int i = 0; i < dimension; i++) {
            sb.append('[').append(ops[i]).append(']');
        }
        return sb.toString();
    }

}
