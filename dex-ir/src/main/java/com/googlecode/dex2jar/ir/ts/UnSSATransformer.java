package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.PhiExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.an.AnalyzeValue;
import com.googlecode.dex2jar.ir.ts.an.BaseAnalyze;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Remove {@link PhiExpr}s, add a=x to each CFG from.
 * <p>
 * TODO clean frame
 *
 * @author bob
 */
public class UnSSATransformer implements Transformer {

    private static final boolean DEBUG = false;

    protected static final Comparator<RegAssign> ORDER_REG_ASSIGN_BY_EXCLUDE_SIZE_DESC =
            (o1, o2) -> o2.excludes.size() - o1.excludes.size();

    public UnSSATransformer() {
        super();
    }

    /**
     * there is somewhere both a and its possible x is both live, insert a=x, will change the meaning for example
     *
     * <pre>
     *                      L0:
     *                      a = phi(b, ... )
     *                      b = 234;
     *                      if a>0 goto L0: // a, b both live here
     *                      ...
     * </pre>
     * <p>
     * after insert a=b before the if stmt, the programe change to
     *
     * <pre>
     *                      L0:
     *                      // a = phi(b, ... )
     *                      b = 234;
     *                      a = b
     *                      if a>0 goto L0:
     *                      ...
     * </pre>
     * <p>
     * the solution is by introduce a new local x
     *
     * <pre>
     *                      L0:
     *                      x = phi(b, ... )
     *                      a = x
     *                      b = 234;
     *                      if a>0 goto L0: // a, b both live here
     *                      ...
     * </pre>
     * <p>
     * insert x = b is ok now
     *
     * <pre>
     *                      L0:
     *                      // x = phi(b, ... )
     *                      a = x
     *                      b = 234;
     *                      x = b
     *                      if a>0 goto L0: // a, b both live here
     *                      ...
     * </pre>
     */
    private void fixPhi(IrMethod method, Collection<LabelStmt> phiLabels) {
        for (LabelStmt labelStmt : phiLabels) {
            List<AssignStmt> phis = labelStmt.phis;

            for (AssignStmt phi : phis) {

                Local a = (Local) phi.getOp1();
                PhiExpr b = (PhiExpr) phi.getOp2();
                boolean introduceNewLocal = false;
                RegAssign aReg = (RegAssign) a.tag;
                for (Value op : b.getOps()) {
                    RegAssign bReg = (RegAssign) op.tag;
                    if (aReg.excludes.contains(bReg)) {
                        introduceNewLocal = true;
                        break;
                    }
                }
                if (introduceNewLocal) {
                    Local newLocal = (Local) a.clone();
                    if (DEBUG) {
                        newLocal.debugName = "x" + method.locals.size();
                    }
                    phi.op1 = newLocal;
                    RegAssign newRegAssign = new RegAssign();
                    newLocal.tag = newRegAssign;

                    method.locals.add(newLocal);
                    Stmt newAssigStmt = Stmts.nAssign(a, newLocal);
                    Stmt next = labelStmt.getNext();
                    if (next != null && next.st == ST.IDENTITY && next.getOp2().vt == VT.EXCEPTION_REF) {
                        // it's a handler, insert after the exception ref
                        method.stmts.insertAfter(next, newAssigStmt);
                    } else {
                        method.stmts.insertAfter(labelStmt, newAssigStmt);
                    }
                    LiveV[] frame = (LiveV[]) labelStmt.frame;
                    if (DEBUG) {
                        LiveV[] copy = frame.clone();
                        LiveV n = new LiveV();
                        n.local = a;
                        n.used = true;
                        copy[a.lsIndex] = new LiveV();
                        newAssigStmt.frame = copy;
                    }
                    LiveV thePhi = frame[a.lsIndex];
                    thePhi.local = newLocal;
                    for (LiveV v : frame) {
                        if (v != null && v.used) {
                            RegAssign s = (RegAssign) v.local.tag;
                            s.excludes.add(newRegAssign);
                            newRegAssign.excludes.add(s);
                        }
                    }

                }
            }
        }
    }

    private void insertAssignPath(IrMethod method, Collection<LabelStmt> phiLabels) {
        // FIXME the phi in Exception handler is buggy
        List<AssignStmt> buff = new ArrayList<>();
        for (LabelStmt labelStmt : phiLabels) {
            List<AssignStmt> phis = labelStmt.phis;
            LiveV[] frame = (LiveV[]) labelStmt.frame;
            for (Stmt from : labelStmt.cfgFroms) {
                if (from.visited) { // at lease it is reached by cfg
                    for (AssignStmt phi : phis) {
                        Local a = (Local) phi.getOp1();
                        LiveV v = frame[a.lsIndex];
                        Local local = v.stmt2regMap.get(from);
                        if (local != a) {
                            buff.add(Stmts.nAssign(a, local));
                        }
                    }
                    insertAssignPath(method.stmts, from, labelStmt, buff);
                    buff.clear();
                }
            }
        }
    }

