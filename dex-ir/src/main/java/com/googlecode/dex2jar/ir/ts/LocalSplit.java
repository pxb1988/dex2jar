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

import static com.googlecode.dex2jar.ir.Constant.nInt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.EnStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.ts.Cfg.FrameVisitor;

/**
 * TODO DOC
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class LocalSplit implements Transformer {

    static ValueBox exec(ValueBox vb, ValueBox[] currentFrame) {
        if (vb == null)
            return null;
        Value v = vb.value;
        switch (v.et) {
        case E0:
            if (v.vt == VT.LOCAL) {
                Local local = (Local) trimLocalVB(currentFrame[((Local) v)._ls_index]).value;
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

    public void transform(final IrMethod jm) {
        final int orgLocalSize = jm.locals.size();
        final StmtList list = jm.stmts;
        for (int i = 0; i < orgLocalSize; i++) {
            Local local = jm.locals.get(i);
            local._ls_index = i;
        }
        jm.locals.clear();
        final List<Local> locals = jm.locals;

        final Value NEED = nInt(1);
        final Value NotNEED = nInt(0);
        Cfg.createCFG(jm);

        Cfg.Backward(jm, new FrameVisitor<ValueBox[]>() {

            private void doLocalRef(ValueBox vb, ValueBox[] frame) {
                if (vb == null)
                    return;
                Value v = vb.value;
                switch (v.et) {
                case E0:
                    if (v.vt == VT.LOCAL) {
                        frame[((Local) v)._ls_index] = new ValueBox(NEED);
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
                    for (int i = 0; i < en.ops.length; i++) {
                        doLocalRef(en.ops[i], frame);
                    }
                    break;
                }
            }

            @Override
            public ValueBox[] exec(Stmt stmt) {
                ValueBox[] tmp = new ValueBox[orgLocalSize];
                if (stmt._ls_backward_frame != null) {
                    System.arraycopy(stmt._ls_backward_frame, 0, tmp, 0, tmp.length);
                } else {
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = new ValueBox(NotNEED);
                    }
                }

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
                        tmp[((Local) e2.op1.value)._ls_index] = new ValueBox(NotNEED);
                        doLocalRef(e2.op2, tmp);
                    } else {
                        doLocalRef(e2.op1, tmp);
                        doLocalRef(e2.op2, tmp);
                    }
                    break;
                case En:
                    EnStmt en = (EnStmt) stmt;
                    for (int i = 0; i < en.ops.length; i++) {
                        doLocalRef(en.ops[i], tmp);
                    }
                    break;
                }
                return tmp;
            }

            @Override
            public void merge(ValueBox[] currnetFrame, Stmt dist) {
                if (dist._ls_backward_frame == null) {
                    dist._ls_backward_frame = new ValueBox[orgLocalSize];
                    for (int i = 0; i < currnetFrame.length; i++) {
                        dist._ls_backward_frame[i] = new ValueBox(currnetFrame[i].value);
                    }
                } else {
                    ValueBox[] distFrame = (ValueBox[]) dist._ls_backward_frame;
                    for (int i = 0; i < currnetFrame.length; i++) {
                        if (currnetFrame[i].value == NEED) {
                            distFrame[i].value = NEED;
                        }
                    }
                }
            }
        });

        final ArrayList<Stmt> _ls_visit_order = new ArrayList<Stmt>(list.getSize());

        Cfg.Forward(jm, new FrameVisitor<ValueBox[]>() {

            int localId = 0;
            ValueBox[] tmp = new ValueBox[orgLocalSize];

            @Override
            public ValueBox[] exec(Stmt stmt) {
                _ls_visit_order.add(stmt);
                ValueBox[] currentFrame = (ValueBox[]) stmt._ls_forward_frame;
                if (currentFrame == null) {
                    stmt._ls_forward_frame = currentFrame = new ValueBox[orgLocalSize];
                }

                switch (stmt.st) {
                case ASSIGN:
                case IDENTITY:
                    E2Stmt assignStmt = (E2Stmt) stmt;
                    if (assignStmt.op1.value.vt == VT.LOCAL) {
                        System.arraycopy(currentFrame, 0, tmp, 0, tmp.length);
                        Local local = (Local) assignStmt.op1.value;
                        int reg = local._ls_index;
                        Local nLocal = Exprs.nLocal("a_" + localId++, null);
                        nLocal._ls_index = reg;
                        locals.add(nLocal);
                        ValueBox vb = new ValueBox(nLocal);
                        nLocal._ls_vb = vb;
                        assignStmt.op1 = vb;
                        tmp[reg] = vb;
                        currentFrame = tmp;
                    }
                }
                return currentFrame;
            }

            @Override
            public void merge(ValueBox[] frame, Stmt distStmt) {
                ValueBox[] currentFrame = (ValueBox[]) frame;
                if (distStmt == null) {
                    return;
                }
                if (distStmt._ls_forward_frame == null) {
                    distStmt._ls_forward_frame = new ValueBox[currentFrame.length];
                    System.arraycopy(currentFrame, 0, distStmt._ls_forward_frame, 0, currentFrame.length);
                } else {
                    ValueBox[] b = (ValueBox[]) distStmt._ls_forward_frame;
                    ValueBox[] backwardFrame = distStmt._ls_backward_frame;
                    for (int i = 0; i < currentFrame.length; i++) {
                        if (backwardFrame[i].value == NotNEED) {
                            continue;
                        }
                        ValueBox ai = trimLocalVB(currentFrame[i]);
                        ValueBox bi = trimLocalVB(b[i]);
                        if (bi == null) {
                            // b[i] = ai;
                            continue;
                        }
                        if (ai == bi) {
                            continue;
                        }

                        if (ai.value != bi.value) {
                            locals.remove(ai.value);
                        }
                        Local la = (Local) ai.value;
                        ai.value = bi.value;
                        la._ls_vb = bi;
                    }
                }
            }
        });

        // reassign valuebox
        for (Iterator<Stmt> it = list.iterator(); it.hasNext();) {
            Stmt st = it.next();
            if (st._ls_forward_frame == null) {// dead code
                it.remove();
                continue;
            }
            ValueBox[] currentFrame = st._ls_forward_frame;
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
                    if (e2.op1.value.vt == VT.LOCAL) {
                        e2.op1 = trimLocalVB(e2.op1);
                        Local local = (Local) e2.op1.value;
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
            st._ls_backward_frame = null;
        }
        // for (Stmt st : jm.stmts) {
        // System.out.printf("%30s %s\n",
        // st._ls_backward_frame == null ? Arrays.asList() : Arrays.asList(st._ls_backward_frame), st);
        // }
        jm.stmts._ls_visit_order = _ls_visit_order;
    }

    static ValueBox trimLocalVB(ValueBox vb) {
        if (vb == null)
            return null;
        while (vb != ((Local) vb.value)._ls_vb) {
            vb = ((Local) vb.value)._ls_vb;
        }
        return vb;
    }
}
