package com.googlecode.dex2jar.ir.ts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;

public class Reorder implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        if (irMethod.traps.size() > 0) {
            // FIXME
            return;
        }
        StmtList stmts = irMethod.stmts;
        for (Stmt stmt = stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {

            switch (stmt.st) {
            case IF:
                Stmt n = stmt.getNext();
                if (n != null && n.st != ST.LABEL) {
                    LabelStmt ls = Stmts.nLabel();
                    stmts.insertAftre(stmt, ls);
                }
                stmt._ro_default_next = stmt.getNext();
                break;
            case GOTO:
            case RETURN:
            case RETURN_VOID:
            case TABLE_SWITCH:
            case LOOKUP_SWITCH:
            case THROW:
                stmt._ro_default_next = null;
                break;
            default:
                stmt._ro_default_next = stmt.getNext();
                break;
            }
        }
        Cfg.createCFG(irMethod);

        for (Stmt stmt = stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
            stmt._cfg_visited = false;
            switch (stmt.st) {
            case GOTO:
                JumpStmt js = (JumpStmt) stmt;
                for (Stmt f : stmt._cfg_froms) {
                    f._cfg_tos.remove(stmt);
                    f._cfg_tos.addAll(stmt._cfg_tos);
                    f._ro_default_next = js.target;
                }

                for (Stmt t : stmt._cfg_tos) {
                    t._cfg_froms.remove(stmt);
                    t._cfg_froms.addAll(stmt._cfg_froms);
                }
                break;
            }
        }

        List<Stmt> out = new ArrayList(stmts.getSize());
        Stack<Stmt> stack = new Stack();
        stack.push(stmts.getFirst());
        stmts.clear();

        while (!stack.empty()) {
            Stmt stmt = stack.pop();
            if (stmt._cfg_visited) {
                continue;
            }
            if (stmt._cfg_froms.size() == 0 || stack.size() == 0) {
                stmt._cfg_visited = true;
                out.add(stmt);
                Collection<Stmt> tos = stmt._cfg_tos;
                if (stmt.st == ST.TABLE_SWITCH) {
                    TableSwitchStmt tss = (TableSwitchStmt) stmt;
                    List<Stmt> toPush = new ArrayList(tss._cfg_tos.size());
                    toPush.remove(tss.defaultTarget);
                    for (int i = 0; i < tss.targets.length; i++) {
                        toPush.add(tss.targets[i]);
                    }
                    toPush.add(tss.defaultTarget);

                    for (Stmt t : tss._cfg_tos) {
                        if (!toPush.contains(t)) {
                            toPush.add(t);
                        }
                    }

                    Collections.reverse(toPush);
                    tos = toPush;
                } else if (stmt.st == ST.LOOKUP_SWITCH) {
                    LookupSwitchStmt lss = (LookupSwitchStmt) stmt;
                    List<Stmt> toPush = new ArrayList(lss._cfg_tos.size());
                    toPush.remove(lss.defaultTarget);
                    for (int i = 0; i < lss.targets.length; i++) {
                        toPush.add(lss.targets[i]);
                    }
                    toPush.add(lss.defaultTarget);

                    for (Stmt t : lss._cfg_tos) {
                        if (!toPush.contains(t)) {
                            toPush.add(t);
                        }
                    }

                    Collections.reverse(toPush);
                    tos = toPush;
                }
                for (Stmt t : tos) {
                    t._cfg_froms.remove(stmt);
                    if (!t._cfg_visited) {
                        stack.push(t);
                    }
                }
            }
        }
        List<Stmt> out2 = new ArrayList(stmts.getSize());
        System.out.println(out);

        for (int i = 0; i < out.size(); i++) {
            Stmt stmt = out.get(i);
            out2.add(stmt);
            Stmt orgNext = stmt._ro_default_next;
            if (orgNext != null && orgNext.st == ST.LABEL) {
                if (i + 1 < out.size()) {
                    Stmt next = out.get(i + 1);
                    if (next != orgNext) {
                        if (stmt.st == ST.IF) {
                            JumpStmt jumpStmt = (JumpStmt) stmt;
                            if (jumpStmt.target == next) {
                                reverse(jumpStmt.op);
                                jumpStmt.target = (LabelStmt) orgNext;
                            } else {
                                out2.add(Stmts.nGoto((LabelStmt) orgNext));
                            }
                        } else {
                            out2.add(Stmts.nGoto((LabelStmt) orgNext));
                        }
                    }
                } else {
                    out2.add(Stmts.nGoto((LabelStmt) orgNext));
                }
            }
        }
        System.out.println(out2);
    }

    private void reverse(ValueBox op) {
        E2Expr e2 = (E2Expr) op.value;

        switch (e2.vt) {
        case GE:
            op.value = Exprs.nLt(e2.op1.value, e2.op2.value);
            break;
        case GT:
            op.value = Exprs.nLe(e2.op1.value, e2.op2.value);
            break;
        case LT:
            op.value = Exprs.nGe(e2.op1.value, e2.op2.value);
            break;
        case LE:
            op.value = Exprs.nGt(e2.op1.value, e2.op2.value);
            break;
        case EQ:
            op.value = Exprs.nNe(e2.op1.value, e2.op2.value);
            break;
        case NE:
            op.value = Exprs.nEq(e2.op1.value, e2.op2.value);
            break;
        }
    }
}
