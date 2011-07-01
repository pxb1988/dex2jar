package pxb.xjimple.ts;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import pxb.xjimple.JimpleMethod;
import pxb.xjimple.Local;
import pxb.xjimple.Value;
import pxb.xjimple.Value.VT;
import pxb.xjimple.ValueBox;
import pxb.xjimple.expr.ArrayExpr;
import pxb.xjimple.expr.BinopExpr;
import pxb.xjimple.expr.CastExpr;
import pxb.xjimple.expr.FieldExpr;
import pxb.xjimple.expr.InstanceOfExpr;
import pxb.xjimple.expr.InvokeExpr;
import pxb.xjimple.expr.NewArrayExpr;
import pxb.xjimple.expr.NewMutiArrayExpr;
import pxb.xjimple.expr.UnopExpr;
import pxb.xjimple.stmt.AssignStmt;
import pxb.xjimple.stmt.JumpStmt;
import pxb.xjimple.stmt.LookupSwitchStmt;
import pxb.xjimple.stmt.Stmt;
import pxb.xjimple.stmt.StmtList;
import pxb.xjimple.stmt.TableSwitchStmt;
import pxb.xjimple.stmt.UnopStmt;

public class LocalRemover implements Transformer {

    @Override
    public void transform(JimpleMethod je) {
        StmtList list = je.stmts;
        for (Stmt st : je._ls_visit_order) {
            if (!list.contains(st)) {
                continue;
            }
            switch (st.st) {
            case ASSIGN:
                AssignStmt as = (AssignStmt) st;
                if (as.left.value.vt == VT.LOCAL) {
                    Local aLeft = (Local) as.left.value;
                    if (aLeft._ls_write_count == 1) {
                        switch (as.right.value.vt) {
                        case LOCAL: {
                            Local b = (Local) as.right.value;
                            b._ls_read_count += aLeft._ls_read_count - 1;
                            je.locals.remove(aLeft);
                            aLeft._ls_vb.value = b;
                            list.remove(st);
                            continue;
                        }
                        case CONSTANT: {
                            as.left.value = as.right.value;
                            je.locals.remove(aLeft);
                            list.remove(st);
                            continue;
                        }
                        }
                    }
                }
            }
        }

        List<ValueBox> vbs = new ArrayList<ValueBox>(20);

        Stack<ValueBox> tmp = new Stack<ValueBox>();

        for (Stmt st : je._ls_visit_order) {
            if (!list.contains(st)) {
                continue;
            }

            switch (st.st) {
            case RETURN_VOID:
            case LABEL:
            case GOTO:
            case NOP:
            case IDENTITY:
                continue;
            }

            Stmt pre = st.getPre();
            if (pre == null) {
                continue;
            }

            if (canRemove(pre)) {
                vbs.clear();
                tmp.clear();
                AssignStmt as = (AssignStmt) pre;
                Local preLocal = (Local) as.left.value;
                execStmt(vbs, st, preLocal);
                for (ValueBox vb : vbs) {
                    switch (vb.value.vt) {
                    case LOCAL:
                        tmp.push(vb);
                        continue;
                    case CONSTANT:
                        continue;
                    }
                    break;
                }
                while (!tmp.isEmpty()) {
                    ValueBox vb = tmp.pop();
                    if (vb.value == preLocal) {
                        vb.value = as.right.value;
                        list.remove(as);
                        je.locals.remove(preLocal);
                        pre = st.getPre();
                        if (pre != null && canRemove(pre)) {
                            as = (AssignStmt) pre;
                            preLocal = (Local) as.left.value;
                        } else {
                            break;
                        }
                    }
                }

            }

        }
    }

    private static boolean canRemove(Stmt pre) {
        switch (pre.st) {
        case ASSIGN:
            AssignStmt as = (AssignStmt) pre;
            if (as.left.value.vt == VT.LOCAL) {
                Local aLeft = (Local) as.left.value;
                if (aLeft._ls_write_count == 1 && aLeft._ls_read_count == 1) {
                    switch (as.right.value.vt) {
                    case THIS_REF:
                    case PARAMETER_REF:
                    case EXCEPTION_REF:
                        return false;
                    default:
                        return true;
                    }
                }
            }
        }
        return false;

    }

