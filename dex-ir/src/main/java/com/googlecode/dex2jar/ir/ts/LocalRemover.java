package com.googlecode.dex2jar.ir.ts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.BinopExpr;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.NewMutiArrayExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.expr.UnopExpr;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;

public class LocalRemover implements Transformer {

    public static Type NEW_TYPE = Type.getType("Lc.g.d.i.t.LocalRemover$NEW;");

    @Override
    public void transform(IrMethod je) {
        StmtList list = je.stmts;
        List<Stmt> orderList = list._ls_visit_order;

        for (int p = 0; p < orderList.size(); p++) {
            Stmt st = orderList.get(p);
            if (st == null || !list.contains(st)) {
                continue;
            }
            switch (st.st) {
            case ASSIGN:
            case IDENTITY:
                AssignStmt as = (AssignStmt) st;
                if (as.op1.value.vt == VT.LOCAL) {
                    Local aLeft = (Local) as.op1.value;
                    if (as.op2.value.vt == VT.CONSTANT) {// remove new
                        Constant c = (Constant) as.op2.value;
                        if (NEW_TYPE.equals(c.type)) {
                            list.remove(st);
                            for (Iterator<AssignStmt> it = list._ls_inits.iterator(); it.hasNext();) {
                                AssignStmt stmt = it.next();
                                InvokeExpr ie = (InvokeExpr) stmt.op2.value;
                                if (ie.ops[0].value == aLeft) {
                                    it.remove();
                                    ValueBox[] vb = new ValueBox[ie.ops.length - 1];
                                    System.arraycopy(ie.ops, 1, vb, 0, vb.length);
                                    AssignStmt nas = Stmts.nAssign(as.op1,
                                            new ValueBox(Exprs.nInvokeNew(vb, ie.argmentTypes, ie.methodOwnerType)));
                                    list.replace(stmt, nas);
                                    aLeft._ls_read_count--;
                                    orderList.set(orderList.indexOf(stmt), nas);
                                }
                            }
                            continue;
                        }
                    }
                    if (aLeft._ls_write_count == 1) {
                        switch (as.op2.value.vt) {
                        case LOCAL: {
                            Local b = (Local) as.op2.value;
                            b._ls_read_count += aLeft._ls_read_count - 1;
                            je.locals.remove(aLeft);
                            aLeft._ls_vb.value = b;
                            list.remove(st);
                            orderList.set(p, null);
                            continue;
                        }
                        case CONSTANT: {
                            as.op1.value = as.op2.value;
                            je.locals.remove(aLeft);
                            list.remove(st);
                            orderList.set(p, null);
                            continue;
                        }
                        }
                    }

                }
            }
        }

        list._ls_inits = null;

        List<ValueBox> vbs = new ArrayList<ValueBox>(20);

        Stack<ValueBox> tmp = new Stack<ValueBox>();

        for (int p = 0; p < orderList.size(); p++) {
            Stmt st = orderList.get(p);
            if (st == null || !list.contains(st)) {
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
                Local preLocal = (Local) as.op1.value;
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
                        vb.value = as.op2.value;
                        list.remove(as);
                        je.locals.remove(preLocal);
                        pre = st.getPre();
                        if (pre != null && canRemove(pre)) {
                            as = (AssignStmt) pre;
                            preLocal = (Local) as.op1.value;
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
        case IDENTITY:
            AssignStmt as = (AssignStmt) pre;
            if (as.op1.value.vt == VT.LOCAL) {
                Local aLeft = (Local) as.op1.value;
                if (aLeft._ls_write_count == 1 && aLeft._ls_read_count == 1) {
                    switch (as.op2.value.vt) {
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
        case IDENTITY:
            AssignStmt as = (AssignStmt) st;
            if (as.op1.value.vt != VT.LOCAL) {
                execValue(stack, as.op1, local);
            }
            execValue(stack, as.op2, local);
            break;
        case IF:
            JumpStmt js = (JumpStmt) st;
            execValue(stack, js.op, local);
            break;
        case LOOKUP_SWITCH:
            LookupSwitchStmt lss = (LookupSwitchStmt) st;
            execValue(stack, lss.op, local);
            break;
        case TABLE_SWITCH:
            TableSwitchStmt tss = (TableSwitchStmt) st;
            execValue(stack, tss.op, local);
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
            stack.add(ae.op1);
            stack.add(ae.op2);
            break;
        case FIELD:
            FieldExpr fe = (FieldExpr) toReplace;
            if (fe.op != null) {// not a static field
                stack.add(fe.op);
            }
            break;
        case CAST:
            TypeExpr ce = (TypeExpr) toReplace;
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
        case NEW_ARRAY:
        case INSTANCEOF:
            TypeExpr iof = (TypeExpr) toReplace;
            stack.add(iof.op);
            break;
        case INVOKE_INTERFACE:
        case INVOKE_SPECIAL:
        case INVOKE_VIRTUAL:
        case INVOKE_NEW:
        case INVOKE_STATIC:
            InvokeExpr ie = (InvokeExpr) toReplace;
            for (ValueBox vb : ie.ops) {
                stack.add(vb);
            }
            break;
        case LENGTH:
        case NEG:
        case NOT:
            UnopExpr ue = (UnopExpr) toReplace;
            stack.add(ue.op);
            break;

        case NEW_MUTI_ARRAY:
            NewMutiArrayExpr nmae = (NewMutiArrayExpr) toReplace;
            for (ValueBox vb : nmae.ops) {
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
