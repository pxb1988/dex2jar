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
package com.googlecode.dex2jar.v3;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Transformer;

/**
 * issue 63
 * 
 * <pre>
 * L1: 
 *    STMTs
 * L2:
 *    RETURN
 * L1~L2 > L2 Exception
 * </pre>
 * 
 * currect to
 * 
 * <pre>
 * L1: 
 *    STMTs
 *    GOTO L3
 * L2:
 *    MOVE-EXCEPTION
 * L3:
 *    RETURN
 * L1~L2 > L2 Exception
 * </pre>
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class ExceptionHandlerCurrect implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        Local ex = null;
        for (Trap t : irMethod.traps) {
            LabelStmt handler = t.handler;
            Stmt st = handler.getNext();
            Stmt pre = handler.getPre();
            while (st.st == ST.LABEL) {
                st = st.getNext();
            }
            while (pre.st == ST.LABEL) {
                pre = pre.getPre();
            }
            if (needInsertMoveExceptionRef(st) && needInsertGoto(pre)) {

                LabelStmt lbl = Stmts.nLabel();
                JumpStmt g = Stmts.nGoto(lbl);
                irMethod.stmts.insertAftre(pre, g);
                irMethod.stmts.insertBefore(st, lbl);
                if (ex == null) {
                    ex = Exprs.nLocal("unRefEx", null);
                }
                irMethod.stmts.insertBefore(lbl, Stmts.nAssign(ex, Exprs.nExceptionRef(t.type)));
            }
        }
        if (ex != null) {
            irMethod.locals.add(ex);
        }
    }

    private boolean needInsertGoto(Stmt pre) {
        switch (pre.st) {
        case RETURN:
        case GOTO:
        case RETURN_VOID:
        case THROW:
            return false;
        }
        return true;
    }

    private boolean needInsertMoveExceptionRef(Stmt st) {
        return st.st != ST.ASSIGN || ((AssignStmt) st).op2.value.vt != VT.EXCEPTION_REF;
    }

}