    /**
     * 
     * @param stack
     * @param st
     * @param local
     */
    static private void execStmt(List<ValueBox> stack, Stmt st, Local local) {

        switch (st.st) {
        case ASSIGN:
            AssignStmt as = (AssignStmt) st;
            if (as.left.value.vt != VT.LOCAL) {
                execValue(stack, as.left, local);
            }
            execValue(stack, as.right, local);
            break;
        case IF:
            JumpStmt js = (JumpStmt) st;
            execValue(stack, js.condition, local);
            break;
        case LOOKUP_SWITCH:
            LookupSwitchStmt lss = (LookupSwitchStmt) st;
            execValue(stack, lss.key, local);
            break;
        case TABLE_SWITCH:
            TableSwitchStmt tss = (TableSwitchStmt) st;
            execValue(stack, tss.key, local);
            break;
        case RETURN:
        case LOCK:
        case UNLOCK:
        case THROW:
            UnopStmt us = (UnopStmt) st;
            execValue(stack, us.op, local);
            break;
        }
    }

    static private void execValue(List<ValueBox> stack, ValueBox left, Local local) {
        Value toReplace = left.value;
        switch (toReplace.vt) {
        case LOCAL:
            stack.add(left);
            break;
        case CONSTANT:
            break;

        case AND:
        case OR:
        case XOR:
        case ADD:
        case MUL:
        case DIV:
        case SUB:
        case REM:
        case SHL:
        case USHR:
        case SHR: {
            BinopExpr be = (BinopExpr) toReplace;
            switch (be.vt) {
            case AND:
            case OR:
            case XOR:
            case ADD:
            case MUL:
                if (be.op1.value == local) {
                    ValueBox tmp = be.op1;
                    be.op1 = be.op2;
                    be.op2 = tmp;
                }
                break;
            }

            stack.add(be.op1);
            stack.add(be.op2);
            break;
        }
        case ARRAY:
            ArrayExpr ae = (ArrayExpr) toReplace;
            stack.add(ae.base);
            stack.add(ae.index);
            break;
        case FIELD:
            FieldExpr fe = (FieldExpr) toReplace;
            if (fe.object != null) {// not a static field
                stack.add(fe.object);
            }
            break;
        case CAST:
            CastExpr ce = (CastExpr) toReplace;
            stack.add(ce.op);
            break;
        case CMP:
        case CMPG:
        case CMPL:
        case EQ:
        case GE:
        case GT:
        case LE:
        case LT:
        case NE: {
            BinopExpr be = (BinopExpr) toReplace;
            stack.add(be.op1);
            stack.add(be.op2);
            break;
        }
        case INSTANCEOF:
            InstanceOfExpr iof = (InstanceOfExpr) toReplace;
            stack.add(iof.op);
            break;
        case INVOKE_INTERFACE:
        case INVOKE_SPECIAL:
        case INVOKE_VIRTUAL:
        case INVOKE_NEW:
        case INVOKE_STATIC:
            InvokeExpr ie = (InvokeExpr) toReplace;
            if (ie.object != null) {
                stack.add(ie.object);
            }
            for (ValueBox vb : ie.args) {
                stack.add(vb);
            }
            break;
        case LENGTH:
        case NEG:
            UnopExpr ue = (UnopExpr) toReplace;
            stack.add(ue.op);
            break;
        case NEW_ARRAY:
            NewArrayExpr nae = (NewArrayExpr) toReplace;
            stack.add(nae.size);
            break;
        case NEW_MUTI_ARRAY:
            NewMutiArrayExpr nmae = (NewMutiArrayExpr) toReplace;
            for (ValueBox vb : nmae.sizes) {
                stack.add(vb);
            }
            break;
        case THIS_REF:
        case PARAMETER_REF:
        case EXCEPTION_REF:
            break;
        }
    }

}
