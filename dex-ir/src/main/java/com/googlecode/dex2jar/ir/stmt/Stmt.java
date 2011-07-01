package com.googlecode.dex2jar.ir.stmt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.googlecode.dex2jar.ir.ValueBox;

public abstract class Stmt {
    public static enum ST {
        ASSIGN, GOTO, IDENTITY, IF, LABEL, LOCK, LOOKUP_SWITCH, NOP, RETURN, RETURN_VOID, TABLE_SWITCH, THROW, UNLOCK
    }

    public Set<Stmt> _cfg_froms;
    public Set<Stmt> _cfg_tos;
    public ValueBox[] _ls_frame;
    public Set<Stmt> _ls_traps = new HashSet<Stmt>();
    public boolean _ls_visited;

    /* default */StmtList list;
    /* default */Stmt next;

    /* default */Stmt pre;

    public final ST st;

    public Stmt(ST type) {
        this.st = type;
    }

    public abstract Stmt clone(Map<LabelStmt, LabelStmt> map);

    public final Stmt getNext() {
        return next;
    }

    public final Stmt getPre() {
        return pre;
    }

}
