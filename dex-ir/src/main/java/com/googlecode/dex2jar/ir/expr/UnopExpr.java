package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.ValueBox;

public class UnopExpr extends E1Expr {

    /**
     * @param type
     * @param value
     */
    public UnopExpr(VT type, Value value) {
        super(type, new ValueBox(value));
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
