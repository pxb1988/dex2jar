package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
        Iterator<AssignStmt> it = assignStmtList.iterator();
        while (it.hasNext()) {
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
                    phi.getOp2().setOps(set.toArray(new Value[0]));
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
            Iterator<LabelStmt> itLabel = phiLabels.iterator();
            while (itLabel.hasNext()) {
                LabelStmt labelStmt = itLabel.next();
                Iterator<AssignStmt> it = labelStmt.phis.iterator();
                while (it.hasNext()) {
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
                if (labelStmt.phis.isEmpty()) {
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
            Iterator<LabelStmt> itLabel = phiLabels.iterator();
            while (itLabel.hasNext()) {
                LabelStmt labelStmt = itLabel.next();
                labelStmt.phis.removeIf(phi -> toDeletePhiAssign.contains(phi.getOp1()));
                if (labelStmt.phis.isEmpty()) {
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
        List<Map.Entry<Local, T>> set = new ArrayList<>(toReplace.entrySet());
        set.sort(Comparator.comparingInt(localTEntry -> localTEntry.getKey().lsIndex));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<Local, T> e : set) {
                T b = e.getValue();
                if (b instanceof Local) {
                    T n = toReplace.get(b);
                    if (n != null && b != n) {
                        changed = true;
                        e.setValue(n);
                    }
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

            while (simplePhi(phiLabels, toReplace, set)) { // remove a = phi(b)
                fixReplace(toReplace);
                replacePhi(phiLabels, toReplace, set);
            }
            while (simpleAssign(phiLabels, assignStmtList, toReplace, method.stmts)) { // remove a=b
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
        if (!toReplace.isEmpty()) {
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
