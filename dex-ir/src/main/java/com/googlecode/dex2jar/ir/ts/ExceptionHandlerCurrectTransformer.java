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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.BaseSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.Stmts;

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
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class ExceptionHandlerCurrectTransformer implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {

        Set<LabelStmt> hanlders = new HashSet<LabelStmt>();
        for (Trap t : irMethod.traps) {
            for (int i = 0; i < t.handlers.length; i++) {
                hanlders.add(t.handlers[i]);
            }
        }
        Local ex = null;
        for (LabelStmt handler : hanlders) {
            Stmt st = handler.getNext();
            while (st.st == ST.LABEL && !hanlders.contains(st)) {
                st = st.getNext();
            }
            if (needInsertMoveExceptionRef(st)) {
                if (ex == null) {
                    ex = Exprs.nLocal("unRefEx");
                    irMethod.locals.add(ex);
                }
                Stmt stmt = Stmts.nIdentity(ex, Exprs.nExceptionRef(Type.getType(Throwable.class)));

                irMethod.stmts.insertBefore(st, stmt);
            }
        }
        Cfg.createCFG(irMethod, false);

        for (LabelStmt handler : hanlders) {
            Stmt st = handler.getNext();
            while (st.st == ST.LABEL) {
                st = st.getNext();
            }
            if (handler._cfg_froms.size() > 0) {
                LabelStmt lbl = Stmts.nLabel();
                lbl._cfg_froms = new TreeSet<Stmt>(irMethod.stmts);
                lbl._cfg_tos = new TreeSet<Stmt>(irMethod.stmts);
                if (isExHandler(st)) {
                    Stmt after = st.getNext();
                    irMethod.stmts.insertAfter(st, lbl);
                    cfgInsert(st, after, lbl);
                } else {
                    Stmt pre = st.getPre();
                    irMethod.stmts.insertBefore(st, lbl);
                    cfgInsert(pre, st, lbl);
                }
                for (Stmt stmt : new HashSet<Stmt>(handler._cfg_froms)) {

                    switch (stmt.st) {
                    case GOTO: {
                        JumpStmt jumpStmt = (JumpStmt) stmt;
                        jumpStmt.target = lbl;
                        cfgReplace(stmt, handler, lbl);
                    }
                        break;
                    case LOOKUP_SWITCH:
                    case TABLE_SWITCH:
                        BaseSwitchStmt bss = (BaseSwitchStmt) stmt;
                        if (bss.defaultTarget.equals(handler)) {
                            bss.defaultTarget = lbl;
                        }
                        LabelStmt[] targets = bss.targets;
                        for (int i = 0; i < targets.length; i++) {
                            if (targets[i].equals(handler)) {
                                targets[i] = lbl;
                            }
                        }
                        cfgReplace(stmt, handler, lbl);
                        break;
                    case IF: {
                        JumpStmt jumpStmt = (JumpStmt) stmt;
                        if (jumpStmt.target.equals(handler)) {
                            jumpStmt.target = lbl;
                            cfgReplace(stmt, handler, lbl);
                        }
                    }
                    // go thought
                    default:
                        Stmt next = stmt.getNext();
                        if (handler.equals(next)) {
                            Stmt g = Stmts.nGoto(lbl);
                            g._cfg_froms = new TreeSet<Stmt>(irMethod.stmts);
                            g._cfg_tos = new TreeSet<Stmt>(irMethod.stmts);
                            g._cfg_tos.add(lbl);
                            irMethod.stmts.insertAfter(stmt, g);
                            cfgReplace(stmt, handler, g);
                        }
                    }
                }
            }
        }
    }

    void cfgInsert(Stmt pre, Stmt next, Stmt p) {
        if (pre._cfg_tos.contains(next)) {
            pre._cfg_tos.remove(next);
            pre._cfg_tos.add(p);
            next._cfg_froms.remove(pre);
            next._cfg_froms.add(p);
            p._cfg_froms.add(pre);
            p._cfg_tos.add(next);
        }
    }

    void cfgReplace(Stmt pre, Stmt next, Stmt p) {
        pre._cfg_tos.remove(next);
        pre._cfg_tos.add(p);
        next._cfg_froms.remove(pre);
        p._cfg_froms.add(pre);
    }

    private boolean isExHandler(Stmt st) {
        return (st.st == ST.ASSIGN || st.st == ST.IDENTITY) && ((AssignStmt) st).op2.value.vt == VT.EXCEPTION_REF;
    }

    private boolean needInsertMoveExceptionRef(Stmt st) {
        return !isExHandler(st);
    }

}
