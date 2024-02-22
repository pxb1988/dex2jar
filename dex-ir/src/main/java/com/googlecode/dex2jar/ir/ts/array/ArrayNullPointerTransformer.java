package com.googlecode.dex2jar.ir.ts.array;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.expr.Value.E1Expr;
import com.googlecode.dex2jar.ir.expr.Value.E2Expr;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Transformer;
import java.util.ArrayList;
import java.util.List;

/**
 * run after {@link com.googlecode.dex2jar.ir.ts.ConstTransformer}, to deal with following code
 *
 * <pre>
 * int[] a = null;
 * int b = a[1];
 * </pre>
 * <p>
 * replace {@code int b = a[1];} to {@code throw new NullPointException()}, and we get
 *
 * <pre>
 * int[] a = null;
 * throw new NullPointException();
 * </pre>
 *
 * @author Panxiaobo
 */
public class ArrayNullPointerTransformer implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        Stmt p = irMethod.stmts.getFirst();
        while (p != null) {
            if (arrayNPE(p)) {
                Stmt q = p.getNext();
                replaceNPE(irMethod.stmts, irMethod.locals, p);
                p = q;
                continue;
            }
            p = p.getNext();
        }
    }

    private void replaceNPE(StmtList stmts, List<Local> locals, Stmt p) {
        List<Value> values = new ArrayList<>();
        switch (p.et) {
        case E1:
            tryAdd(((E1Stmt) p).op.trim(), values);
            break;
        case E2:
            E2Stmt e2 = (E2Stmt) p;
            switch (e2.op1.trim().vt) {
            case LOCAL:
                tryAdd(e2.op2.trim(), values);
                break;
            case ARRAY:
                ArrayExpr ae = (ArrayExpr) e2.op1.trim();
                if (tryAdd(ae.op1.trim(), values)) {
                    if (tryAdd(ae.op2.trim(), values)) {
                        tryAdd(e2.op2.trim(), values);
                    }
                }
                break;
            case FIELD:// putfield
                FieldExpr fe = (FieldExpr) e2.op1.trim();
                if (fe.op == null || fe.op.trim() == null || tryAdd(fe.op.trim(), values)) {
                    tryAdd(e2.op2.trim(), values);
                }
                break;
            default:
                if (tryAdd(e2.op2.trim(), values)) {
                    tryAdd(e2.op1.trim(), values);
                }
            }
        default:
        }
        for (Value value : values) {
            switch (value.vt) {
            case CONSTANT:
            case LOCAL:
                break;
            default:
                Local n = Exprs.nLocal("xxx");
                locals.add(n);
                stmts.insertBefore(p, Stmts.nAssign(n, value));
            }
        }
        stmts.insertBefore(p,
                Stmts.nThrow(Exprs.nInvokeNew(new Value[0], new String[0], "Ljava/lang/NullPointerException;")));
        stmts.remove(p);
    }

    private boolean tryAdd(Value value, List<Value> values) {
        if (!arrayNPE(value)) {
            values.add(value);
            return true;
        } else {
            switch (value.et) {
            case E0:
                values.add(value);
                break;
            case E1:
                E1Expr e1 = (E1Expr) value;
                if (e1.op == null || e1.op.trim() == null) {
                    return false;
                }
                tryAdd(e1.op.trim(), values);
                break;
            case E2:
                E2Expr e2 = (E2Expr) value;
                if (e2.vt == VT.ARRAY && e2.op1.trim().vt == VT.CONSTANT) {
                    Constant cst = (Constant) e2.op1.trim();
                    if (cst.value.equals(0)) {
                        tryAdd(e2.op2.trim(), values);
                        return false;
                    }
                }
                if (tryAdd(e2.op1.trim(), values)) {
                    tryAdd(e2.op2.trim(), values);
                }

            case En:
                for (Value vb : ((EnExpr) value).ops) {
                    if (!tryAdd(vb.trim(), values)) {
                        break;
                    }
                }
            default:
                break;
            }
        }
        return false;
    }

    private boolean arrayNPE(Stmt p) {
        switch (p.et) {
        case E1:
            if (p.st == ST.GOTO) {
                return false;
            }
            return arrayNPE(((E1Stmt) p).op.trim());
        case E2:
            E2Stmt e2 = (E2Stmt) p;
            switch (e2.op1.trim().vt) {
            case ARRAY:
            case FIELD:
                return arrayNPE(e2.op1.trim()) || arrayNPE(e2.op2.trim());
            default:
                return arrayNPE(e2.op2.trim()) || arrayNPE(e2.op1.trim());
            }
        default:
            return false;
        }
    }

    private boolean arrayNPE(Value value) {
        switch (value.et) {
        case E1:
            E1Expr e1 = (E1Expr) value;
            if (e1.op == null || e1.op.trim() == null) {
                return false;
            }
            return arrayNPE(e1.op.trim());
        case E2:
            E2Expr e2 = (E2Expr) value;
            if (e2.vt == VT.ARRAY && e2.op1.trim().vt == VT.CONSTANT) {
                Constant cst = (Constant) e2.op1.trim();
                if (cst.value.equals(0)) {
                    return true;
                }
            }
            return arrayNPE(e2.op1.trim()) || arrayNPE(e2.op2.trim());

        case En:
            for (Value vb : ((EnExpr) value).ops) {
                if (arrayNPE(vb.trim())) {
                    return true;
                }
            }
        default:
            return false;
        }
    }

}
