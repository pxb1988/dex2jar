package pxb.xjimple.stmt;

import java.util.Map;

public abstract class Stmt {
    public static enum ST {
        ASSIGN, GOTO, IDENTITY, IF, LOOKUP_SWITCH, NOP, TABLE_SWITCH, LABEL
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

}
