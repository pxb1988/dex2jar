/*
 * Copyright (c) 2009-2012 Panxiaobo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.PhiExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;

import java.util.*;

public class DeadCodeTransformer implements Transformer {
    @Override
    public void transform(IrMethod method) {
        Cfg.createCFG(method);
        Cfg.dfsVisit(method, null);
        if (method.traps != null) {
            for (Iterator<Trap> it = method.traps.iterator(); it.hasNext();) {
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
                        t.handlers = labelStmts.toArray(new LabelStmt[labelStmts.size()]);
                        t.types = types.toArray(new String[types.size()]);
                    }
                }
            }
        }
        Set<Local> definedLocals = new HashSet<>();
        for (Iterator<Stmt> it = method.stmts.iterator(); it.hasNext();) {
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
        if (method.phiLabels != null) {
            for (Iterator<LabelStmt> it = method.phiLabels.iterator(); it.hasNext();) {
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
            for (Iterator<LabelStmt> it = method.phiLabels.iterator(); it.hasNext();) {
                LabelStmt labelStmt = it.next();
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
                            phiExpr.setOps(tmp.toArray(new Value[tmp.size()]));
                            tmp.clear();
                        }
                    }
                }
            }
        }
    }
}