    private void insertAssignPath(StmtList stmts, Stmt from, LabelStmt labelStmt, List<AssignStmt> buff) {
        boolean insertBeforeFromStmt;
        if (from.exceptionHandlers != null && from.exceptionHandlers.contains(labelStmt)) {
            insertBeforeFromStmt = true;
        } else {
            switch (from.st) {
            case GOTO:
            case IF:
                JumpStmt jumpStmt = (JumpStmt) from;
                insertBeforeFromStmt = jumpStmt.getTarget().equals(labelStmt); //
                break;
            case TABLE_SWITCH:
            case LOOKUP_SWITCH:
                insertBeforeFromStmt = true;
                break;
            default:
                insertBeforeFromStmt = false;
                break;
            }
        }
        if (insertBeforeFromStmt) {
            for (AssignStmt as : buff) {
                stmts.insertBefore(from, as);
            }
        } else {
            for (AssignStmt as : buff) {
                stmts.insertAfter(from, as);
            }
        }
        LiveV[] frame = (LiveV[]) from.frame;
        List<LiveV> newLiveVs = new ArrayList<>(buff.size());
        for (AssignStmt as : buff) {
            Local left = (Local) as.getOp1();
            {
                LiveV liveV = new LiveV();
                liveV.local = left;
                liveV.used = true;
                newLiveVs.add(liveV);
            }
            RegAssign leftRegAssign = (RegAssign) left.tag;
            Local right = (Local) as.getOp2();
            int toSkip = right.lsIndex;
            for (int i = 0; i < frame.length; i++) {
                if (i == toSkip) {
                    continue;
                }
                LiveV v = frame[i];
                if (v != null && v.used) {
                    RegAssign assign = (RegAssign) v.local.tag;
                    assign.excludes.add(leftRegAssign);
                    leftRegAssign.excludes.add(assign);
                }
            }
            for (AssignStmt as2 : buff) {
                RegAssign assign = (RegAssign) as2.getOp1().tag;
                assign.excludes.add(leftRegAssign);
                leftRegAssign.excludes.add(assign);
            }
        }

        LiveV[] newFrame = new LiveV[frame.length + newLiveVs.size()];
        System.arraycopy(frame, 0, newFrame, 0, frame.length);
        for (int i = 0; i < newLiveVs.size(); i++) {
            newFrame[i + frame.length] = newLiveVs.get(i);
        }
    }

    @Override
    public void transform(IrMethod method) {
        if (method.phiLabels == null || method.phiLabels.isEmpty()) {
            return;
        }

        // fix issue in github 186
        // .?.....?.?.?.x?x.x..?x.?.??.x.... | L24e82f43: // [a15 = φ(a4, a23), a17 = φ(a5, a20)]
        // .?x....?.?.?.x?x.x..?x.?.??.x.... | L16e1a441: // [a2 = φ(a4, a15)]
        // the local a15 is fixed to x15 in fixPhi
        // .?.....?.?.?.x?x.x..?x.?.??.x.... | L24e82f43: // [x15 = φ(a4, a23), a17 = φ(a5, a20)]
        //                                   | a15 = x15
        // .?x....?.?.?.x?x.x..?x.?.??.x.... | L16e1a441: // [a2 = φ(a4, a15)]
        //  after that, when demote a2, it is inserted after L24e82f43, which will cause a15 undefined
        // .?.....?.?.?.x?x.x..?x.?.??.x.... | L24e82f43: // [x15 = φ(a4, a23), a17 = φ(a5, a20)]
        //                                   | a2 = a15
        //                                   | a15 = x15
        // .?x....?.?.?.x?x.x..?x.?.??.x.... | L16e1a441: // [a2 = φ(a4, a15)]

        // this is simple fix to github 186
        // insert a Nop between two LabelStmt if both have phis
        for (LabelStmt phiLabel : method.phiLabels) {
            Stmt stmt = phiLabel.getNext();
            if (stmt.st == ST.LABEL) {
                LabelStmt labelStmt2 = (LabelStmt) stmt;
                if (labelStmt2.phis != null && !labelStmt2.phis.isEmpty()) {
                    method.stmts.insertAfter(phiLabel, Stmts.nNop());
                }
            }
        }

        // 1. Live analyze the method,
        // a. remove Phi,
        // b. record parameter reference
        LiveA liveA = new LiveA(method);
        liveA.analyze();

        genRegGraph(method, liveA);

        // 2. insert x=y
        fixPhi(method, method.phiLabels);
        insertAssignPath(method, method.phiLabels);

        // 4. clean up
        for (Local local : method.locals) {
            local.tag = null;
        }
        for (Stmt stmt : method.stmts) {
            stmt.frame = null;
        }
        for (LabelStmt labelStmt : method.phiLabels) {
            labelStmt.phis = null;
        }
        method.phiLabels = null;
    }

