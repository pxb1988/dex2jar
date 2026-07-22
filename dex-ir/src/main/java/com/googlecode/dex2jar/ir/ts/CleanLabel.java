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
        Set<LabelStmt> useLabels = new HashSet<>();
        addTrap(irMethod.traps, useLabels);
        addVars(irMethod.vars, useLabels);
        addStmt(irMethod.stmts, useLabels);
        if (irMethod.phiLabels != null) {
            useLabels.addAll(irMethod.phiLabels);
        }
        addLineNumber(irMethod.stmts, useLabels);
        rmUnused(irMethod.stmts, useLabels);
    }

    private void addVars(List<LocalVar> vars, Set<LabelStmt> useLabels) {
        if (vars != null) {
            for (LocalVar var : vars) {
                useLabels.add(var.start);
                useLabels.add(var.end);
            }
        }

    }

    private void rmUnused(StmtList stmts, Set<LabelStmt> useLabels) {
        Stmt p = stmts.getFirst();
        while (p != null) {
            if (p instanceof LabelStmt && p.st == ST.LABEL) {
                if (!useLabels.contains(p)) {
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

    // fix https://github.com/pxb1988/dex2jar/issues/165
    private void addLineNumber(StmtList stmts, Set<LabelStmt> useLabels) {
        for (Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
            if (p instanceof LabelStmt && ((LabelStmt) p).lineNumber != -1) {
                useLabels.add((LabelStmt) p);
            }
        }
    }

}
