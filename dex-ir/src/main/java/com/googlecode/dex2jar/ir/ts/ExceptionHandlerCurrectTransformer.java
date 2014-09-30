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

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.GotoStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.Stmts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * issue 63
 * <p/>
 * 
 * <pre>
 * L1:
 *    STMTs
 * L2:
 *    RETURN
 * L1~L2 > L2 Exception
 * </pre>
 * <p/>
 * currect to
 * <p/>
 * 
 * <pre>
 * L1:
 *    STMTs
 * L2:
 *    RETURN
 * L3:
 *    MOVE-EXCEPTION
 *    GOTO L2:
 * L1~L2 > L3 Exception
 * </pre>
 * 
 * we insert MOVE-EXCEPTION at tail of code to prevent the following
 * 
 * <pre>
 *     L1:
 *        ...
 *        goto L2:
 *        ...
 *     L2:
 *        return;
 *     L1~L2 > L3 Exception
 * </pre>
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class ExceptionHandlerCurrectTransformer implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        if (irMethod.traps.size() == 0) {
            return;
        }
        Local ex = null;
        Set<LabelStmt> handlers=new HashSet<>();
        Map<LabelStmt, LabelStmt> newLocations = new HashMap<>();
        for (Trap t : irMethod.traps) {
            for (int i = 0; i < t.handlers.length; i++) {
                LabelStmt handler = t.handlers[i];
                Stmt st = handler.getNext();
                while (st.st == ST.LABEL) {
                    st = st.getNext();
                }
                LabelStmt x = (LabelStmt) st.getPre();
                if (needInsertMoveExceptionRef(st)) {
                    LabelStmt newHandler = newLocations.get(x);
                    if (newHandler == null) {
                        if (ex == null) {
                            ex = Exprs.nLocal("unRefEx");
                        }

                        newHandler = Stmts.nLabel();
                        GotoStmt g = Stmts.nGoto(x);

                        irMethod.stmts.add(newHandler);
                        irMethod.stmts.add(Stmts.nIdentity(ex, Exprs.nExceptionRef("Ljava/lang/Throwable;")));
                        irMethod.stmts.add(g);
                        newLocations.put(x, newHandler);
                    }
                    t.handlers[i] = newHandler;
                } else if (x != handler) {
                    t.handlers[i] = x;
                }
                handlers.add(t.handlers[i]);
            }
        }
        newLocations.clear();
        if (ex != null) {
            irMethod.locals.add(ex);
        }



    }

    private boolean needInsertMoveExceptionRef(Stmt st) {
        return st.st != ST.IDENTITY || st.getOp2().vt != VT.EXCEPTION_REF;
    }

}
