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
import com.googlecode.dex2jar.ir.ts.Cfg.StmtVisitor;

public class LocalSpliter implements Transformer {

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

        Cfg.createCFG(jm);

        Cfg.Backward(jm, new StmtVisitor() {

            @Override
            public void merge(Object frame, Stmt dist) {
                ValueBox[] currnetFrame = (ValueBox[]) frame;
                if (dist._ls_backward_frame == null) {
                    dist._ls_backward_frame = new ValueBox[orgLocalSize];
                    System.arraycopy(currnetFrame, 0, dist._ls_backward_frame, 0, currnetFrame.length);
                } else {
                    ValueBox[] distFrame = (ValueBox[]) dist._ls_backward_frame;
                    for (int i = 0; i < currnetFrame.length; i++) {
                        if (currnetFrame[i].value != null) {
                            distFrame[i].value = currnetFrame[i].value;
                        }
                    }
                }
            }

            void doLocalRef(ValueBox vb, ValueBox[] frame) {
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
            public Object exec(Stmt stmt) {
                ValueBox[] tmp = new ValueBox[orgLocalSize];
                if (stmt._ls_backward_frame != null) {
                    System.arraycopy(stmt._ls_backward_frame, 0, tmp, 0, tmp.length);
                } else {
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = new ValueBox(null);
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
                        doLocalRef(e2.op2, tmp);
                        tmp[((Local) e2.op1.value)._ls_index] = new ValueBox(null);
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
        });

        final ArrayList<Stmt> _ls_visit_order = new ArrayList<Stmt>(list.getSize());

        Cfg.Forward(jm, new StmtVisitor() {

            ValueBox[] tmp = new ValueBox[orgLocalSize];
            int localId = 0;

            @Override
            public void merge(Object frame, Stmt distStmt) {
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
                        if (backwardFrame[i].value == null) {
                            continue;
                        }
                        ValueBox ai = currentFrame[i];
                        if (ai != null) {
                            ValueBox bi = b[i];
                            if (ai != bi) {
                                if (bi == null) {
                                    b[i] = ai;
                                } else {
                                    if (ai.value != bi.value) {
                                        locals.remove(ai.value);
                                    }
                                    Local la = (Local) ai.value;
                                    Local lb = (Local) bi.value;
                                    ai.value = bi.value;
                                    la._ls_vb = lb._ls_vb;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public Object exec(Stmt stmt) {
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
                        Local local = (Local) e2.op1.value;
                        local._ls_write_count++;
                        e2.op1 = local._ls_vb;
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

        jm.stmts._ls_visit_order = _ls_visit_order;
    }

    static ValueBox exec(ValueBox vb, ValueBox[] currentFrame) {
        if (vb == null)
            return null;
        Value v = vb.value;
        switch (v.et) {
        case E0:
            if (v.vt == VT.LOCAL) {
                Local local = (Local) currentFrame[((Local) v)._ls_index].value;
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

}
