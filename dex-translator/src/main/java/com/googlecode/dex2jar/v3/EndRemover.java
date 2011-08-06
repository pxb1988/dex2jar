package com.googlecode.dex2jar.v3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Cfg;
import com.googlecode.dex2jar.ir.ts.Transformer;

public class EndRemover implements Transformer {

    static boolean isSimple(Stmt stmt) {
        while (stmt != null) {
            switch (stmt.st) {
            case ASSIGN:
            case IDENTITY:
                E2Stmt e2 = (E2Stmt) stmt;
                if (e2.op1.value.vt != VT.LOCAL) {
                    return false;
                }
                switch (e2.op2.value.vt) {
                case LOCAL:
                case CONSTANT:
                    break;
                default:
                    return false;
                }
                break;
            case LABEL:
            case NOP:
            case UNLOCK:
                break;
            case THROW:
            case IF:
            case LOCK:
            case LOOKUP_SWITCH:
            case TABLE_SWITCH:
                return false;
            case RETURN:
            case RETURN_VOID:
            case GOTO:
                return true;
            }
            stmt = stmt.getNext();
        }
        return true;
    }

    static void directJump(StmtList list) {
        Map<LabelStmt, LabelStmt> map = new HashMap<LabelStmt, LabelStmt>();
        for (Stmt st = list.getFirst(); st != null;) {

            switch (st.st) {
            case GOTO: {
                JumpStmt js = (JumpStmt) st;
                if (isSimple(js.target)) {
                    boolean end = false;
                    for (Stmt p = js.target.getNext(); !end; p = p.getNext()) {
                        switch (p.st) {
                        case LABEL:
                            break;
                        case GOTO:
                            LabelStmt target = ((JumpStmt) p).target;
                            list.insertBefore(js, Stmts.nGoto(target));
                            end = true;
                            break;
                        case RETURN:
                        case RETURN_VOID:
                            list.insertBefore(js, p.clone(null));
                            end = true;
                            break;
                        default:
                            list.insertBefore(js, p.clone(null));
                            break;
                        }
                    }
                    Stmt tmp = st.getNext();
                    list.remove(st);
                    st = tmp;
                } else {
                    st = st.getNext();
                }
                break;
            }
            case IF: {
                JumpStmt js = (JumpStmt) st;
                if (map.containsKey(js.target)) {
                    LabelStmt nTarget = map.get(js.target);
                    js.target = nTarget;
                } else if (isSimple(js.target)) {
                    LabelStmt nTarget = Stmts.nLabel();
                    map.put(js.target, nTarget);
                    list.add(nTarget);
                    boolean end = false;
                    for (Stmt p = js.target.getNext(); !end; p = p.getNext()) {
                        switch (p.st) {
                        case LABEL:
                            break;
                        case GOTO:
                            LabelStmt target = ((JumpStmt) p).target;
                            list.add(Stmts.nGoto(target));
                            end = true;
                            break;
                        case RETURN:
                        case RETURN_VOID:
                            list.add(p.clone(null));
                            end = true;
                            break;
                        default:
                            list.add(p.clone(null));
                            break;
                        }
                    }
                    js.target = nTarget;

                }
            }
                st = st.getNext();
                break;

            default: {
                st = st.getNext();
                break;
            }
            }
        }
    }

    @Override
    public void transform(IrMethod irMethod) {
        for (Iterator<Trap> it = irMethod.traps.iterator(); it.hasNext();) {
            Trap trap = it.next();

            LabelStmt start = null;

            boolean allNotThrow = true;
            for (Stmt p = trap.start.getNext(); p != null && p != trap.end;) {
                boolean notThrow = Cfg.notThrow(p);
                if (!notThrow) {
                    allNotThrow = false;
                    start = null;
                    p = p.getNext();
                    continue;
                }
                switch (p.st) {
                case LABEL:
                    start = (LabelStmt) p;
                    p = p.getNext();
                    break;
                case GOTO:
                case RETURN:
                case RETURN_VOID:
                    if (start != null) {
                        Stmt tmp = p.getNext();
                        move(irMethod.stmts, start, p);
                        p = tmp;
                    } else {
                        p = p.getNext();
                    }
                    break;
                default:
                    p = p.getNext();
                }
            }
            if (allNotThrow) {
                it.remove();
            }

        }
    }

    private void move(StmtList stmts, LabelStmt start, Stmt p) {
        stmts.insertBefore(start, Stmts.nGoto(start));
        stmts.move(start, p, stmts.getLast());
    }
}
