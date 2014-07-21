/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;

import java.util.*;

import static com.googlecode.dex2jar.ir.stmt.Stmt.ST.*;
import static com.googlecode.dex2jar.ir.expr.Value.VT.*;

/**
 * simply merge
 * 
 * <pre>
 *     a=NEW Labc;
 *     a.<init>();
 * </pre>
 * 
 * to
 * 
 * <pre>
 * a = new abc();
 * </pre>
 * 
 * Run after [SSATransformer, RemoveLocalFromSSA]
 */
public class NewTransformer implements Transformer {
    @Override
    public void transform(IrMethod method) {
        Map<Local, NewExpr> nAssign = new HashMap<>();
        Map<Local, AssignStmt> init = new HashMap<>();
        for (Iterator<Stmt> it = method.stmts.iterator(); it.hasNext();) {
            Stmt p = it.next();
            if (p.st == ASSIGN && p.getOp1().vt == LOCAL && p.getOp2().vt == NEW) {
                // the stmt is a new assign stmt
                Local local = (Local) p.getOp1();
                nAssign.put(local, (NewExpr) p.getOp2());
                init.put(local, (AssignStmt) p);
            }
        }

        Set<Local> replaced = new HashSet<>();
        for (Iterator<Stmt> it = method.stmts.iterator(); it.hasNext();) {
            Stmt p = it.next();

            InvokeExpr ie = null;

            if (p.st == ASSIGN) {
                if (p.getOp2().vt == INVOKE_SPECIAL) {
                    ie = (InvokeExpr) p.getOp2();
                }
            } else if (p.st == VOID_INVOKE) {
                ie = (InvokeExpr) p.getOp();
            }

            if (ie != null) {
                if ("<init>".equals(ie.name) && "V".equals(ie.ret)) {
                    Local objToInit = (Local) ie.getOps()[0];
                    NewExpr newExpr = nAssign.get(objToInit);
                    if (newExpr != null) {
                        if (!ie.owner.equals(newExpr.type)) {
                            throw new RuntimeException("");
                        }
                        Value[] orgOps = ie.getOps();
                        Value[] nOps = new Value[orgOps.length - 1];
                        System.arraycopy(orgOps, 1, nOps, 0, nOps.length);
                        InvokeExpr invokeNew = Exprs.nInvokeNew(nOps, ie.args, ie.owner);
                        method.stmts.insertBefore(p, Stmts.nAssign(objToInit, invokeNew));
                        it.remove();
                        replaced.add(objToInit);
                    } else {
                        // a invoke super in constructor
                    }
                }
            }
        }
        nAssign.clear();
        for (Local x : replaced) {
            method.stmts.remove(init.remove(x));
        }
        if (init.size() > 0) {
            int[] reads = Cfg.countLocalReads(method);
            for (Map.Entry<Local, AssignStmt> e : init.entrySet()) {
                Local local = e.getKey();
                AssignStmt as = e.getValue();
                if (reads[local._ls_index] == 0) {
                    method.stmts.remove(as);
                    method.locals.remove(local);
                }
            }
        }
    }
}
