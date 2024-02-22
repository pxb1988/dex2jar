package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.BaseSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Cfg.TravelCallBack;
import com.googlecode.dex2jar.ir.ts.an.AnalyzeValue;
import com.googlecode.dex2jar.ir.ts.an.BaseAnalyze;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Transform Stmt to SSA form and count local read
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class SSATransformer implements Transformer {

    private void cleanTagsAndReIndex(IrMethod method) {
        int i = 0;
        for (Local local : method.locals) {
            local.tag = null;
            local.lsIndex = i++;
        }
    }

    private void deleteDeadCode(IrMethod method) {
        Iterator<Stmt> it = method.stmts.iterator();
        while (it.hasNext()) {
            Stmt stmt = it.next();
            if (!stmt.visited && stmt.st != ST.LABEL) {
                it.remove();
            }
        }
    }

    private void replaceLocalsWithSSA(final IrMethod method) {
        final List<Local> locals = method.locals;
        locals.clear();
        StmtList stmts = method.stmts;

        TravelCallBack tcb = new TravelCallBack() {

            @Override
            public Value onAssign(Local a, AssignStmt as) {
                if (a.lsIndex < 0) {
                    locals.add(a);
                    return a;
                }
                SSAValue lsv = (SSAValue) a.tag;
                Local b = lsv.local;
                locals.add(b);
                return b;
            }

            @Override
            public Value onUse(Local a) {
                if (a.lsIndex < 0) {
                    return a;
                }
                SSAValue lsv = (SSAValue) a.tag;
                return lsv.local;
            }

        };
        Set<Value> froms = new HashSet<>();
        List<LabelStmt> phiLabels = new ArrayList<>();
        // 2. we are looking for Phis and insert Phi node to the code
        for (Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
            if (p.st == ST.LABEL) {
                LabelStmt labelStmt = (LabelStmt) p;
                List<AssignStmt> phis = null;
                SSAValue[] frame = (SSAValue[]) p.frame;
                if (frame != null) {
                    for (SSAValue v : frame) {
                        if (v == null || !v.used) {
                            continue;
                        }
                        if (v.parent != null) {
                            froms.add(v.parent.local);
                        }
                        if (v.otherParents != null) {
                            for (SSAValue parent : v.otherParents) {
                                froms.add(parent.local);
                            }
                        }
                        froms.remove(v.local);
                        if (phis == null) {
                            phis = new ArrayList<>();
                        }
                        locals.add(v.local);
                        phis.add(Stmts.nAssign(v.local, Exprs.nPhi(froms.toArray(new Value[0]))));
                        froms.clear();
                    }
                }
                labelStmt.phis = phis;
                if (phis != null) {
                    phiLabels.add(labelStmt);
                }
            } else {
                Cfg.travelMod(p, tcb, true);
            }
            p.frame = null;
        }
        if (!phiLabels.isEmpty()) {
            method.phiLabels = phiLabels;
        }
    }

    @Override
    public void transform(final IrMethod method) {

        boolean needSSA = prepare(method);
        if (needSSA) {
            // 1. analyze and build value graph
            new SSAAnalyze(method).analyze();
            // 2. delete dead code
            deleteDeadCode(method);
            // 3. replace locals with SSA-locals
            replaceLocalsWithSSA(method);
        }

        // 4. clean tags on Local
        cleanTagsAndReIndex(method);
    }

    private boolean prepare(final IrMethod method) {
        int index = Cfg.reIndexLocal(method);

        final int[] readCounts = new int[index];
        final int[] writeCounts = new int[index];
        Cfg.travel(method.stmts, new TravelCallBack() {
            @Override
            public Value onAssign(Local v, AssignStmt as) {
                writeCounts[v.lsIndex]++;
                return v;
            }

            @Override
            public Value onUse(Local v) {
                readCounts[v.lsIndex]++;
                return v;
            }
        }, true);

        boolean needTravel = false;
        boolean needSSAAnalyze = false;
        index = 0;
        List<Local> oldLocals = method.locals;
        List<Local> locals = new ArrayList<>(oldLocals);
        oldLocals.clear();

        for (Local local : locals) {
            int idx = local.lsIndex;
            int read = readCounts[idx];
            int write = writeCounts[idx];
            /*if (read > 0 && write == 0) {
                // TODO if we need throw exception ?
                // or the code is dead?
            }*/

            if (read != 0 || write != 0) {
                if (write <= 1) {
                    // no phi require
                    local.lsIndex = -1;
                    oldLocals.add(local);
                } else if (read == 0) {
                    local.lsIndex = -2;
                    needTravel = true;
                    // we are going to duplicate each usage of the local and add to method.locals,
                    // so not add the original local to method.locals
                } else {
                    needSSAAnalyze = true;
                    local.lsIndex = index++;
                    oldLocals.add(local);
                }
            }
        }
        if (needSSAAnalyze || needTravel) {
            Cfg.travelMod(method.stmts, new TravelCallBack() {

                @Override
                public Value onAssign(Local v, AssignStmt as) {
                    if (v.lsIndex == -1) {
                        return v;
                    } else if (v.lsIndex == -2) {
                        Local n = (Local) v.clone();
                        method.locals.add(n);
                        return n;
                    }
                    // others
                    return v.clone();
                }

                @Override
                public Value onUse(Local v) {
                    if (v.lsIndex == -1) {
                        return v;
                    }
                    return v.clone();
                }
            }, true);
        }
        return needSSAAnalyze;
    }

    static class SSAAnalyze extends BaseAnalyze<SSAValue> {

        public int nextIndex;

        SSAAnalyze(IrMethod method) {
            super(method, false);
        }

        @Override
        protected void afterExec(SSAValue[] frame, Stmt stmt) {
            if (!DEBUG) {
                // remove frame to save memory
                if (stmt.cfgFroms.size() < 2) {
                    // we only care stmt only has one or less parent,
                    // the parent must be visited already.
                    // if more than 1 parent, the other may not been visited at
                    // the moment
                    setFrame(stmt, null);
                }
            }
        }

        @Override
        public Local onUse(Local local) {
            if (local.lsIndex < 0) {
                return local;
            }
            return super.onUse(local);
        }

        @Override
        public Local onAssign(Local local, AssignStmt as) {
            if (local.lsIndex < 0) {
                return local;
            }
            return super.onAssign(local, as);
        }

        @Override
        protected void analyzeValue() {
            Set<SSAValue> set = markUsed();
            aValues.clear();
            aValues = null;
            if (DEBUG) {
                clearLsEmptyValueFromFrame();
            }
            for (SSAValue v0 : set) {
                if (v0.used && v0.local == null) {
                    v0.local = new Local(nextIndex++);
                }
            }
        }

        protected void clearLsEmptyValueFromFrame() {
            for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
                SSAValue[] frame = (SSAValue[]) p.frame;
                if (frame != null) {
                    for (int i = 0; i < frame.length; i++) {
                        SSAValue r = frame[i];
                        if (r != null && !r.used) {
                            frame[i] = null;
                        }
                    }
                }
            }
        }

        @Override
        protected void init() {
            super.init();
            nextIndex = method.locals.size();
        }

        @Override
        protected void initCFG() {
            Cfg.createCFG(this.method);
        }

        protected Set<SSAValue> markUsed() {
            Set<SSAValue> used = new HashSet<>(aValues.size() / 2);
            Queue<SSAValue> q = new UniqueQueue<>();
            q.addAll(aValues);
            while (!q.isEmpty()) {
                SSAValue v = q.poll();
                if (v.used) {
                    used.add(v);
                    {
                        SSAValue p = v.parent;
                        if (p != null) {
                            if (!p.used) {
                                p.used = true;
                                q.add(p);
                            }
                        }
                    }
                    if (v.otherParents != null) {
                        for (SSAValue p : v.otherParents) {
                            if (!p.used) {
                                p.used = true;
                                q.add(p);
                            }
                        }
                    }

                }
            }
            return used;
        }

        @Override
        public SSAValue[] merge(SSAValue[] frame, SSAValue[] distFrame, Stmt src, Stmt dist) {
            if (distFrame != null) {
                relationMerge(frame, dist, distFrame);
            } else {
                if (dist.cfgFroms.size() > 1) { // detail mode
                    distFrame = newFrame();
                    relationMerge(frame, dist, distFrame);
                } else if (needCopyFrame(src)) {
                    distFrame = newFrame();
                    System.arraycopy(frame, 0, distFrame, 0, distFrame.length);
                } else {
                    distFrame = frame;
                }
            }
            return distFrame;
        }

        private static boolean needCopyFrame(Stmt src) {
            int c = 0;
            if (src.exceptionHandlers != null) {
                c += src.exceptionHandlers.size();
                if (c > 1) {
                    return true;
                }
            }
            if (src.st.canContinue()) {
                c += 1;
                if (c > 1) {
                    return true;
                }
            }
            if (src.st.canBranch()) {
                c += 1;
                if (c > 1) {
                    return true;
                }
            }
            if (src.st.canSwitch()) {
                c += 1;
                BaseSwitchStmt bss = (BaseSwitchStmt) src;
                c += bss.targets.length;
            }
            return c > 1;
        }

        @Override
        protected SSAValue[] newFrame(int size) {
            return new SSAValue[size];
        }

        @Override
        protected SSAValue newValue() {
            return new SSAValue();
        }

        @Override
        protected SSAValue onAssignLocal(Local local, Value value) {
            SSAValue aValue = newValue();
            aValue.local = local;
            local.tag = aValue;
            return aValue;
        }

        @Override
        protected void onUseLocal(SSAValue aValue, Local local) {
            local.tag = aValue;
            aValue.used = true;
        }

        protected void relationMerge(SSAValue[] frame, Stmt dist, SSAValue[] distFrame) {
            for (int i = 0; i < localSize; i++) {
                SSAValue srcValue = frame[i];
                if (srcValue != null) {
                    SSAValue distValue = distFrame[i];
                    if (distValue == null) {
                        if (!dist.visited) {
                            distValue = newValue();
                            aValues.add(distValue);
                            distFrame[i] = distValue;
                            linkParentChildren(srcValue, distValue);
                        }
                    } else {
                        linkParentChildren(srcValue, distValue);
                    }
                }
            }
        }

        private void linkParentChildren(SSAValue p, SSAValue c) {
            if (c.parent == null) {
                c.parent = p;
            } else if (c.parent != p) {
                Set<SSAValue> ps = c.otherParents;
                if (ps == null) {
                    ps = new HashSet<>(3);
                    c.otherParents = ps;
                }
                ps.add(p);
            }
        }

    }

    private static class SSAValue implements AnalyzeValue {

        public Local local;

        public Set<SSAValue> otherParents;

        public boolean used = false;

        public SSAValue parent;

        @Override
        public char toRsp() {
            return used ? 'x' : '.';
        }

        @Override
        public String toString() {
            if (local != null) {
                return local.toString();
            } else {
                return "N";
            }
        }

    }

}
