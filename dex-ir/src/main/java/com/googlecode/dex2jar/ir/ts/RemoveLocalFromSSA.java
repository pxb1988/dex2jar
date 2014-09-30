/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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

import java.util.*;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;

public class RemoveLocalFromSSA extends StatedTransformer {
    static <T extends Value> void replaceAssign(List<AssignStmt> assignStmtList, Map<Local, T> toReplace) {
        for (AssignStmt as : assignStmtList) {
            Value right = as.getOp2();
            T to = toReplace.get(right);
            if (to != null) {
                as.setOp2(to);
            }
        }
    }

    private boolean simpleAssign(List<LabelStmt> phiLabels, List<AssignStmt> assignStmtList,
                                 Map<Local, Local> toReplace, StmtList stmts) {
        Set<Value> usedInPhi = new HashSet<>();
        if (phiLabels != null) {
            for (LabelStmt labelStmt : phiLabels) {
                for (AssignStmt phi : labelStmt.phis) {
                    usedInPhi.addAll(Arrays.asList(phi.getOp2().getOps()));
                }
            }
        }
        boolean changed = false;
        for (Iterator<AssignStmt> it = assignStmtList.iterator(); it.hasNext(); ) {
            AssignStmt as = it.next();
            if (!usedInPhi.contains(as.getOp1())) {
                it.remove();
                stmts.remove(as);
                toReplace.put((Local) as.getOp1(), (Local) as.getOp2());
                changed = true;
            }
        }

        return changed;
    }

    private void replacePhi(List<LabelStmt> phiLabels, Map<Local, Local> toReplace, Set<Value> set) {
        if (phiLabels != null) {
            for (LabelStmt labelStmt : phiLabels) {
                for (AssignStmt phi : labelStmt.phis) {
                    Value[] ops = phi.getOp2().getOps();
                    for (Value op : ops) {
                        Value n = toReplace.get(op);
                        if (n != null) {
                            set.add(n);
                        } else {
                            set.add(op);
                        }
                    }
                    set.remove(phi.getOp1());
                    phi.getOp2().setOps(set.toArray(new Value[set.size()]));
                    set.clear();
                }
            }
        }
    }

    static class PhiObject {
        Set<PhiObject> parent = new HashSet<>();
        Set<PhiObject> children = new HashSet<>();
        Local local;
        boolean isInitByPhi = false;
    }

    public static PhiObject getOrCreate(Map<Local, PhiObject> map, Local local) {
        PhiObject po = map.get(local);
        if (po == null) {
            po = new PhiObject();
            po.local = local;
            map.put(local, po);
        }
        return po;
    }

    public static void linkPhiObject(PhiObject parent, PhiObject child) {
        parent.children.add(child);
        child.parent.add(parent);
    }


    private boolean simplePhi(List<LabelStmt> phiLabels, Map<Local, Local> toReplace, Set<Value> set) {
        boolean changed = false;
        if (phiLabels != null) {
            for (Iterator<LabelStmt> itLabel = phiLabels.iterator(); itLabel.hasNext(); ) {
                LabelStmt labelStmt = itLabel.next();
                for (Iterator<AssignStmt> it = labelStmt.phis.iterator(); it.hasNext(); ) {
                    AssignStmt phi = it.next();
                    set.addAll(Arrays.asList(phi.getOp2().getOps()));
                    set.remove(phi.getOp1());
                    if (set.size() == 1) {
                        it.remove();
                        changed = true;
                        toReplace.put((Local) phi.getOp1(), (Local) set.iterator().next());
                    }
                    set.clear();
                }
                if (labelStmt.phis.size() == 0) {
                    labelStmt.phis = null;
                    itLabel.remove();
                }
            }
        }
        return changed;
    }

