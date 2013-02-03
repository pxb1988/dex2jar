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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E0Expr;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.EnStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.BaseLiveAnalyze.Phi;

/**
 * 
 * @author Panxiaobo
 * 
 */
public class ZeroTransformer implements Transformer {
    static class ZeroAnalyzePhi extends Phi {
        public Boolean isZero = null;
        public Set<Phi> assignFrom = new HashSet<Phi>(3);
        public Set<Phi> assignTo = new HashSet<Phi>(3);

        @Override
        public String toString() {
            if (isZero == null) {
                return "?";
            }
            return isZero ? "Z" : ".";
        }
    }

    private static class ZeroAnalyze extends BaseLiveAnalyze {

        @Override
        protected Phi newPhi() {
            return new ZeroAnalyzePhi();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Stmt stmt = method.stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
                Phi[] frame = (Phi[]) stmt._ls_forward_frame;
                if (frame != null) {
                    for (Phi p : frame) {
                        if (p == null) {
                            sb.append("_");
                        } else {
                            sb.append(p.toString());
                        }
                    }
                    sb.append(" | ");
                }
                sb.append(stmt.toString()).append('\n');
            }
            return sb.toString();
        }

        public ZeroAnalyze(IrMethod irMethod) {
            super(irMethod);
        }

        @Override
        protected void initCFG() {
            Cfg.createCFG(method);
        }

        @Override
        protected void onAssignLocal(Phi[] frame, Phi phi, Value value) {
            if (value.vt == VT.CONSTANT) {
                ZeroAnalyzePhi zaf = (ZeroAnalyzePhi) phi;
                Constant c = (Constant) value;
                if (c.value instanceof Integer && ZERO.equals(c.value)) {
                    zaf.isZero = Boolean.TRUE;
                } else {
                    zaf.isZero = Boolean.FALSE;
                }
            } else if (value.vt == VT.LOCAL) {
                Local local = (Local) value;
                ZeroAnalyzePhi zaf1 = (ZeroAnalyzePhi) phi;
                ZeroAnalyzePhi zaf2 = (ZeroAnalyzePhi) frame[local._ls_index];
                zaf1.assignFrom.add(zaf2);
                zaf2.assignTo.add(zaf1);
            } else {
                ZeroAnalyzePhi zaf = (ZeroAnalyzePhi) phi;
                zaf.isZero = Boolean.FALSE;
            }
        }

        protected void analyzePhi() {
            super.analyzePhi();
            Queue<Phi> queue = new LinkedList<Phi>();
            queue.addAll(phis);
            while (!queue.isEmpty()) {
                ZeroAnalyzePhi phi = (ZeroAnalyzePhi) queue.poll();
                if (Boolean.FALSE.equals(phi.isZero)) {
                    for (Phi p : phi.children) {
                        ZeroAnalyzePhi cp = (ZeroAnalyzePhi) p;
                        if (cp.isZero == null) {
                            cp.isZero = Boolean.FALSE;
                            queue.add(cp);
                        }
                    }
                    for (Phi p : phi.assignTo) {
                        ZeroAnalyzePhi cp = (ZeroAnalyzePhi) p;
                        if (cp.isZero == null) {
                            cp.isZero = Boolean.FALSE;
                            queue.add(cp);
                        }
                    }
                }
            }

            for (Phi p : phis) {
                ZeroAnalyzePhi cp = (ZeroAnalyzePhi) p;
                if (cp.isZero == null) {
                    cp.isZero = Boolean.TRUE;
                }
            }

        }
    }

    private static final Integer ZERO = Integer.valueOf(0);

    @Override
    public void transform(IrMethod irMethod) {
        // 1. analyze
        ZeroAnalyze za = new ZeroAnalyze(irMethod);
        za.analyze();

        List<Local> locals = irMethod.locals;
        int localSize = locals.size();
        StmtList stmts = irMethod.stmts;
        // 2. mark MUST-BE-ZERO
        for (Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
            Phi[] frame = (Phi[]) p._ls_forward_frame;
            if (frame == null || !p._cfg_visited) {// dead code ?
                continue;
            }
            switch (p.et) {
            case E0:
                if (p.st == ST.LABEL) {
                    if (p._cfg_froms.size() > 0) { // there is a merge here
                        for (int i = 0; i < localSize; i++) {
                            ZeroAnalyzePhi phi = (ZeroAnalyzePhi) frame[i];
                            Local local = locals.get(i);
                            if (phi != null && Boolean.FALSE.equals(phi.isZero)) {// the local is not null
                                for (Stmt from : p._cfg_froms) {// check for each from
                                    if (needInsertX(from, i)) {
                                        insertX(stmts, (LabelStmt) p, from, i, local);
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case E1:
                E1Stmt e1 = (E1Stmt) p;
                replace(e1.op, frame);
                break;
            case E2:
                E2Stmt e2 = (E2Stmt) p;
                if (e2.op1.value.vt != VT.LOCAL) {
                    replace(e2.op1, frame);
                }
                replace(e2.op2, frame);
                break;
            case En:
                EnStmt en = (EnStmt) p;
                for (ValueBox vb : en.ops) {
                    replace(vb, frame);
                }
                break;
            }
        }
    }

    private void insertX(StmtList stmts, LabelStmt ls, Stmt p, int index, Local local) {
        switch (p.st) {
        case GOTO:
        case IF:
        case LOOKUP_SWITCH:
        case TABLE_SWITCH:
            stmts.insertBefore(p, Stmts.nAssign(local, Constant.nInt(0)));
            break;
        default:
            // TODO check if we need to insert for other smt
            // stmts.insertAfter(p, Stmts.nAssign(local, Constant.nInt(0)));
        }
    }

    /**
     * if the phi in frame p is ZERO, return true
     * 
     * @param p
     * @param index
     * @return
     */
    private boolean needInsertX(Stmt p, int index) {
        Phi[] frame = (Phi[]) p._ls_forward_frame;
        if (frame == null) {
            return false;
        }
        ZeroAnalyzePhi phi = (ZeroAnalyzePhi) frame[index];
        if (phi == null || phi.isZero == null) {
            return false;
        }
        return phi.isZero;
    }

    private void replace(ValueBox op, Phi[] frame) {
        if (op == null) {
            return;
        }
        Value value = op.value;
        switch (value.et) {
        case E0:
            E0Expr e0 = (E0Expr) value;
            if (e0.vt == VT.LOCAL) {
                Local local = (Local) e0;
                ZeroAnalyzePhi phi = (ZeroAnalyzePhi) frame[local._ls_index];
                if (Boolean.TRUE.equals(phi.isZero)) {
                    op.value = Constant.nInt(0);// replace it with zero
                }
            }
            break;
        case E1:
            E1Expr e1 = (E1Expr) value;
            replace(e1.op, frame);
            break;
        case E2:
            E2Expr e2 = (E2Expr) value;
            replace(e2.op1, frame);
            replace(e2.op2, frame);
            break;
        case En:
            EnExpr en = (EnExpr) value;
            for (ValueBox vb : en.ops) {
                replace(vb, frame);
            }
            break;
        }
    }
}
