package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.ArrayList;
import java.util.List;

/**
 * Trim Exception handler.
 * <p>
 * before:
 *
 * <pre>
 * L1
 * STMTs
 * throwableSTMTs
 * STMTs
 * throwableSTMTs
 * L2
 * ...
 * L3
 * ...
 *
 * L1 - L2 : all &gt; L3
 *
 * </pre>
 * <p>
 * after:
 *
 * <pre>
 * L1
 * STMTs
 * L4
 * throwableSTMTs
 * L5
 * STMTs
 * L6
 * throwableSTMTs
 * L2
 * ...
 * L3
 * ...
 *
 * L4 - L5 : all &gt; L3
 * L6 - L2 : all &gt; L3
 * </pre>
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class ExceptionHandlerTrim implements Transformer {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void transform(IrMethod irMethod) {
        List<Trap> trips = irMethod.traps;
        irMethod.traps = new ArrayList();
        LabelAndLocalMapper map = new LabelAndLocalMapper() {
            @Override
            public LabelStmt map(LabelStmt label) {
                return label;
            }
        };
        for (Trap trap : trips) {
            Trap ntrap = trap.clone(map);
            int status = 0;
            for (Stmt p = trap.start.getNext(); p != trap.end; p = p.getNext()) {
                if (!Cfg.notThrow(p)) {
                    if (status == 0) {
                        Stmt pre = p.getPre();
                        if (pre == null || pre.st != ST.LABEL) {
                            pre = Stmts.nLabel();
                            irMethod.stmts.insertBefore(p, pre);
                        }
                        ntrap.start = (LabelStmt) pre;
                        status = 1;
                    } /*else {
                        // continue;
                    }*/
                } else if (status == 1) {
                    Stmt pre = p.getPre();
                    if (pre == null || pre.st != ST.LABEL) {
                        pre = Stmts.nLabel();
                        irMethod.stmts.insertBefore(p, pre);
                    }

                    ntrap.end = (LabelStmt) pre;
                    irMethod.traps.add(ntrap);
                    status = 0;
                    ntrap = trap.clone(map);
                }
            }
            if (status == 1) {
                ntrap.end = trap.end;
                irMethod.traps.add(ntrap);
            }
        }
    }

}
