package com.googlecode.dex2jar.ir.ts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;

/**
 * Try to clean following between a {@link Trap}
 * <ol>
 * <li>Move {@link Stmt}s outside a {@link Trap} if {@link Stmt}s are not throw</li>
 * <li>Remove {@link Trap} if all {@link Stmt}s are not throw</li>
 * </ol>
 * 
 * @author bob
 * 
 */
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
        for (Trap trap : new ArrayList<Trap>(irMethod.traps)) {// copy the list and we can remove one from original list
            LabelStmt start = null;
            boolean removeTrap = true;
            for (Stmt p = trap.start.getNext(); p != null && p != trap.end;) {
                boolean notThrow = Cfg.notThrow(p);
                if (!notThrow) {
                    start = null;
                    p = p.getNext();
                    removeTrap = false;
                    continue;
                }
                switch (p.st) {
                case LABEL:
                    if (start != null) {
                        move4Label(irMethod.stmts, start, p.getPre(), (LabelStmt) p);
                    }
                    start = (LabelStmt) p;
                    p = p.getNext();

                    break;
                case GOTO:
                case RETURN:
                case RETURN_VOID:
                    if (start != null) {
                        Stmt tmp = p.getNext();
                        move4End(irMethod.stmts, start, p);
                        start = null;
                        p = tmp;
                    } else {
                        p = p.getNext();
                    }
                    break;
                default:
                    p = p.getNext();
                }
            }
            if (removeTrap) {
                irMethod.traps.remove(trap);
            }
        }
    }

    private void move4Label(StmtList stmts, LabelStmt start, Stmt end, LabelStmt label) {
        move4End(stmts, start, end);
        stmts.insertAftre(end, Stmts.nGoto(label));
    }

    private void move4End(StmtList stmts, LabelStmt start, Stmt end) {
        Stmt g1 = Stmts.nGoto(start);
        stmts.insertBefore(start, g1);
        Stmt last = stmts.getLast();
        while (last.st == ST.GOTO && ((JumpStmt) last).target == start) {
            stmts.remove(last);
            last = stmts.getLast();
        }
        stmts.move(start, end, last);

    }
}
