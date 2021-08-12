package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LocalVar;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.Stmts;

/**
 * the {@link LocalVar#reg} in {@link LocalVar} may be replaced by a constant value in {@link ConstTransformer}. This
 * class try to insert a new local before {@link LocalVar#start}.
 *
 * <p>
 * before:
 * </p>
 *
 * <pre>
 *   ...
 * L0:
 *   return a0
 * L1:
 * ======
 * .var L0 ~ L1 1 -> test // int
 * </pre>
 * <p>
 * after:
 * </p>
 *
 * <pre>
 *   ...
 *   d1 = 1
 * L0:
 *   return a0
 * L1:
 * ======
 * .var L0 ~ L1 d1 -> test // int
 * </pre>
 *
 * @author Panxiaobo
 */
public class FixVar implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        int i = 0;
        for (LocalVar var : irMethod.vars) {
            if (var.reg.trim().vt != VT.LOCAL) {
                if (var.reg.trim().vt == VT.CONSTANT) {
                    Local n = new Local(i++);
                    Value old = var.reg.trim();
                    irMethod.stmts.insertBefore(var.start, Stmts.nAssign(n, old));
                    var.reg = n;
                    irMethod.locals.add(n);
                } /* else {
                    // throw new DexException("not supported");
                }*/
            }
        }
    }

}
