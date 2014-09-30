/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
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
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;

/**
 * convert
 * 
 * <pre>
 * a = b.get();
 * </pre>
 * 
 * to
 * 
 * <pre>
 * b.get();
 * </pre>
 * 
 * if a is not used in other place.
 */
public class VoidInvokeTransformer extends StatedTransformer {
    @Override
    public boolean transformReportChanged(IrMethod method) {
        if (method.locals.size() == 0) {
            return false;
        }
        int reads[] = Cfg.countLocalReads(method);
        boolean changed = false;
        for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
            if (p.st == Stmt.ST.ASSIGN && p.getOp1().vt == Value.VT.LOCAL) {
                Local left = (Local) p.getOp1();
                if (reads[left._ls_index] == 0) {
                    switch (p.getOp2().vt) {
                    case INVOKE_INTERFACE:
                    case INVOKE_NEW:
                    case INVOKE_SPECIAL:
                    case INVOKE_STATIC:
                    case INVOKE_VIRTUAL:
                        method.locals.remove(left);
                        Stmt nVoidInvoke = Stmts.nVoidInvoke(p.getOp2());
                        method.stmts.replace(p, nVoidInvoke);
                        p = nVoidInvoke;
                        changed = true;
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public void transform(IrMethod method) {
        transformReportChanged(method);

    }
}
