package pxb.xjimple.stmt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pxb.xjimple.ValueBox;

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

    public ValueBox[] _ls_frame;
    public boolean _ls_visited;
    public Set<Stmt> _ls_traps = new HashSet<Stmt>();

    public final Stmt getPre() {
        return pre;
    }

    public final ST st;

    public Stmt(ST type) {
        this.st = type;
    }

    public abstract Stmt clone(Map<LabelStmt, LabelStmt> map);

}