    private boolean removeLoopFromPhi(List<LabelStmt> phiLabels, Map<Local, Local> toReplace) {
        boolean changed = false;
        if (phiLabels != null) {
            Set<Local> toDeletePhiAssign = new HashSet<>();
            Map<Local, PhiObject> phis;
            // detect loop init in phi
            phis = collectPhiObjects(phiLabels);
            Queue<PhiObject> q = new UniqueQueue<>();
            q.addAll(phis.values());
            while (!q.isEmpty()) {
                PhiObject po = q.poll();
                for (PhiObject child : po.children) {
                    if (child.isInitByPhi) {
                        if (child.parent.addAll(po.parent)) {
                            q.add(child);
                        }
                    }
                }
            }
            for (PhiObject po : phis.values()) {
                if (po.isInitByPhi) {
                    Local local = null;
                    for (PhiObject p : po.parent) {
                        if (!p.isInitByPhi) {
                            if (local == null) { // the first non-phi value
                                local = p.local;
                            } else {
                                local = null;
                                break;
                            }
                        }
                    }
                    if (local != null) {
                        toReplace.put(po.local, local);
                        toDeletePhiAssign.add(po.local);
                        changed = true;
                    }
                }
            }
            for (Iterator<LabelStmt> itLabel = phiLabels.iterator(); itLabel.hasNext(); ) {
                LabelStmt labelStmt = itLabel.next();
                for (Iterator<AssignStmt> it = labelStmt.phis.iterator(); it.hasNext(); ) {
                    AssignStmt phi = it.next();
                    if (toDeletePhiAssign.contains(phi.getOp1())) {
                        it.remove();
                    }
                }
                if (labelStmt.phis.size() == 0) {
                    labelStmt.phis = null;
                    itLabel.remove();
                }
            }
        }
        return changed;
    }

    private Map<Local, PhiObject> collectPhiObjects(List<LabelStmt> phiLabels) {
        Map<Local, PhiObject> phis;
        phis = new HashMap<>();
        for (LabelStmt labelStmt : phiLabels) {
            for (AssignStmt as : labelStmt.phis) {
                Local local = (Local) as.getOp1();
                PhiObject child = getOrCreate(phis, local);
                child.isInitByPhi = true;
                for (Value op : as.getOp2().getOps()) {
                    if (op == local) {
                        continue;
                    }
                    PhiObject parent = getOrCreate(phis, (Local) op);
                    linkPhiObject(parent, child);
                }
            }
        }
        return phis;
    }

    static <T> void fixReplace(Map<Local, T> toReplace) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<Local, T> e : toReplace.entrySet()) {
                T b = e.getValue();
                T n = toReplace.get(b);
                if (n != null && b != n) {
                    changed = true;
                    e.setValue(n);
                }
            }
        }
    }

    @Override
    public boolean transformReportChanged(IrMethod method) {
        boolean irChanged = false;
        List<AssignStmt> assignStmtList = new ArrayList<>();
        List<LabelStmt> phiLabels = method.phiLabels;
        for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
            if (p.st == Stmt.ST.ASSIGN) {
                AssignStmt as = (AssignStmt) p;
                if (as.getOp1().vt == Value.VT.LOCAL && as.getOp2().vt == Value.VT.LOCAL) {
                    assignStmtList.add(as);
                }
            }
        }
        final Map<Local, Local> toReplace = new HashMap<>();
        Set<Value> set = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;

            if (removeLoopFromPhi(phiLabels, toReplace)) {
                fixReplace(toReplace);
                replacePhi(phiLabels, toReplace, set);
            }

            while (simplePhi(phiLabels, toReplace, set)) {// remove a = phi(b)
                fixReplace(toReplace);
                replacePhi(phiLabels, toReplace, set);
            }
            while (simpleAssign(phiLabels, assignStmtList, toReplace, method.stmts)) {// remove a=b
                fixReplace(toReplace);
                replaceAssign(assignStmtList, toReplace);
                changed = true;
                irChanged = true;
            }
            replacePhi(phiLabels, toReplace, set);
        }

        for (Local local : toReplace.keySet()) {
            method.locals.remove(local);
            irChanged = true;
        }
        if (toReplace.size() > 0) {
            Cfg.travelMod(method.stmts, new Cfg.TravelCallBack() {
                @Override
                public Value onAssign(Local v, AssignStmt as) {
                    return v;
                }

                @Override
                public Value onUse(Local v) {
                    Local n = toReplace.get(v);
                    return n == null ? v : n;
                }
            }, true);
        }
        return irChanged;
    }
}
