/*
 * Copyright (c) 2009-2012 Panxiaobo
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
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FilledArrayExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.NewExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;

/**
 * TODO DOC
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class LocalRemove implements Transformer {

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
                    if (as.op2.value.vt == VT.NEW) {// remove new
                        NewExpr c = (NewExpr) as.op2.value;
                        boolean replaced = false;
                        for (Iterator<AssignStmt> it = list._ls_inits.iterator(); it.hasNext();) {
                            AssignStmt stmt = it.next();
                            InvokeExpr ie = (InvokeExpr) stmt.op2.value;
                            if (ie.ops[0].value == aLeft && ie.methodOwnerType.equals(c.type)) {
                                list.remove(st);
                                it.remove();
                                je.locals.remove(stmt.op1.value);
                                ValueBox[] vb = new ValueBox[ie.ops.length - 1];
                                System.arraycopy(ie.ops, 1, vb, 0, vb.length);
                                AssignStmt nas = Stmts.nAssign(as.op1,
                                        new ValueBox(Exprs.nInvokeNew(vb, ie.argmentTypes, ie.methodOwnerType)));
                                list.replace(stmt, nas);
                                aLeft._ls_read_count--;
                                orderList.set(orderList.indexOf(stmt), nas);
                                replaced = true;
                                break;
                            }
                        }
                        if (replaced) {
                            continue;
                        }
                    }
                    if (aLeft._ls_write_count == 1) {
                        switch (as.op2.value.vt) {
                        case LOCAL: {
                            Local b = (Local) as.op2.value;
                            if (b._ls_write_count == 1) {// if b is only write for once
                                b._ls_read_count += aLeft._ls_read_count - 1;
                                je.locals.remove(aLeft);
                                aLeft._ls_vb.value = b;
                                list.remove(st);
                                orderList.set(p, null);
                            }
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

        /* 
         * Merge some simple filled arrays
         * merge:
         * a[][]=new xx[2][];a[0]=new xx[2];a[0][0]=b;a[0][1]=c;a[1]=new xx[2];a[1][0]=d;a[1][1]=e;	->	a[][]=new xx[][]{{b,c},{d,e}};
         * tmp[][]=new xx[][]{{b,c},{d,e}};a=tmp;	->	a=new xx[][]{{b,c},{d,e}};
         * not merge:
         * a[]=new xx[3];a[0]=b;a[1]=c;(Not full)
         * a[]=new xx[2];a[0]=b;a[1]=c.gg();(Not simple)
         */
        boolean changed = false;
        do {
            changed = false;
            for (int p = 0; p < orderList.size(); p++) {
                Stmt st = orderList.get(p);
                if (st == null || !list.contains(st)) {
                    continue;
                }
                switch (st.st) {
                
                case ASSIGN:
                    AssignStmt as = (AssignStmt) st;
                    if (as.op2.value.vt == VT.NEW_ARRAY) {
                        TypeExpr te = (TypeExpr)as.op2.value;
                        Value val = as.op1.value;
                        if (te.op.value instanceof Constant) {
                            int arraySize = (Integer)((Constant)te.op.value).value;
                            Type type = te.type;
                            int size = 0;
                            int empty = 0;
                            //Verify array data
                            for (int j = 1; j < orderList.size() - p; j++) {
                                Stmt st2 = orderList.get(p + j);
                                if(st2 == null) {
                                    empty ++;
                                    continue;
                                }
                                if (st2.st == ST.ASSIGN) {
                                    AssignStmt as2 = (AssignStmt) st2;
                                    if(as2.op1.value.vt == VT.ARRAY) {
                                        ArrayExpr ae2 = (ArrayExpr)as2.op1.value;
                                        if ((ae2.op1.value == val) && (ae2.op2.value instanceof Constant)) {
                                            int idx = (Integer)((Constant)ae2.op2.value).value;
                                            if (idx == (j-empty-1)) {
                                                continue;
                                            }
                                        }
                                    }
                                }
                                size = j -1;
                                break;
                            }
                            int dataSize = size - empty;
                            if (dataSize == arraySize) {//Not full array may cause some NullPoint problems
                                Value[] vbs = new Value[arraySize];
                                for (int j = 1; j <= size; j++) {
                                    Stmt st2 = orderList.get(p + j);
                                    if(st2 == null) {
                                        continue;
                                    }
                                    AssignStmt as2 = (AssignStmt) st2;
                                    ArrayExpr ae2 = (ArrayExpr)as2.op1.value;
                                    int idx = ((Integer)((Constant)ae2.op2.value).value);
                                    vbs[idx] = as2.op2.value;
                                    orderList.set((p + j), null);
                                    list.remove(st2);
                                }
                                Local loc = (Local)val;
                                loc._ls_read_count -= dataSize;
                                FilledArrayExpr fa = Exprs.nFilledArray(type, vbs);
                                AssignStmt nas = Stmts.nAssign(loc, fa);
                                list.replace(st, nas);
                                orderList.set(p, nas);
                                changed = true;
                                //Merge tmp Locals
                                if (loc._ls_write_count == 1 && loc._ls_read_count == 1) {
                                    Stmt st3 = null;
                                    for (int j = size + 1; j < orderList.size() - p; j++) {
                                        st3 = orderList.get(p + j);
                                        if (st3 != null) {
                                            break;
                                        }
                                    }
                                    if (st3 != null && (st3.st == ST.ASSIGN || st3.st == ST.IDENTITY)) {
                                        AssignStmt as3 = (AssignStmt) st3;
                                        if (as3.op2.value == loc) {
                                            loc._ls_read_count = 0;
                                            loc._ls_write_count = 0;
                                            
                                            list.remove(nas);
                                            orderList.set(p, null);
                                            
                                            AssignStmt nas3 = Stmts.nAssign(as3.op1.value, fa);
                                            list.replace(st3, nas3);
                                            orderList.set(orderList.indexOf(st3), nas3);
                                            
                                            je.locals.remove(loc);
                                            
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } while (changed);

        {
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
                        case CONSTANT:
                            continue;
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

        if (list._ls_inits.size() > 0) {
            // replace new again
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
                        if (as.op2.value.vt == VT.NEW) {// remove new
                            NewExpr c = (NewExpr) as.op2.value;
                            boolean replaced = false;
                            for (Iterator<AssignStmt> it = list._ls_inits.iterator(); it.hasNext();) {
                                AssignStmt stmt = it.next();
                                InvokeExpr ie = (InvokeExpr) stmt.op2.value;
                                if (ie.ops[0].value == aLeft && ie.methodOwnerType.equals(c.type)) {
                                    list.remove(st);
                                    it.remove();
                                    ValueBox[] vb = new ValueBox[ie.ops.length - 1];
                                    System.arraycopy(ie.ops, 1, vb, 0, vb.length);
                                    AssignStmt nas = Stmts.nAssign(as.op1,
                                            new ValueBox(Exprs.nInvokeNew(vb, ie.argmentTypes, ie.methodOwnerType)));
                                    list.replace(stmt, nas);
                                    aLeft._ls_read_count--;
                                    orderList.set(orderList.indexOf(stmt), nas);
                                    replaced = true;
                                    break;
                                }
                            }
                            if (replaced) {
                                continue;
                            }
                        }
                    }
                }
            }
        }
        list._ls_inits = null;
    }
}
