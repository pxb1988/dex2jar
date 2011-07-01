package com.googlecode.dex2jar.ir.ts;

import java.util.HashSet;
import java.util.Set;

import com.googlecode.dex2jar.ir.JimpleMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;

public class CfgCreater implements Transformer {

    static void link(Stmt from, Stmt to) {
        from._cfg_tos.add(to);
        to._cfg_froms.add(from);
    }

    @Override
    public void transform(JimpleMethod jm) {
        for (Trap t : jm.traps) {
            for (Stmt s = t.start.getNext(); s != t.end; s = s.getNext()) {
                link(s, t.handler);
            }
        }
        Set<Stmt> tails = new HashSet<Stmt>();

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
            case RETURN:
            case RETURN_VOID:
                tails.add(st);
                break;
            }

            
        }
        jm._cfg_tails=tails;
    }

}
