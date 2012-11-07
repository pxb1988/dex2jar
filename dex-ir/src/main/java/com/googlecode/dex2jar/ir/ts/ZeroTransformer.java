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
import com.googlecode.dex2jar.ir.ET;
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
import com.googlecode.dex2jar.ir.ts.Cfg.FrameVisitor;

/**
 * 
 * @author Panxiaobo
 * 
 */
public class ZeroTransformer implements Transformer {

    public static class Phi {
        public Boolean isZero = null;
        public Set<Phi> parents = new HashSet<Phi>();
        public Set<Phi> children = new HashSet<Phi>();

        @Override
        public String toString() {
            if (isZero == null) {
                return "?";
            }
            return isZero ? "Z" : ".";
        }
    }

    private static class ZeroAnalyze {
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

        IrMethod method;

        public ZeroAnalyze(IrMethod irMethod) {
            super();
            this.method = irMethod;
        }

        private void init() {
            int i = 0;
            for (Local local : method.locals) {
                local._ls_index = i++;
            }
        }

        public void analyze() {
            init();
            analyze0();
        }

        private void analyze0() {
            Cfg.createCfgForLiveAnalyze(method);
            final int localSize = method.locals.size();
            final Phi[] tmp = new Phi[localSize];
            final Queue<Phi> phis = new LinkedList<Phi>();
            Cfg.Forward(method, new FrameVisitor<Phi[]>() {

                private Phi newPhi() {
                    Phi phi = new Phi();
                    phis.add(phi);
                    return phi;
                }

                @Override
                public Phi[] exec(Stmt stmt) {
                    {
                        Phi[] frame = (Phi[]) stmt._ls_forward_frame;
                        if (frame == null) {
                            stmt._ls_forward_frame = frame = new Phi[localSize];
                        }
                        System.arraycopy(frame, 0, tmp, 0, localSize);
                    }
                    if (stmt.et == ET.E2) {
                        E2Stmt e2 = (E2Stmt) stmt;
                        Value op1 = e2.op1.value;
                        Value op2 = e2.op2.value;
                        switch (stmt.st) {
                        case ASSIGN:
                            if (op1.vt == VT.LOCAL) {
                                Local a = (Local) op1;
                                switch (op2.vt) {
                                case CONSTANT: {
                                    Constant c = (Constant) op2;
                                    Phi phi = newPhi();
                                    if (c.value instanceof Integer && ZERO.equals(c.value)) {
                                        phi.isZero = Boolean.TRUE;
                                    } else {
                                        phi.isZero = Boolean.FALSE;
                                    }

                                    tmp[a._ls_index] = phi;
                                }
                                    break;
                                case LOCAL: {
                                    Local b = (Local) op2;
                                    tmp[a._ls_index] = tmp[b._ls_index];
                                }
                                    break;
                                default: {
                                    Phi phi = newPhi();
                                    phi.isZero = Boolean.FALSE;
                                    tmp[a._ls_index] = phi;
                                }
                                    break;
                                }
                            }
                            break;
                        case IDENTITY: {
                            Local a = (Local) op1;
                            Phi phi = newPhi();
                            phi.isZero = Boolean.FALSE;
                            tmp[a._ls_index] = phi;
                        }
                            break;
                        }
                    }
                    return tmp;
                }

                @Override
                public void merge(Phi[] frame, Stmt dist) {
                    if (dist._cfg_froms.size() == 1) {
                        dist._ls_forward_frame = new Phi[localSize];
                        System.arraycopy(frame, 0, dist._ls_forward_frame, 0, localSize);
                    } else {
                        Phi[] distFrame = (Phi[]) dist._ls_forward_frame;
                        if (distFrame == null) {
                            dist._ls_forward_frame = distFrame = new Phi[localSize];
                        }
                        for (int i = 0; i < localSize; i++) {
                            Phi b = distFrame[i];
                            if (b == null) {
                                distFrame[i] = b = newPhi();
                                phis.add(b);
                            }
                            Phi a = frame[i];
                            if (a != null) {
                                b.parents.add(a);
                                a.children.add(b);
                            }
                        }
                    }
                }
            });

            Set<Phi> cache = new HashSet<Phi>();

            while (!phis.isEmpty()) {
                Phi phi = phis.poll();
                cache.clear();
                if (phi.isZero == null) {
                    if (phi.parents.size() > 0) {
                        boolean isZero = true;

                        for (Phi parent : phi.parents) {
                            if (!Boolean.TRUE.equals(parent.isZero)) {
                                isZero = false;
                                break;
                            }
                        }
                        if (isZero) {
                            phi.isZero = Boolean.TRUE;
                            for (Phi child : phi.children) {
                                if (child.isZero == null) {
                                    cache.add(child);
                                }
                            }
                        }
                    }
                } else {
                    if (Boolean.FALSE.equals(phi.isZero)) {
                        for (Phi child : phi.children) {
                            if (child.isZero == null) {
                                cache.add(child);
                            }
                            child.isZero = Boolean.FALSE;
                        }
                    }
                }
                for (Phi x : cache) {
                    if (!phis.contains(x)) {
                        phis.add(x);
                    }
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
                            Phi phi = frame[i];
                            Local local = locals.get(i);
                            if (phi != null && phi.isZero != null && phi.isZero) {// the local is not null
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
            while (p != null) {
                Stmt q = p.getPre();
                if (q == null || q.st != ST.LABEL) {// insert before any label
                    stmts.insertBefore(p, Stmts.nAssign(local, Constant.nInt(0)));
                    break;
                }
                p = q;
            }
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
        Phi phi = frame[index];
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
                Phi phi = frame[local._ls_index];
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
