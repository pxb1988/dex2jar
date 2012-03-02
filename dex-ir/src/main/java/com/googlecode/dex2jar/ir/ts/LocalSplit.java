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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.LocalVar;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.EnStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.ts.Cfg.FrameVisitor;

/**
 * TODO DOC
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class LocalSplit implements Transformer {

    static ValueBox exec(ValueBox vb, Phi[] currentFrame) {
        if (vb == null) {
            return null;
        }
        Value v = vb.value;
        switch (v.et) {
        case E0:
            if (v.vt == VT.LOCAL) {
                Local local = trim(currentFrame[((Local) v)._ls_index]);
                local._ls_read_count++;
                vb = local._ls_vb;
            }
            break;
        case E1:
            E1Expr e1 = (E1Expr) v;
            e1.op = exec(e1.op, currentFrame);
            break;
        case E2:
            E2Expr e2 = (E2Expr) v;
            e2.op1 = exec(e2.op1, currentFrame);
            e2.op2 = exec(e2.op2, currentFrame);
            break;
        case En:
            EnExpr en = (EnExpr) v;
            for (int i = 0; i < en.ops.length; i++) {
                en.ops[i] = exec(en.ops[i], currentFrame);
            }
            break;
        }
        return vb;
    }

    static Local trim(ValueBox vb) {
        Local local = (Local) vb.value;
        if (local == null) {
            return null;
        }
        while (local._ls_vb.value != local) {
            local = (Local) local._ls_vb.value;
        }
        return local;
    }

    private static class Phi extends ValueBox {
        public Phi() {
            super(null);
        }

        public List<Phi> parent;

        public void setLocal(Local local) {
            if (this.value != null) {
                Local local2 = trim(this);
                if (local2 != local) {
                    local2._ls_vb = local._ls_vb;
                }
                this.value = local;
            } else {
                this.value = local;
            }
            if (parent != null) {
                for (Phi p : parent) {
                    if (p == null || p.value != local) {
                        p.setLocal(local);
                    }
                }
            }
        }
    }

    @Override
    public void transform(final IrMethod jm) {
        final int orgLocalSize = jm.locals.size();
        final StmtList list = jm.stmts;
        for (int i = 0; i < orgLocalSize; i++) {
            Local local = jm.locals.get(i);
            local._ls_index = i;
        }

        jm.locals.clear();
        Cfg.createCFG(jm);

        final ArrayList<Stmt> _ls_visit_order = new ArrayList<Stmt>(list.getSize());
        final int[] localId = { 0 };
        Cfg.Forward(jm, new FrameVisitor<Phi[]>() {

            private void doLocalRef(ValueBox vb, Phi[] frame) {
                if (vb == null) {
                    return;
                }
                Value v = vb.value;
                switch (v.et) {
                case E0:
                    if (v.vt == VT.LOCAL) {
                        Phi p = frame[((Local) v)._ls_index];
                        if (p.value == null) {
                            Local local = new Local("a" + localId[0]++);
                            ValueBox nvb = new ValueBox(local);
                            local._ls_vb = nvb;
                            p.setLocal(local);
                        }
                    }
                    break;
                case E1:
                    E1Expr e1 = (E1Expr) v;
                    doLocalRef(e1.op, frame);
                    break;
                case E2:
                    E2Expr e2 = (E2Expr) v;
                    doLocalRef(e2.op1, frame);
                    doLocalRef(e2.op2, frame);
                    break;
                case En:
                    EnExpr en = (EnExpr) v;
                    for (ValueBox op : en.ops) {
                        doLocalRef(op, frame);
                    }
                    break;
                }
            }

            Phi[] tmp = new Phi[orgLocalSize];

            @Override
            public Phi[] exec(Stmt stmt) {
                _ls_visit_order.add(stmt);
                Phi[] currentFrame = (Phi[]) stmt._ls_forward_frame;
                if (currentFrame == null) {
                    stmt._ls_forward_frame = currentFrame = new Phi[orgLocalSize];
                    for (int i = 0; i < currentFrame.length; i++) {
                        currentFrame[i] = new Phi();
                    }
                }
                System.arraycopy(currentFrame, 0, tmp, 0, tmp.length);

                switch (stmt.et) {
                case E0:
                    break;
                case E1:
                    E1Stmt e1 = (E1Stmt) stmt;
                    doLocalRef(e1.op, tmp);
                    break;
                case E2:
                    E2Stmt e2 = (E2Stmt) stmt;
                    if (e2.op1.value.vt == VT.LOCAL) {

                        doLocalRef(e2.op2, tmp);
                        Phi phi = new Phi();
                        tmp[((Local) e2.op1.value)._ls_index] = phi;
                        e2.op1 = phi;
                    } else {
                        doLocalRef(e2.op1, tmp);
                        doLocalRef(e2.op2, tmp);
                    }
                    break;
                case En:
                    EnStmt en = (EnStmt) stmt;
                    for (ValueBox op : en.ops) {
                        doLocalRef(op, tmp);
                    }
                    break;
                }
                return tmp;
            }

            @Override
            public void merge(Phi[] sourceF, Stmt distStmt) {
                Phi[] targetF = (Phi[]) distStmt._ls_forward_frame;
                int dist_froms_size = distStmt._cfg_froms.size();
                if (targetF != null) {
                    for (int i = 0; i < targetF.length; i++) {
                        Phi source = sourceF[i];
                        Phi target = targetF[i];
                        if (target.parent == null) {
                            target.parent = new ArrayList<Phi>(dist_froms_size);
                        }
                        target.parent.add(source);
                        if (target.value != null) {
                            source.setLocal(trim(((Local) target.value)._ls_vb));
                        }
                    }
                } else {
                    distStmt._ls_forward_frame = targetF = new Phi[orgLocalSize];
                    if (distStmt._cfg_froms.size() > 1) {
                        for (int i = 0; i < targetF.length; i++) {
                            Phi target = new Phi();
                            target.parent = new ArrayList<Phi>(dist_froms_size);
                            target.parent.add(sourceF[i]);
                            targetF[i] = target;
                        }
                    } else {
                        System.arraycopy(sourceF, 0, targetF, 0, sourceF.length);
                    }
                }
            }
        });

        for (LocalVar var : jm.vars) {
            Stmt stmt = var.start.getNext();
            int index = ((Local) var.reg.value)._ls_index;
            while (stmt.st == ST.LABEL) {
                stmt = stmt.getNext();
            }
            Phi[] targetF = (Phi[]) stmt._ls_forward_frame;
            Phi p = targetF[index];

            if (p.value == null) {
                Local local = new Local("a" + localId[0]++);
                ValueBox nvb = new ValueBox(local);
                local._ls_vb = nvb;
                p.setLocal(local);
            }
            Local local2 = trim(p);
            var.reg = local2._ls_vb;
            local2._ls_write_count += 2;
        }

        int unRef = 0;
        Set<Local> locals = new HashSet<Local>();
        // reassign valuebox
        for (Iterator<Stmt> it = list.iterator(); it.hasNext();) {
            Stmt st = it.next();
            if (st._ls_forward_frame == null) {// dead code
                if (st.st != ST.LABEL) {// not remove label
                    it.remove();
                    continue;
                }
            }
            Phi[] currentFrame = (Phi[]) st._ls_forward_frame;
            switch (st.et) {
            case E0:
                break;
            case E1:
                E1Stmt e1 = (E1Stmt) st;
                e1.op = exec(e1.op, currentFrame);
                break;
            case E2:
                E2Stmt e2 = (E2Stmt) st;
                switch (e2.st) {
                case ASSIGN:
                case IDENTITY:
                    if (e2.op1 instanceof Phi) {
                        Local local = trim(e2.op1);
                        if (local == null) {
                            local = new Local("unRef" + unRef++);
                            local._ls_vb = new ValueBox(local);
                        }
                        locals.add(local);
                        e2.op1 = local._ls_vb;
                        local._ls_write_count++;
                        if (e2.op2.value.vt == VT.INVOKE_SPECIAL) {
                            InvokeExpr ie = (InvokeExpr) e2.op2.value;
                            if (ie.methodName.equals("<init>")) {
                                list._ls_inits.add((AssignStmt) e2);
                            }
                        }
                    } else {
                        e2.op1 = exec(e2.op1, currentFrame);
                    }
                    break;
                default:
                    e2.op1 = exec(e2.op1, currentFrame);
                    break;
                }
                e2.op2 = exec(e2.op2, currentFrame);
                break;
            case En:
                EnStmt en = (EnStmt) st;
                for (int i = 0; i < en.ops.length; i++) {
                    en.ops[i] = exec(en.ops[i], currentFrame);
                }
            }
            // clean
            st._ls_forward_frame = null;
        }
        jm.locals.addAll(locals);
        jm.stmts._ls_visit_order = _ls_visit_order;
    }
}
