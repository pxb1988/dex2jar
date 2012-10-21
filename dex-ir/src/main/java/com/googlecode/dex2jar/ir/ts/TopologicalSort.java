package com.googlecode.dex2jar.ir.ts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.BinopExpr;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;

public class TopologicalSort implements Transformer {
    @Override
    public void transform(IrMethod irMethod) {
        if (irMethod.traps.size() > 0) {
            // FIXME
            return;
        }
        StmtList stmts = irMethod.stmts;
        // 1. generate graph
        init(stmts, irMethod.traps);

        // 2.remove any loop in the graph
        removeLoop(stmts);

        // 3. topological sorting algorithms
        List<Stmt> out = topologicalSort(stmts);

        stmts.clear();
        // 4. rebuild stmts
        rebuild(stmts, out);
    }

    private void rebuild(StmtList stmts, List<Stmt> out) {
        List<JumpStmt> gotos = new ArrayList<JumpStmt>();
        for (int i = 0; i < out.size(); i++) {
            Stmt stmt = out.get(i);
            stmts.add(stmt);
            Stmt orgNext = stmt._ts_default_next;
            if (orgNext != null && orgNext.st == ST.LABEL) {
                if (i + 1 < out.size()) {
                    Stmt next = out.get(i + 1);
                    if (next != orgNext) {
                        if (stmt.st == ST.IF) {
                            JumpStmt jumpStmt = (JumpStmt) stmt;
                            if (jumpStmt.target == next) {
                                reverseIF(jumpStmt.op);
                                jumpStmt.target = (LabelStmt) orgNext;
                            } else {
                                JumpStmt gotoStmt = Stmts.nGoto((LabelStmt) orgNext);
                                stmts.add(gotoStmt);
                            }
                        } else {
                            JumpStmt gotoStmt = Stmts.nGoto((LabelStmt) orgNext);
                            stmts.add(gotoStmt);
                        }
                    }
                } else {
                    JumpStmt gotoStmt = Stmts.nGoto((LabelStmt) orgNext);
                    stmts.add(gotoStmt);
                }
            }
        }

        for (JumpStmt gotoStmt : gotos) {
            Stmt t = gotoStmt.getNext();
            while (t.st == ST.LABEL) {
                if (t == gotoStmt.target) {
                    stmts.remove(gotoStmt);
                    break;
                }
                t = t.getNext();
            }
        }
    }

    private List<Stmt> topologicalSort(StmtList stmts) {

        List<Stmt> out = new ArrayList<Stmt>(stmts.getSize());
        Stack<Stmt> stack = new Stack<Stmt>();
        stack.push(stmts.getFirst());

        boolean visitedFlag = false;

        while (!stack.empty()) {
            Stmt stmt = stack.pop();
            if (stmt._cfg_visited == visitedFlag) {
                continue;
            }
            if (stmt._cfg_froms.size() == 0 || stack.size() == 0) {
                stmt._cfg_visited = visitedFlag;
                out.add(stmt);
                for (Stmt t : stmt._cfg_tos) {
                    t._cfg_froms.remove(stmt);
                    if (t._cfg_visited != visitedFlag) {
                        stack.push(t);
                    }
                }
            }
        }
        return out;
    }

    private static class Item {
        public Stmt stmt;
        public Iterator<Stmt> it;
        public boolean visitedAdded = false;
    }

    private Item buildItem(Stmt stmt) {
        Item item = new Item();
        item.stmt = stmt;
        item.it = new ArrayList<Stmt>(stmt._cfg_tos).iterator();
        return item;
    }

    /**
     * A graph has a cycle if and only if depth-first search produces a back edge if there are
     * 
     * @param stmts
     */
    private void removeLoop(StmtList stmts) {
        if (stmts.getSize() < 50) {// use Recursive if no more than 50 stmts
            dfsRecursiveCallRemove(stmts.getFirst(), new HashSet<Stmt>());
        } else {
            dfsStackRemove(stmts.getFirst(), new HashSet<Stmt>());
        }
    }

    /**
     * Remove loop by store value in stack
     * 
     * @param stmt
     * @param visited
     */
    private void dfsStackRemove(Stmt stmt, Set<Stmt> visited) {
        Stack<Item> stack = new Stack<Item>();
        stack.push(buildItem(stmt));
        while (!stack.empty()) {
            Item item = stack.peek();
            stmt = item.stmt;
            item.stmt._cfg_visited = true;
            if (!item.visitedAdded) {
                visited.add(item.stmt);
                item.visitedAdded = true;
            }
            boolean needPop = true;
            Iterator<Stmt> it = item.it;
            while (it.hasNext()) {
                Stmt to = it.next();
                if (visited.contains(to)) {// a loop here
                    // cut the loop
                    to._cfg_froms.remove(stmt);
                    stmt._cfg_tos.remove(to);
                } else {
                    if (!to._cfg_visited) {
                        needPop = false;
                        stack.push(buildItem(to));
                        break;
                    }
                }
            }
            if (needPop) {
                if (item.visitedAdded) {
                    visited.remove(stmt);
                    stack.pop();
                }
            }
        }
    }

