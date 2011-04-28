package pxb.xjimple.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import pxb.xjimple.Trap;
import pxb.xjimple.stmt.JumpStmt;
import pxb.xjimple.stmt.LabelStmt;
import pxb.xjimple.stmt.LookupSwitchStmt;
import pxb.xjimple.stmt.Stmt;
import pxb.xjimple.stmt.StmtList;
import pxb.xjimple.stmt.TableSwitchStmt;

public class StmtGraph implements DirectedGraph<Stmt> {

    private Set<Stmt> tails = Collections.emptySet();
    private Set<Stmt> heads = Collections.emptySet();
    StmtList stmtList;

    public StmtGraph(StmtList stmtList, List<Trap> traps) {
        this.stmtList = stmtList;
        buildExceptionalEdges(stmtList, traps);
        buildUnexceptionalEdges(stmtList);
        buildHeadsAndTails(stmtList);
    }

    private void link(Stmt a, Stmt b) {
        if (!a._gSuccs.contains(b)) {
            a._gSuccs.add(b);
        }
        if (!b._gPreds.contains(a)) {
            b._gPreds.add(a);
        }
    }

    public Set<Stmt> getHeads() {
        return heads;
    }

    public Set<Stmt> getTails() {
        return tails;
    }

    public List<Stmt> getSuccsOf(Stmt stmt) {
        return stmt._gSuccs;
    }

    public List<Stmt> getPredsOf(Stmt stmt) {
        return stmt._gPreds;
    }

    protected void buildHeadsAndTails(StmtList unitChain) {

        for (Stmt stmt : unitChain) {
            Collection<Stmt> succs = getSuccsOf(stmt);
            if (succs.size() == 0) {
                tails.add(stmt);
            }
            Collection<Stmt> preds = getPredsOf(stmt);
            if (preds.size() == 0) {
                heads.add(stmt);
            }
        }

        // Add the first Unit, even if it is the target of
        // a branch.
        heads.add(unitChain.getFirst());
    }

    protected void buildExceptionalEdges(StmtList stmts, List<Trap> traps) {
        for (Trap trap : traps) {
            for (Stmt p = trap.start.getNext(); p != null && p != trap.end; p = p.getNext()) {
                link(p, trap.handler);
            }
        }
    }

    protected void buildUnexceptionalEdges(StmtList stmts) {

        Stmt current = null;
        Stmt next = null;
        Iterator<Stmt> it = stmts.iterator();
        if (it.hasNext()) {
            current = it.next();
        }
        while (it.hasNext()) {
            next = it.next();
            switch (current.st) {
                case IF:
                    link(current, next);
                    break;
                case GOTO:
                    link(current, next);
                    link(current, ((JumpStmt) current).target);
                    break;
                case LOOKUP_SWITCH:
                    LookupSwitchStmt lookupSwitchStmt = (LookupSwitchStmt) current;
                    link(current, lookupSwitchStmt.defaultTarget);
                    for (LabelStmt s : lookupSwitchStmt.targets) {
                        link(current, s);
                    }
                    break;
                case TABLE_SWITCH:
                    TableSwitchStmt tableSwitchStmt = (TableSwitchStmt) current;
                    link(current, tableSwitchStmt.defaultTarget);
                    for (LabelStmt s : tableSwitchStmt.targets) {
                        link(current, s);
                    }
                    break;
                case RETURN:
                case RETURN_VOID:
                case THROW:
                    break;
                default:
                    link(current, next);
            }
            current = next;
        }
    }

    @Override
    public int size() {
        return stmtList.getSize();
    }

    @Override
    public Iterator<Stmt> iterator() {
        return stmtList.iterator();
    }
}
