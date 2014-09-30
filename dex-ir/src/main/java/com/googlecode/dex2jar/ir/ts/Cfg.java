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

import java.util.Collections;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.*;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

/**
 * TODO DOC
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Cfg {

    public static int[] countLocalReads(IrMethod method) {
        int size = reIndexLocal(method);
        final int readCounts[] = new int[size];
        travel(method.stmts, new TravelCallBack() {
            @Override
            public Value onAssign(Local v, AssignStmt as) {
                return v;
            }

            @Override
            public Value onUse(Local v) {
                readCounts[v._ls_index]++;
                return v;
            }
        }, true);
        return readCounts;
    }

    public static void reIndexLocalAndLabel(IrMethod irMethod) {
        reIndexLocal(irMethod);
        reIndexLabel(irMethod);
    }

    private static void reIndexLabel(IrMethod irMethod) {
        int i = 0;
        for (Stmt stmt : irMethod.stmts) {
            if (stmt.st == ST.LABEL) {
                ((LabelStmt) stmt).displayName = "L" + i++;
            }
        }
    }

    public interface FrameVisitor<T> {
        T merge(T srcFrame, T distFrame, Stmt src, Stmt dist);

        T initFirstFrame(Stmt first);

        T exec(T frame, Stmt stmt);
    }


    public static boolean notThrow(Stmt s) {
        return !isThrow(s);
    }

    public static boolean isThrow(Stmt s) {
        ST st = s.st;
        if (st.canThrow()) {
            return true;
        } else if (st.mayThrow()) {
            ET et = s.et;
            if (et == ET.E1) {
                return isThrow(s.getOp());
            } else if (et == ET.E2) {
                return isThrow(s.getOp1()) || isThrow(s.getOp2());
            } else {
                throw new RuntimeException();
            }
        } else {
            return false;
        }
    }

    private static boolean isThrow(Value op) {
        VT vt = op.vt;
        if (vt.canThrow()) {
            return true;
        } else if (vt.mayThrow()) {
            switch (op.et) {
            case E1:
                return isThrow(op.getOp());
            case E2:
                return isThrow(op.getOp1()) || isThrow(op.getOp2());
            default:
            case En:
            case E0:
                throw new RuntimeException();
            }
        } else {
            return false;
        }
    }

    public static void createCfgWithoutEx(IrMethod jm) {
        for (Stmt st : jm.stmts) {
            st.frame = null;
            st.exceptionHandlers = null;
            if (st._cfg_froms == null) {
                st._cfg_froms = new TreeSet<>(jm.stmts);
            } else {
                st._cfg_froms.clear();
            }
        }

        for (Stmt st : jm.stmts) {
            if (st.st.canBranch()) {
                link(st, ((JumpStmt) st).getTarget());
            }
            if (st.st.canContinue()) {
                link(st, st.getNext());
            }
            if (st.st.canSwitch()) {
                BaseSwitchStmt bss = (BaseSwitchStmt) st;
                link(st, bss.defaultTarget);
                for (Stmt target : bss.targets) {
                    link(st, target);
                }
            }
        }
    }

    public static void createCFG(IrMethod jm) {
        createCfgWithoutEx(jm);
        for (Trap t : jm.traps) {
            for (Stmt s = t.start; s != t.end; s = s.getNext()) {
                if (isThrow(s)) {
                    Set<LabelStmt> hs = s.exceptionHandlers;
                    if (hs == null) {
                        hs = new TreeSet<>(jm.stmts);
                        s.exceptionHandlers = hs;
                    }
                    for (LabelStmt handler : t.handlers) {
                        link(s, handler);
                        hs.add(handler);
                    }
                }
            }
        }

    }

    public static interface DfsVisitor {
        void onVisit(Stmt p);
    }

    public static void dfsVisit(IrMethod method, DfsVisitor visitor) {
        for (Stmt st : method.stmts) {
            st.visited = false;
        }
        Stack<Stmt> stack = new Stack<>();
        stack.add(method.stmts.getFirst());
        while (!stack.isEmpty()) {
            Stmt currentStmt = stack.pop();
            if (currentStmt.visited) {
                continue;
            } else {
                currentStmt.visited = true;
            }
            if (currentStmt.exceptionHandlers != null) {
                for (LabelStmt labelStmt : currentStmt.exceptionHandlers) {
                    stack.push(labelStmt);
                }
            }
            if (visitor != null) {
                visitor.onVisit(currentStmt);
            }
            if (currentStmt.st.canSwitch()) {
                BaseSwitchStmt bs = (BaseSwitchStmt) currentStmt;
                Collections.addAll(stack, bs.targets);
                LabelStmt target = bs.defaultTarget;
                stack.add(target);
            }
            if (currentStmt.st.canBranch()) {
                Stmt target = ((JumpStmt) currentStmt).getTarget();
                stack.add(target);
            }
            if (currentStmt.st.canContinue()) {
                Stmt target = currentStmt.getNext();
                stack.add(target);
            }
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> void dfs(StmtList stmts, FrameVisitor<T> sv) {
        if (stmts.getSize() == 0) {
            return;
        }
        // clean
        for (Stmt st : stmts) {
            st.visited = false;
            st.frame = null;
        }

        Stack<Stmt> stack = new Stack<Stmt>();
        Stmt first = stmts.getFirst();
        Stmt nop = null;
        if (first.st == ST.LABEL && first._cfg_froms.size() > 0) {
            nop = Stmts.nNop();
            // for
            // L0:
            // ...
            // GOTO L0:
            // make sure the first Label has one more super
            first._cfg_froms.add(nop);
        }
        stack.add(first);
        first.frame = sv.initFirstFrame(first);

        while (!stack.isEmpty()) {
            Stmt currentStmt = stack.pop();
            if (currentStmt == null || currentStmt.visited) {
                continue;
            } else {
                currentStmt.visited = true;
            }

            T beforeExecFrame = (T) currentStmt.frame;
            
            if (currentStmt.exceptionHandlers != null) {
                for (LabelStmt labelStmt : currentStmt.exceptionHandlers) {
                    labelStmt.frame = sv.merge(beforeExecFrame, (T) labelStmt.frame, currentStmt, labelStmt);
                    stack.push(labelStmt);
                }
            }
            
            T afterExecFrame = sv.exec(beforeExecFrame, currentStmt);

            if (currentStmt.st.canSwitch()) {
                BaseSwitchStmt bs = (BaseSwitchStmt) currentStmt;
                for (LabelStmt target : bs.targets) {
                    target.frame = sv.merge(afterExecFrame, (T) target.frame, currentStmt, target);
                    stack.push(target);
                }
                LabelStmt target = bs.defaultTarget;
                target.frame = sv.merge(afterExecFrame, (T) target.frame, currentStmt, target);
                stack.push(target);
            }
            if (currentStmt.st.canBranch()) {
                Stmt target = ((JumpStmt) currentStmt).getTarget();
                target.frame = sv.merge(afterExecFrame, (T) target.frame, currentStmt, target);
                stack.push(target);
            }
            if (currentStmt.st.canContinue()) {
                Stmt target = currentStmt.getNext();
                target.frame = sv.merge(afterExecFrame, (T) target.frame, currentStmt, target);
                stack.push(target);
            }
        }

        if (nop != null) {
            first._cfg_froms.remove(nop);
        }      
    }

    private static void link(Stmt from, Stmt to) {
        if (to == null) {// last stmt is a LabelStmt
            return;
        }
        to._cfg_froms.add(from);
    }

    public interface OnUseCallBack {
        Value onUse(Local v);
    }

    public interface OnAssignCallBack {
        Value onAssign(Local v, AssignStmt as);
    }

    public interface TravelCallBack extends OnUseCallBack, OnAssignCallBack {

    }

    public static Value travelMod(Value value, TravelCallBack callback) {
        switch (value.et) {
        case E0:
            if (value.vt == VT.LOCAL) {
                return callback.onUse((Local) value);
            }
            break;
        case E1:
            value.setOp(travelMod(value.getOp(), callback));
            break;
        case E2:
            value.setOp1(travelMod(value.getOp1(), callback));
            value.setOp2(travelMod(value.getOp2(), callback));
            break;
        case En:
            Value ops[] = value.getOps();
            for (int i = 0; i < ops.length; i++) {
                ops[i] = travelMod(ops[i], callback);
            }
            break;
        }
        return value;
    }

    public static void travel(Value value, OnUseCallBack callback) {
        switch (value.et) {
        case E0:
            if (value.vt == VT.LOCAL) {
                callback.onUse((Local) value);
            }
            break;
        case E1:
            travel(value.getOp(), callback);
            break;
        case E2:
            travel(value.getOp1(), callback);
            travel(value.getOp2(), callback);
            break;
        case En:
            Value ops[] = value.getOps();
            for (int i = 0; i < ops.length; i++) {
                travel(ops[i], callback);
            }
            break;
        }
    }

    public static void travelMod(Stmt p, TravelCallBack callback, boolean travelPhi) {
        switch (p.et) {
        case E1:
            p.setOp(travelMod(p.getOp(), callback));
            break;
        case E2:
            Value e2op1 = p.getOp1();
            if (e2op1.vt == VT.LOCAL && (p.st == ST.ASSIGN || p.st == ST.IDENTITY)) {
                p.setOp2(travelMod(p.getOp2(), callback));
                p.setOp1(callback.onAssign((Local) e2op1, (AssignStmt) p));
            } else {
                p.setOp1(travelMod(p.getOp1(), callback));
                p.setOp2(travelMod(p.getOp2(), callback));
            }
            break;
        case En:
        case E0:
            if (travelPhi && p.st == ST.LABEL) {
                LabelStmt labelStmt = (LabelStmt) p;
                if (labelStmt.phis != null) {
                    for (AssignStmt phi : labelStmt.phis) {
                        travelMod(phi, callback, false);
                    }
                }
            }
            break;
        }
    }

    public static void travel(Stmt p, TravelCallBack callback, boolean travelPhi) {
        switch (p.et) {
        case E1:
            travel(p.getOp(), callback);
            break;
        case E2:
            Value e2op1 = p.getOp1();
            if (e2op1.vt == VT.LOCAL && (p.st == ST.ASSIGN || p.st == ST.IDENTITY)) {
                travel(p.getOp2(), callback);
                callback.onAssign((Local) e2op1, (AssignStmt) p);
            } else {
                travel(p.getOp1(), callback);
                travel(p.getOp2(), callback);
            }
            break;
        case En:
        case E0:
            if (travelPhi && p.st == ST.LABEL) {
                LabelStmt labelStmt = (LabelStmt) p;
                if (labelStmt.phis != null) {
                    for (AssignStmt phi : labelStmt.phis) {
                        travel(phi, callback, false);
                    }
                }
            }
            break;
        }
    }

    public static void travel(StmtList stmts, TravelCallBack callback, boolean travelPhi) {
        for (Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
            travel(p, callback, travelPhi);
        }
    }

    public static void travelMod(StmtList stmts, TravelCallBack callback, boolean travelPhi) {
        for (Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
            travelMod(p, callback, travelPhi);
        }
    }

    /**
     * @param method
     * @return size of locals
     */
    public static int reIndexLocal(IrMethod method) {
        int i = 0;
        for (Local local : method.locals) {
            local._ls_index = i++;
        }
        return i;
    }

    public static void collectTos(Stmt stmt, Set<Stmt> tos) {
        if (stmt.st.canBranch()) {
            tos.add(((JumpStmt) stmt).getTarget());
        }
        if (stmt.st.canContinue()) {
            tos.add(stmt.getNext());
        }
        if (stmt.st.canSwitch()) {
            BaseSwitchStmt bss = (BaseSwitchStmt) stmt;
            tos.add(bss.defaultTarget);

            for (Stmt target : bss.targets) {
                tos.add(target);
            }
        }
        if (stmt.exceptionHandlers != null) {
            tos.addAll(stmt.exceptionHandlers);
        }
    }
}
