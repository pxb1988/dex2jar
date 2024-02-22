package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.PhiExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DeadCodeTransformer implements Transformer {

    @Override
    public void transform(IrMethod method) {
        Cfg.createCFG(method);
        Cfg.dfsVisit(method, null);
        if (method.traps != null) {
            Iterator<Trap> it = method.traps.iterator();
            while (it.hasNext()) {
                Trap t = it.next();
                boolean allNotThrow = true;
                for (Stmt p = t.start; p != t.end; p = p.getNext()) {
                    if (p.visited && Cfg.isThrow(p)) {
                        allNotThrow = false;
                        break;
                    }
                }
                if (allNotThrow) {
                    it.remove();
                    continue;
                }

                boolean allNotVisited = true;
                boolean allVisited = true;
                for (LabelStmt labelStmt : t.handlers) {
                    if (labelStmt.visited) {
                        allNotVisited = false;
                    } else {
                        allVisited = false;
                    }
                }
                if (allNotVisited) {
                    it.remove();
                } else {
                    // keep start and end
                    t.start.visited = true;
                    t.end.visited = true;
                    if (!allVisited) { // part visited
                        List<String> types = new ArrayList<>(t.handlers.length);
                        List<LabelStmt> labelStmts = new ArrayList<>(t.handlers.length);
                        for (int i = 0; i < t.handlers.length; i++) {
                            labelStmts.add(t.handlers[i]);
                            types.add(t.types[i]);
                        }
                        t.handlers = labelStmts.toArray(new LabelStmt[0]);
                        t.types = types.toArray(new String[0]);
                    }
                }
            }
        }
        Set<Local> definedLocals = new HashSet<>();
        {
            Iterator<Stmt> it = method.stmts.iterator();
            while (it.hasNext()) {
                Stmt p = it.next();
                if (!p.visited) {
                    it.remove();
                    continue;
                }
                if (p.st == Stmt.ST.ASSIGN || p.st == Stmt.ST.IDENTITY) {
                    if (p.getOp1().vt == Value.VT.LOCAL) {
                        definedLocals.add((Local) p.getOp1());
                    }
                }
            }
        }
        if (method.phiLabels != null) {
            Iterator<LabelStmt> it = method.phiLabels.iterator();
            while (it.hasNext()) {
                LabelStmt labelStmt = it.next();
                if (!labelStmt.visited) {
                    it.remove();
                    continue;
                }
                if (labelStmt.phis != null) {
                    for (AssignStmt phi : labelStmt.phis) {
                        definedLocals.add((Local) phi.getOp1());
                    }
                }
            }
        }

        method.locals.clear();
        method.locals.addAll(definedLocals);
        Set<Value> tmp = new HashSet<>();
        if (method.phiLabels != null) {
            for (LabelStmt labelStmt : method.phiLabels) {
                if (labelStmt.phis != null) {
                    for (AssignStmt phi : labelStmt.phis) {
                        PhiExpr phiExpr = (PhiExpr) phi.getOp2();
                        boolean needRebuild = false;
                        for (Value v : phiExpr.getOps()) {
                            if (!definedLocals.contains(v)) {
                                needRebuild = true;
                                break;
                            }
                        }
                        if (needRebuild) {
                            for (Value v : phiExpr.getOps()) {
                                if (definedLocals.contains(v)) {
                                    tmp.add(v);
                                }
                            }
                            phiExpr.setOps(tmp.toArray(new Value[0]));
                            tmp.clear();
                        }
                    }
                }
            }
        }
    }

}
