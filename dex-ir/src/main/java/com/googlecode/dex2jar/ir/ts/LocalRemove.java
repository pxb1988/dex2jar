/*
 * Copyright (c) 2009-2011 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;

/**
 * TODO DOC
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class LocalRemove implements Transformer {

    public static Type NEW_TYPE = Type.getType("Lc.g.d.i.t.LocalRemover$NEW;");

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
     * calculate express execution order of current statement
     * 
     * @param vbs
     *            valueBox order list
     * @param st
     *            current statement
     * @param local
     *            local in left of pre statement
     */
    static private void execStmt(List<ValueBox> vbs, Stmt st, Local local) {

        switch (st.st) {
        case ASSIGN:
        case IDENTITY:
            AssignStmt as = (AssignStmt) st;
            if (as.op1.value.vt != VT.LOCAL) {
                execValue(vbs, as.op1, local);
            }
            execValue(vbs, as.op2, local);
            break;
        case IF:
            JumpStmt js = (JumpStmt) st;
            execValue(vbs, js.op, local);
            break;
        case LOOKUP_SWITCH:
            LookupSwitchStmt lss = (LookupSwitchStmt) st;
            execValue(vbs, lss.op, local);
            break;
        case TABLE_SWITCH:
            TableSwitchStmt tss = (TableSwitchStmt) st;
            execValue(vbs, tss.op, local);
            break;
        case RETURN:
        case LOCK:
        case UNLOCK:
        case THROW:
            UnopStmt us = (UnopStmt) st;
            execValue(vbs, us.op, local);
            break;
        }
    }

    static private void execValue(List<ValueBox> vbs, ValueBox valueBox, Local local) {
        Value toReplace = valueBox.value;

        switch (toReplace.et) {
        case E0:
            switch (toReplace.vt) {
            case LOCAL:
                vbs.add(valueBox);
                break;
            }
            break;
        case E1:
            ValueBox op = ((E1Expr) toReplace).op;
            if (op != null) {// op for static file is null
                vbs.add(op);
            }
            break;
        case E2:
            E2Expr e2 = (E2Expr) toReplace;
            switch (e2.vt) {
            case AND:
            case OR:
            case XOR:
            case ADD:
            case MUL:
                if (e2.op1.value == local && (e2.op1.value.vt == VT.LOCAL || e2.op1.value.vt == VT.CONSTANT)) {
                    ValueBox tmp = e2.op1;
                    e2.op1 = e2.op2;
                    e2.op2 = tmp;
                }
                break;
            }

            vbs.add(e2.op1);
            vbs.add(e2.op2);
            break;

        case En:
            EnExpr ie = (EnExpr) toReplace;
            for (ValueBox vb : ie.ops) {
                vbs.add(vb);
            }
            break;
        }
    }

    /**
     * the statements must be simplest.
     * 
     * <pre>
     * a = b + c; // ok
     * a = b[0] + c;// may cause exception, must expend to tmp1=b[0];a=tmp1+c;
     * </pre>
     * 
     * @see com.googlecode.dex2jar.ir.ts.Transformer#transform(com.googlecode.dex2jar.ir.IrMethod)
     */
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

}
