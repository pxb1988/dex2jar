package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.AbstractInvokeExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;

/**
 * convert
 *
 * <pre>
 * a = b.get();
 * </pre>
 * <p>
 * to
 *
 * <pre>
 * b.get();
 * </pre>
 * <p>
 * if a is not used in other place.
 */
public class VoidInvokeTransformer extends StatedTransformer {

    @Override
    public boolean transformReportChanged(IrMethod method) {
        if (method.locals.size() == 0) {
            return false;
        }
        int[] reads = Cfg.countLocalReads(method);
        boolean changed = false;
        for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
            if (p.st == Stmt.ST.ASSIGN && p.getOp1().vt == Value.VT.LOCAL) {
                Local left = (Local) p.getOp1();
                if (reads[left.lsIndex] == 0) {
                    Value op2 = p.getOp2();
                    if (op2 instanceof AbstractInvokeExpr) {
                        method.locals.remove(left);
                        Stmt nVoidInvoke = Stmts.nVoidInvoke(op2);
                        method.stmts.replace(p, nVoidInvoke);
                        p = nVoidInvoke;
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public void transform(IrMethod method) {
        transformReportChanged(method);
    }

}
