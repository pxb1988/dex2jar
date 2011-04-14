package pxb.xjimple.graph;

import java.util.Iterator;
import java.util.List;

import pxb.xjimple.Trap;
import pxb.xjimple.stmt.JumpStmt;
import pxb.xjimple.stmt.LabelStmt;
import pxb.xjimple.stmt.LookupSwitchStmt;
import pxb.xjimple.stmt.Stmt;
import pxb.xjimple.stmt.StmtList;
import pxb.xjimple.stmt.TableSwitchStmt;

public class StmtGraph {

    public void link(Stmt a, Stmt b) {
        a.gSuccs.add(b);
        b.gPreds.add(a);
    }

    public void buildExceptionalEdges(StmtList stmts, List<Trap> traps) {
        for (Trap trap : traps) {
            for (Stmt p = trap.start; p != null && p != trap.end; p = p.getNext()) {
                link(p, trap.handler);
            }
        }
    }

    public void buildUnexceptionalEdges(StmtList stmts) {

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
            default:
                link(current, next);
            }
            current = next;
        }
    }
}