    private void genRegGraph(IrMethod method, LiveA liveA) {
        for (Local local : method.locals) {
            local.tag = new RegAssign();
        }

        Set<Stmt> tos = new HashSet<>();
        for (Stmt stmt : method.stmts) {
            if ((stmt.st == ST.ASSIGN || stmt.st == ST.IDENTITY) && stmt.getOp1().vt == VT.LOCAL) {
                Local localAssignTo = (Local) stmt.getOp1();
                RegAssign regAssignTo = (RegAssign) localAssignTo.tag;
                Set<Integer> excludeIdx = new HashSet<>();
                Cfg.collectTos(stmt, tos);
                for (Stmt target : tos) {
                    LiveV[] frame = (LiveV[]) target.frame;
                    if (frame == null) {
                        continue;
                    }
                    // exclude thisReg and phiReg
                    excludeIdx.clear();
                    excludeIdx.add(localAssignTo.lsIndex);
                    if (target.st == ST.LABEL) {
                        LabelStmt label = (LabelStmt) target;
                        if (label.phis != null) {
                            for (AssignStmt phiAssignStmt : label.phis) {
                                Local phiLocal = (Local) phiAssignStmt.getOp1();
                                excludeIdx.add(phiLocal.lsIndex);
                            }
                        }
                    }
                    for (int i = 0; i < frame.length; i++) {
                        if (excludeIdx.contains(i)) {
                            continue;
                        }
                        LiveV v = frame[i];
                        if (v != null && v.used) {
                            RegAssign b = (RegAssign) v.local.tag;
                            regAssignTo.excludes.add(b);
                            b.excludes.add(regAssignTo);
                        }
                    }
                }
                tos.clear();
            } else if (stmt.st == ST.LABEL) { //
                // https://github.com/pxb1988/dex2jar/issues/477
                // the exception handler is dead and stmt.frame is null
                LabelStmt label = (LabelStmt) stmt;
                if (label.phis != null && stmt.frame != null) {
                    for (AssignStmt phiAssignStmt : label.phis) {
                        Local phiLocal = (Local) phiAssignStmt.getOp1();
                        RegAssign a = (RegAssign) phiLocal.tag;
                        LiveV[] frame = (LiveV[]) stmt.frame;
                        if (frame != null) {
                            for (LiveV v : frame) {
                                if (v != null && v.used) {
                                    RegAssign b = (RegAssign) v.local.tag;
                                    a.excludes.add(b);
                                    b.excludes.add(a);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            System.out.println(liveA.toString());
        }
    }

    protected static class LiveA extends BaseAnalyze<LiveV> {

        static Comparator<LiveV> sortByHopsASC = Comparator.comparingInt(arg0 -> arg0.hops);

        public LiveA(IrMethod method) {
            super(method);
        }

        @Override
        protected void analyzeValue() {
            markUsed();

            if (UnSSATransformer.DEBUG) {
                clearUnUsedFromFrame();
            }

        }

        protected void clearUnUsedFromFrame() {
            for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
                LiveV[] frame = (LiveV[]) p.frame;
                if (frame != null) {
                    for (int i = 0; i < frame.length; i++) {
                        LiveV r = frame[i];
                        if (r != null) {
                            if (!r.used) {
                                frame[i] = null;
                            }
                        }
                    }
                }
            }
        }

        protected Set<LiveV> markUsed() {
            Set<LiveV> used = new HashSet<>(aValues.size() / 2);
            Queue<LiveV> q = new UniqueQueue<>();
            q.addAll(aValues);

            while (!q.isEmpty()) {
                LiveV v = q.poll();
                if (v.used) {
                    if (used.contains(v)) {
                        continue;
                    }
                    used.add(v);
                    {
                        LiveV parent = v.parent;
                        if (parent != null && !parent.used) {
                            parent.used = true;
                            q.add(parent);
                        }
                    }
                    {
                        List<LiveV> otherParent = v.otherParents;
                        if (otherParent != null && !otherParent.isEmpty()) {
                            for (LiveV parent : otherParent) {
                                if (parent != null && !parent.used) {
                                    parent.used = true;
                                    q.add(parent);
                                }
                            }
                            v.otherParents = null;
                        }
                    }
                }
            }
            for (LiveV v : aValues) {
                v.parent = null;
            }
            aValues = null;

            return used;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public LiveV[] merge(LiveV[] srcFrame, LiveV[] distFrame, Stmt src, Stmt dist) {

            Map<Integer, AssignStmt> phiLives = new HashMap<>();
            if (dist.st == ST.LABEL) {
                LabelStmt label = (LabelStmt) dist;
                if (label.phis != null) { // we got phis here
                    // travel each phi assignment, find where the phiLocal from
                    for (AssignStmt phiAssignStmt : label.phis) {
                        Local phiLocal = (Local) phiAssignStmt.getOp1();
                        phiLives.put(phiLocal.lsIndex, phiAssignStmt);
                    }
                }
            }
            // relationship
            boolean firstMerge = false;
            if (distFrame == null) { // distFrame is not visited
                distFrame = newFrame(); // init the distFrame
                firstMerge = true;

                // merge each value to distFrame if value is not null;
                for (int i = 0; i < distFrame.length; i++) {
                    if (phiLives.containsKey(i)) { // skip phi
                        continue;
                    }
                    LiveV srcV = srcFrame[i];
                    if (srcV != null) {
                        LiveV distV = newValue();
                        aValues.add(distV);
                        distV.parent = srcV;
                        distV.hops = srcV.hops + 1;
                        distV.local = srcV.local;
                        distFrame[i] = distV;
                    }
                }
            }

            if (!firstMerge) {
                // skip merge phi
                for (int i = 0; i < distFrame.length; i++) {
                    if (phiLives.containsKey(i)) {
                        continue;
                    }
                    LiveV srcV = srcFrame[i];
                    LiveV distV = distFrame[i];
                    if (srcV != null && distV != null) {
                        if (distV.otherParents == null) {
                            distV.otherParents = new ArrayList(5);
                        }
                        distV.otherParents.add(srcV);
                    }
                }
            }

            // deal with phi
            for (AssignStmt phiAssignStmt : phiLives.values()) {
                Local phiLocal = (Local) phiAssignStmt.getOp1();

                LiveV distValue;
                if (firstMerge) {
                    distValue = new LiveV();
                    distValue.local = phiLocal;
                    distValue.stmt2regMap = new HashMap<>();
                    distFrame[phiLocal.lsIndex] = distValue;
                } else {
                    distValue = distFrame[phiLocal.lsIndex];
                }

                List<LiveV> liveVs = new ArrayList();

                LiveV possiblePhiLocal = srcFrame[phiLocal.lsIndex];
                if (possiblePhiLocal != null) {
                    liveVs.add(possiblePhiLocal);
                }

                for (Value p0 : phiAssignStmt.getOp2().getOps()) {
                    Local srcLocal = (Local) p0;
                    LiveV s = srcFrame[srcLocal.lsIndex];
                    if (s != null) {
                        liveVs.add(s);
                    }
                }
                liveVs.sort(sortByHopsASC);
                LiveV a = liveVs.get(0); // this value assign to
                // phiLocal in srcFrame
                a.used = true;
                distValue.stmt2regMap.put(src, a.local);
            }
            return distFrame;
        }

        @Override
        protected LiveV[] newFrame(int size) {
            return new LiveV[size];
        }

        @Override
        protected LiveV newValue() {
            return new LiveV();
        }

        @Override
        protected LiveV onAssignLocal(Local local, Value value) {
            LiveV v = super.onAssignLocal(local, value);
            v.local = local;
            v.used = true;
            return v;
        }

        @Override
        protected void onUseLocal(LiveV aValue, Local local) {
            aValue.used = true;
        }

    }

    private static class LiveV implements AnalyzeValue {

        public int hops;

        public Local local;

        public LiveV parent;

        public boolean used;

        public List<LiveV> otherParents;

        /**
         * for a Phi local, record where the this assigned from, for
         *
         * <pre>
         *     s0: a0=1
         *     s1: if x goto s3
         *     s2: a1=2
         *     s3: a= phi(a0, a1)
         * </pre>
         * <p>
         * the value is s0 : a0 s2 : a1
         */
        Map<Stmt, Local> stmt2regMap;

        @Override
        public char toRsp() {
            return used ? 'x' : '?';
        }

        @Override
        public String toString() {
            return local + "|" + hops;
        }

    }

    /**
     * designed for assign index to Local, each Object is related to a Local
     */
    protected static class RegAssign {

        /**
         * can not have same index with
         */
        public Set<RegAssign> excludes = new HashSet<>();

    }

}
