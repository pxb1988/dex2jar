package pxb.xjimple.stmt;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class Stmt {
    public static enum ST {
        ASSIGN, GOTO, IDENTITY, IF, LOOKUP_SWITCH, NOP, TABLE_SWITCH, LABEL, RETURN, RETURN_VOID, LOCK, UNLOCK, THROW
    }

    /* default */Stmt next;
    /* default */Stmt pre;
    /* default */StmtList list;

    public final Stmt getNext() {
        return next;
    }

    public final Stmt getPre() {
        return pre;
    }

    public final ST st;

    public Stmt(ST type) {
        this.st = type;
    }

    public abstract Stmt clone(Map<LabelStmt, LabelStmt> map);

    /* ========= */
    public Set<Stmt> gPreds = Collections.emptySet();
    public Set<Stmt> gSuccs = Collections.emptySet();

}