    /**
     * Remove Loop by Recursive Method Call, if there are 500+ stmt, we got a out-of-stack error
     * 
     * @param stmt
     * @param visited
     */
    private void dfsRecursiveCallRemove(Stmt stmt, Set<Stmt> visited) {
        if (stmt._cfg_visited) {// make sure every node in visited once
            return;
        } else {
            stmt._cfg_visited = true;
        }
        visited.add(stmt);
        // copy the _ts_tos, so we can modify the collection
        List<Stmt> tos = new ArrayList<Stmt>(stmt._cfg_tos);
        for (Stmt to : tos) {
            if (visited.contains(to)) {// a loop here
                // cut the loop
                to._cfg_froms.remove(stmt);
                stmt._cfg_tos.remove(to);
            } else {
                dfsRecursiveCallRemove(to, visited);
            }
        }
        visited.remove(stmt);
    }

    private static void link(Stmt from, Stmt to) {
        if (to == null) {// last stmt is a LabelStmt
            return;
        }
        from._cfg_tos.add(to);
        to._cfg_froms.add(from);
    }

    private void init(StmtList stmts, List<Trap> traps) {
        // 1. init _ts_default_next and insert label after IF stmt
        for (Stmt stmt = stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
            switch (stmt.st) {
            case IF:
                Stmt n = stmt.getNext();
                if (n != null && n.st != ST.LABEL) {
                    LabelStmt ls = Stmts.nLabel();
                    stmts.insertAftre(stmt, ls);
                }
                stmt._ts_default_next = stmt.getNext();
                break;
            case GOTO:
            case RETURN:
            case RETURN_VOID:
            case TABLE_SWITCH:
            case LOOKUP_SWITCH:
            case THROW:
                stmt._ts_default_next = null;
                break;
            default:
                stmt._ts_default_next = stmt.getNext();
                break;
            }
        }
        // 2. init cfg
        for (Stmt stmt = stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
            if (stmt._cfg_froms == null) {
                stmt._cfg_froms = new TreeSet<Stmt>(stmts);
            } else {
                stmt._cfg_froms.clear();
            }
            if (stmt._cfg_tos == null) {
                stmt._cfg_tos = new TreeSet<Stmt>(stmts);
            } else {
                stmt._cfg_tos.clear();
            }
        }
        // 2.1 link exception handler
        for (Trap t : traps) {
            for (Stmt s = t.start; s != t.end; s = s.getNext()) {
                link(s, t.handler);
            }
        }
        // 2.2 link normal
        for (Stmt stmt = stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
            switch (stmt.st) {
            case GOTO:
                link(stmt, ((JumpStmt) stmt).target);
                break;
            case IF:
                link(stmt, ((JumpStmt) stmt).target);
                link(stmt, stmt.getNext());
                break;
            case LOOKUP_SWITCH:
                LookupSwitchStmt lss = (LookupSwitchStmt) stmt;
                link(stmt, lss.defaultTarget);
                for (LabelStmt ls : lss.targets) {
                    link(stmt, ls);
                }
                break;
            case TABLE_SWITCH:
                TableSwitchStmt tss = (TableSwitchStmt) stmt;
                link(stmt, tss.defaultTarget);
                for (LabelStmt ls : tss.targets) {
                    link(stmt, ls);
                }
                break;
            case THROW:
            case RETURN:
            case RETURN_VOID:
                break;
            default:
                link(stmt, stmt.getNext());
                break;
            }
        }
        // 3. remove goto
        for (Stmt stmt = stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
            stmt._cfg_visited = false;
            switch (stmt.st) {
            case GOTO:
                JumpStmt js = (JumpStmt) stmt;
                for (Stmt f : stmt._cfg_froms) {
                    f._cfg_tos.remove(stmt);
                    f._cfg_tos.addAll(stmt._cfg_tos);
                    f._ts_default_next = js.target;
                }

                for (Stmt t : stmt._cfg_tos) {
                    t._cfg_froms.remove(stmt);
                    t._cfg_froms.addAll(stmt._cfg_froms);
                }
                break;
            }
        }
    }

    private void reverseIF(ValueBox op) {
        BinopExpr e2 = (BinopExpr) op.value;

        switch (e2.vt) {
        case GE:
            op.value = Exprs.nLt(e2.op1.value, e2.op2.value, e2.type);
            break;
        case GT:
            op.value = Exprs.nLe(e2.op1.value, e2.op2.value, e2.type);
            break;
        case LT:
            op.value = Exprs.nGe(e2.op1.value, e2.op2.value, e2.type);
            break;
        case LE:
            op.value = Exprs.nGt(e2.op1.value, e2.op2.value, e2.type);
            break;
        case EQ:
            op.value = Exprs.nNe(e2.op1.value, e2.op2.value, e2.type);
            break;
        case NE:
            op.value = Exprs.nEq(e2.op1.value, e2.op2.value, e2.type);
            break;
        }
    }
}
