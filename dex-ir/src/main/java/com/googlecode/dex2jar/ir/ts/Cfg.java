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

import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;

/**
 * TODO DOC
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class Cfg {

    public interface FrameVisitor<T> extends StmtVisitor<T> {
        void merge(T frame, Stmt dist);
    }

    public interface StmtVisitor<T> {
        T exec(Stmt stmt);
    }

    private static boolean notThrow(Value s) {
        switch (s.et) {
        case E0:
            switch (s.vt) {
            case LOCAL:
            case CONSTANT:
                return true;
            }
            break;
        case E1:
            E1Expr e1 = (E1Expr) s;
            switch (s.vt) {
            case CAST:
            case NEG:
                return notThrow(e1.op.value);
            }
            break;
        case E2:
            E2Expr e2 = (E2Expr) s;
            switch (s.vt) {
            case ADD:
            case AND:
            case LCMP:
            case FCMPG:
            case FCMPL:
            case DCMPG:
            case DCMPL:
                // case DIV: div 0
            case EQ:
            case GE:
            case GT:
            case LE:
            case LT:
            case MUL:
            case NE:
            case OR:
            case REM:
            case SHL:
            case SHR:
            case SUB:
            case USHR:
            case XOR:
                return notThrow(e2.op1.value) && notThrow(e2.op2.value);
            }
        case En:
        }

        return false;
    }

    public static boolean notThrow(Stmt s) {
        switch (s.st) {
        case LABEL:
        case RETURN:
        case RETURN_VOID:
        case GOTO:
        case NOP:
        case IDENTITY:
            return true;
        case ASSIGN:
            E2Stmt e2 = (E2Stmt) s;
            return notThrow(e2.op1.value) && notThrow(e2.op2.value);
        case TABLE_SWITCH:
        case LOOKUP_SWITCH:
            E1Stmt s1 = (E1Stmt) s;
            return notThrow(s1.op.value);
        case IF:
            return notThrow(((E1Stmt) s).op.value);
        }
        return false;
    }

    public static void createCFG(IrMethod jm) {

        for (Stmt st : jm.stmts) {
            if (st._cfg_froms == null) {
                st._cfg_froms = new TreeSet<Stmt>(jm.stmts);
            } else {
                st._cfg_froms.clear();
            }
            if (st._cfg_tos == null) {
                st._cfg_tos = new TreeSet<Stmt>(jm.stmts);
            } else {
                st._cfg_tos.clear();
            }
        }

        for (Trap t : jm.traps) {
            for (Stmt s = t.start.getNext(); s != t.end; s = s.getNext()) {
                if (!notThrow(s)) {
                    // 为什么连接其上一个节点?
                    // 对一条会抛出异常的语句来说,如果执行失败,handler的frame应该和其父节点相同
                    // 比方说
                    // 0
                    // 1 b=(Boolean)b
                    // 2 c=@Ex
                    // 3 c=(string)b
                    // 0 - 2 > 2
                    // 如果1语句出错,则或使用0的frame到2去执行
                    link(s.getPre(), t.handler);
                }
            }
        }
        Set<Stmt> tails = new TreeSet<Stmt>(jm.stmts);

        for (Stmt st : jm.stmts) {
            switch (st.st) {
            case GOTO:
                link(st, ((JumpStmt) st).target);
                break;
            case IF:
                link(st, ((JumpStmt) st).target);
                link(st, st.getNext());
                break;
            case LOOKUP_SWITCH:
                LookupSwitchStmt lss = (LookupSwitchStmt) st;
                link(st, lss.defaultTarget);
                for (LabelStmt ls : lss.targets) {
                    link(st, ls);
                }
                break;
            case TABLE_SWITCH:
                TableSwitchStmt tss = (TableSwitchStmt) st;
                link(st, tss.defaultTarget);
                for (LabelStmt ls : tss.targets) {
                    link(st, ls);
                }
                break;
            case THROW:
                if (st._cfg_tos.size() < 1) {
                    tails.add(st);
                }
                break;
            case RETURN:
            case RETURN_VOID:
                tails.add(st);
                break;
            default:
                link(st, st.getNext());
                break;
            }

        }
        jm.stmts._cfg_tais = tails;
    }

    public static <T> void Forward(IrMethod jm, StmtVisitor<T> sv) {
        if (jm.stmts.getSize() == 0) {
            return;
        }
        // clean
        for (Stmt st : jm.stmts) {
            st._cfg_visited = false;
        }

        Stack<Stmt> toVisitQueue = new Stack<Stmt>();
        boolean isFv = sv instanceof FrameVisitor;
        FrameVisitor<T> fv = isFv ? ((FrameVisitor<T>) sv) : null;

        toVisitQueue.add(jm.stmts.getFirst());

        while (!toVisitQueue.isEmpty()) {
            Stmt currentStmt = toVisitQueue.pop();
            if (currentStmt == null || currentStmt._cfg_visited) {
                continue;
            } else {
                currentStmt._cfg_visited = true;
            }
            toVisitQueue.addAll(currentStmt._cfg_tos);

            T afterExecFrame = sv.exec(currentStmt);
            if (isFv) {
                for (Stmt dist : currentStmt._cfg_tos) {
                    fv.merge(afterExecFrame, dist);
                }
            }
        }
    }

    private static void link(Stmt from, Stmt to) {
        if (to == null) {// last stmt is a LabelStmt
            return;
        }
        from._cfg_tos.add(to);
        to._cfg_froms.add(from);
    }

}
