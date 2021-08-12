package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LocalVar;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.stmt.BaseSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Clean unused {@link LabelStmt}
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class CleanLabel implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        Set<LabelStmt> uselabels = new HashSet<>();
        addTrap(irMethod.traps, uselabels);
        addVars(irMethod.vars, uselabels);
        addStmt(irMethod.stmts, uselabels);
        if (irMethod.phiLabels != null) {
            uselabels.addAll(irMethod.phiLabels);
        }
        rmUnused(irMethod.stmts, uselabels);
    }

    private void addVars(List<LocalVar> vars, Set<LabelStmt> uselabels) {
        if (vars != null) {
            for (LocalVar var : vars) {
                uselabels.add(var.start);
                uselabels.add(var.end);
            }
        }

    }

    private void rmUnused(StmtList stmts, Set<LabelStmt> uselabels) {
        Stmt p = stmts.getFirst();
        while (p != null) {
            if (p.st == ST.LABEL) {
                if (!uselabels.contains(p)) {
                    Stmt q = p.getNext();
                    stmts.remove(p);
                    p = q;
                    continue;
                }
            }
            p = p.getNext();
        }
    }

    private void addStmt(StmtList stmts, Set<LabelStmt> labels) {
        for (Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
            if (p instanceof JumpStmt) {
                labels.add(((JumpStmt) p).getTarget());
            } else if (p instanceof BaseSwitchStmt) {
                BaseSwitchStmt stmt = (BaseSwitchStmt) p;
                labels.add(stmt.defaultTarget);
                Collections.addAll(labels, stmt.targets);
            }
        }
    }

    private void addTrap(List<Trap> traps, Set<LabelStmt> labels) {
        if (traps != null) {
            for (Trap trap : traps) {
                labels.add(trap.start);
                labels.add(trap.end);
                Collections.addAll(labels, trap.handlers);
            }
        }
    }

}
