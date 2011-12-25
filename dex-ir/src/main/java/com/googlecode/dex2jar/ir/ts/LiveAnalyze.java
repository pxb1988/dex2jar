/*
 * Copyright (c) 2009-2011 Panxiaobo
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.EnStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.ts.Cfg.FrameVisitor;

public class LiveAnalyze {
    public static class Phi {
        public Local local;
        public Phi tag;
        public boolean used;
        public Set<Phi> sets = new HashSet<Phi>(3);

        public String toString() {
            if (tag != null) {
                return tag.toString();
            }
            if (local != null) {
                return local.toString();
            }
            return "?";
        }
    }

    private static void doAddUsed(Phi r, Set<Phi> regs) {
        if (r.used) {
            if (!regs.contains(r)) {
                regs.add(r);
                for (Phi p : r.sets) {
                    p.used = true;
                    doAddUsed(p, regs);
                }
            }
        }
    }

    private static Phi trim(Phi phi) {
        while (phi.tag != null) {
            phi = phi.tag;
        }
        return phi;
    }

    protected IrMethod method;
    protected int localSize;

    public LiveAnalyze(IrMethod method) {
        super();
        this.method = method;
        this.localSize = method.locals.size();
    }

    public List<Phi> analyze() {
        init();
        return analyze0();
    }

    protected List<Phi> analyze0() {

        final List<Phi> phis = new ArrayList<Phi>();

        Cfg.createCfgForLiveAnalyze(method);
        Cfg.Forward(method, new FrameVisitor<Phi[]>() {

            @Override
            public Phi[] exec(Stmt stmt) {
                Phi[] frame = (Phi[]) stmt._ls_forward_frame;
                if (frame == null) {
                    frame = new Phi[localSize];
                    stmt._ls_forward_frame = frame;
                }
                Phi[] result = frame;
                switch (stmt.et) {
                case E0:
                    break;
                case E1:
                    use(((E1Stmt) stmt).op, result);
                    break;
                case E2:
                    E2Stmt e2 = (E2Stmt) stmt;
                    if ((e2.st == ST.ASSIGN || e2.st == ST.IDENTITY) && (((AssignStmt) stmt).op1.value.vt == VT.LOCAL)) {
                        Local local = (Local) ((AssignStmt) stmt).op1.value;
                        use(e2.op2, result);
                        result = new Phi[localSize];
                        System.arraycopy(frame, 0, result, 0, localSize);
                        Phi phi = new Phi();
                        phis.add(phi);
                        result[local._ls_index] = phi;
                    } else {
                        use(e2.op1, result);
                        use(e2.op2, result);
                    }
                    break;
                case En:
                    EnStmt en = (EnStmt) stmt;
                    for (ValueBox vb : en.ops) {
                        use(vb, result);
                    }
                    break;
                }

                return result;
            }

            @Override
            public void merge(Phi[] frame, Stmt dist) {
                Phi[] distFrame = (Phi[]) dist._ls_forward_frame;
                if (distFrame == null) {
                    distFrame = new Phi[localSize];
                    dist._ls_forward_frame = distFrame;
                }

                for (int i = 0; i < localSize; i++) {
                    Phi srcPhi = frame[i];
                    if (srcPhi != null) {
                        Phi distPhi = distFrame[i];
                        if (distPhi == null) {
                            if (!dist._cfg_visited) {
                                distPhi = new Phi();
                                phis.add(distPhi);
                                distFrame[i] = distPhi;
                                distPhi.sets.add(srcPhi);
                            }
                        } else {
                            distPhi.sets.add(srcPhi);
                        }
                    }
                }
            }

            private void use(ValueBox op, Phi[] frame) {
                if (op == null) {
                    return;
                }
                Value v = op.value;
                switch (v.et) {
                case E0:
                    if (v.vt == VT.LOCAL) {
                        Local local = (Local) v;
                        frame[local._ls_index].used = true;
                        frame[local._ls_index].local = local;
                    }
                    break;
                case E1:
                    use(((E1Expr) v).op, frame);
                    break;
                case E2:
                    E2Expr e2 = (E2Expr) v;
                    use(e2.op1, frame);
                    use(e2.op2, frame);
                    break;
                case En:
                    EnExpr en = (EnExpr) v;
                    for (ValueBox vb : en.ops) {
                        use(vb, frame);
                    }
                    break;
                }
            }
        });

        Set<Phi> used = new HashSet<Phi>(phis.size() / 2);
        for (Phi reg : phis) {
            doAddUsed(reg, used);
        }
        for (Phi reg : used) {
            Phi a = trim(reg);
            if (a != reg && reg.local != null) {
                a.local = reg.local;
            }
            if (reg.sets.size() > 0) {
                for (Phi r : reg.sets) {
                    Phi b = trim(r);
                    if (a != b) {
                        b.tag = a;
                        if (b.local != null) {
                            a.local = b.local;
                        }
                    }
                }
            }
        }

        for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
            Phi[] frame = (Phi[]) p._ls_forward_frame;
            if (frame != null) {
                for (int i = 0; i < frame.length; i++) {
                    Phi r = frame[i];
                    if (r != null) {
                        if (r.used) {
                            frame[i] = trim(r);
                        } else {
                            frame[i] = null;
                        }
                    }
                }
            }
        }

        phis.clear();
        for (Phi r : used) {
            if (r.used && r.tag == null) {
                r.sets.clear();
                phis.add(r);
            }
        }
        used.clear();

        Collections.sort(phis, new Comparator<Phi>() {
            @Override
            public int compare(Phi o1, Phi o2) {
                return o1.local._ls_index - o2.local._ls_index;
            }
        });
        return phis;
    }

    protected void init() {
        int index = 0;
        for (Local local : method.locals) {
            local._ls_index = index++;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Stmt stmt = method.stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
            Phi[] frame = (Phi[]) stmt._ls_forward_frame;
            if (frame != null) {
                for (Phi p : frame) {
                    if (p == null) {
                        sb.append('.');
                    } else if (p.used) {
                        sb.append('x');
                    } else {
                        sb.append('?');
                    }
                }
                sb.append(" | ");
            }
            sb.append(stmt.toString()).append('\n');
        }
        return sb.toString();
    }
}