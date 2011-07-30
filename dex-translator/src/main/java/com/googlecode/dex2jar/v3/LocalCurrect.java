package com.googlecode.dex2jar.v3;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.ts.LocalType;
import com.googlecode.dex2jar.ir.ts.Transformer;

public class LocalCurrect implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        // type correct
        for (Stmt st : irMethod.stmts) {
            switch (st.et) {
            case E0:
            case En:
                break;
            case E1:
                E1Stmt s1 = (E1Stmt) st;
                currectInExpr(s1.op);
                break;
            case E2:
                E2Stmt s2 = (E2Stmt) st;
                currectInExpr(s2.op1);
                currectInExpr(s2.op2);
                break;
            }
        }
    }

    private void currectInExpr(ValueBox vb) {
        if (vb == null) {
            return;
        }
        Value value = vb.value;
        switch (value.et) {
        case E0:
            if (value.vt == VT.CONSTANT) {
                Constant cstExpr = ((Constant) value);
                Type type = LocalType.type(value);
                if (type == null) {

                    LocalType.type(value, Type.INT_TYPE);
                    type = Type.INT_TYPE;
                }
                Object cst = cstExpr.value;
                switch (type.getSort()) {
                case Type.INT:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.LONG:
                case Type.BOOLEAN:
                    break;
                case Type.FLOAT:
                    if (cst instanceof Integer) {
                        cstExpr.value = Float.intBitsToFloat(((Integer) cst).intValue());
                    }
                    break;

                case Type.DOUBLE:
                    if (cst instanceof Long) {
                        cstExpr.value = Double.longBitsToDouble(((Long) cst).longValue());
                    }
                    break;
                default:// null or class
                    if (cst instanceof Integer && ((Integer) cst).intValue() == 0) {// null
                        cstExpr.value = Constant.Null;
                    }
                    break;
                }
            }
            break;
        case E1:
            currectInExpr(((E1Expr) value).op);
            break;
        case E2:
            E2Expr e2 = (E2Expr) value;
            currectInExpr(e2.op1);
            currectInExpr(e2.op2);
            break;
        case En:
            EnExpr en = (EnExpr) value;
            for (ValueBox op : en.ops) {
                currectInExpr(op);
            }
            break;
        }
    }
}
