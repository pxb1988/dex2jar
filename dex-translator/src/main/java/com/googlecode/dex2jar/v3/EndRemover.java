package com.googlecode.dex2jar.v3;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Transformer;

public class EndRemover implements Transformer {

    boolean isSimple(Stmt stmt) {
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
                break;
            case THROW:
            case IF:
            case LOCK:
            case UNLOCK:
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

    @Override
    public void transform(IrMethod irMethod) {
        Map<LabelStmt, LabelStmt> map = new HashMap<LabelStmt, LabelStmt>();
        StmtList list = irMethod.stmts;
        for (Stmt st = list.getFirst(); st != null; st = st.getNext()) {
            if (st.st == ST.GOTO) {
                JumpStmt js = (JumpStmt) st;
                if (isSimple(js.target)) {
                    boolean end = false;
                    for (Stmt p = js.target.getNext(); !end; p = p.getNext()) {
                        switch (p.st) {
                        case LABEL:
                            break;
                        case GOTO:
                            list.insertBefore(js, Stmts.nGoto(((JumpStmt) p).target));
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
                    Stmt tmp = st.getPre();
                    list.remove(st);
                    st = tmp.getPre();
                }
            } else if (st.st == ST.IF) {
                JumpStmt js = (JumpStmt) st;
                if (map.containsKey(js.target)) {
                    js.target = map.get(js.target);
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
                            list.add(Stmts.nGoto(((JumpStmt) p).target));
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
        }
    }
}
