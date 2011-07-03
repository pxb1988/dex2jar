package com.googlecode.dex2jar.ir.stmt;

import java.util.Set;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.ValueBox;

public abstract class Stmt {
    public static enum ST {
        ASSIGN, GOTO, IDENTITY, IF, LABEL, LOCK, LOOKUP_SWITCH, NOP, RETURN, RETURN_VOID, TABLE_SWITCH, THROW, UNLOCK
    }

    public Set<Stmt> _cfg_froms;
    public Set<Stmt> _cfg_tos;
    public ValueBox[] _ls_forward_frame;
    public ValueBox[] _ls_backward_frame;

    public boolean _cfg_visited;

    /* default */StmtList list;
    /* default */Stmt next;

    /* default */Stmt pre;

    public final ST st;
    public final ET et;

    public Stmt(ST type, ET et) {
        this.st = type;
        this.et = et;
    }

    public final Stmt getNext() {
        return next;
    }

    public final Stmt getPre() {
        return pre;
    }

    public static abstract class E0Stmt extends Stmt {
        public E0Stmt(ST type) {
            super(type, ET.E0);
        }
    }

    public static abstract class E1Stmt extends Stmt {
        public ValueBox op;

        public E1Stmt(ST type, ValueBox op) {
            super(type, ET.E1);
            this.op = op;
        }
    }

    public static abstract class E2Stmt extends Stmt {
        public ValueBox op1;
        public ValueBox op2;

        public E2Stmt(ST type, ValueBox op1, ValueBox op2) {
            super(type, ET.E2);
            this.op1 = op1;
            this.op2 = op2;
        }
    }

    public static abstract class EnStmt extends Stmt {
        public ValueBox[] ops;

        public EnStmt(ST type, ValueBox[] ops) {
            super(type, ET.E1);
            this.ops = ops;
        }
    }

}
