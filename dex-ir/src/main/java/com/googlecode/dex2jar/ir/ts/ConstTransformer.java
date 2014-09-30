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
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.ts.Cfg.TravelCallBack;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Replace must-be-constant local to constant
 * <p/>
 * Require a SSA form, usually run after {@link SSATransformer}
 *
 * @author Panxiaobo
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ConstTransformer implements Transformer {
    @Override
    public void transform(IrMethod m) {

        // 1. init
        init(m);

        // 2. collect
        collect(m);

        // 3. mark constant
        markConstant(m);
        markReplacable(m);
        // 4. replace
        replace(m);

        // 5. clean
        clean(m);
    }

    private void clean(IrMethod m) {
        for (Local local : m.locals) {
            local.tag = null;
        }
    }

    private void replace(IrMethod m) {
        Cfg.travelMod(m.stmts, new TravelCallBack() {

            @Override
            public Value onUse(Local v) {
                ConstAnalyzeValue cav = (ConstAnalyzeValue) v.tag;
                if (cav.replacable) {
                    return Exprs.nConstant(cav.cst);
                }
                return v;
            }

            @Override
            public Value onAssign(Local v, AssignStmt as) {
                ConstAnalyzeValue cav = (ConstAnalyzeValue) v.tag;
                if (cav.replacable) {
                    if (as.op2.trim().vt != VT.CONSTANT) {
                        as.op2 = Exprs.nConstant(cav.cst);
                    }
                }
                return v;
            }

        }, true);
    }

    private void markReplacable(IrMethod m) {
        for (Local local : m.locals) {
            ConstAnalyzeValue cav = (ConstAnalyzeValue) local.tag;
            if (Boolean.TRUE.equals(cav.isConst)) {
                boolean allTosAreCst = true;
                for (ConstAnalyzeValue c : cav.assignTo) {
                    if (!Boolean.TRUE.equals(c.isConst)) {
                        allTosAreCst = false;
                        break;
                    }
                }
                if (allTosAreCst) {
                    cav.replacable = true;
                }
            }
        }
    }

    private void markConstant(IrMethod m) {
        Queue<Local> queue = new UniqueQueue<>();
        queue.addAll(m.locals);
        while (!queue.isEmpty()) {
            ConstAnalyzeValue cav = (ConstAnalyzeValue) queue.poll().tag;

            Object cst = cav.cst;

            if (cav.isConst == null) {
                if (cst != null) {// we have a cst
                    boolean allCstEquals = true;
                    for (ConstAnalyzeValue p0 : cav.assignFrom) {
                        if (!cst.equals(p0.cst)) {
                            allCstEquals = false;
                            break;
                        }
                    }
                    if (allCstEquals) {
                        cav.isConst = true;

                    }
                }
            }

            if (cst != null || Boolean.TRUE.equals(cav.isConst)) {
                for (ConstAnalyzeValue p0 : cav.assignTo) {
                    if (p0.isConst == null) {
                        if (p0.cst == null) {
                            p0.cst = cst;
                        }
                        queue.add(p0.local);
                    }
                }
            }

            if (Boolean.FALSE.equals(cav.isConst)) {
                cav.cst = null;
                for (ConstAnalyzeValue c : cav.assignTo) {
                    if (!Boolean.FALSE.equals(c.isConst)) {
                        c.cst = null;
                        c.isConst = false;
                        queue.add(c.local);
                    }
                }
            }
        }
    }

    private void collect(IrMethod m) {
        for (Stmt p = m.stmts.getFirst(); p != null; p = p.getNext()) {
            if (p.st == ST.ASSIGN || p.st == ST.IDENTITY) {
                E2Stmt e2 = (E2Stmt) p;
                Value op1 = e2.op1.trim();
                Value op2 = e2.op2.trim();
                if (op1.vt == VT.LOCAL) {
                    ConstAnalyzeValue cav = (ConstAnalyzeValue) ((Local) op1).tag;
                    if (op2.vt == VT.CONSTANT) {
                        Constant c = (Constant) op2;
                        cav.isConst = true;
                        cav.cst = c.value;
                    } else if (op2.vt == VT.LOCAL) {
                        Local local2 = (Local) op2;
                        ConstAnalyzeValue zaf2 = (ConstAnalyzeValue) local2.tag;
                        cav.assignFrom.add(zaf2);
                        zaf2.assignTo.add(cav);
                    } else if (op2.vt == VT.PHI) {
                        PhiExpr pe = (PhiExpr) op2;
                        for (Value v : pe.ops) {
                            ConstAnalyzeValue zaf2 = (ConstAnalyzeValue) ((Local) v.trim()).tag;
                            cav.assignFrom.add(zaf2);
                            zaf2.assignTo.add(cav);
                        }
                    } else {
                        cav.isConst = Boolean.FALSE;
                    }
                }
            }
        }
    }

    private void init(IrMethod m) {
        for (Local local : m.locals) {
            local.tag = new ConstAnalyzeValue(local);
        }
    }

    static class ConstAnalyzeValue {
        private static final Integer ZERO = Integer.valueOf(0);
        public final Local local;
        public Boolean isConst = null;
        public boolean replacable = false;
        public Object cst;
        public Set<ConstAnalyzeValue> assignFrom = new HashSet(3);
        public Set<ConstAnalyzeValue> assignTo = new HashSet(3);

        public ConstAnalyzeValue(Local local) {
            super();
            this.local = local;
        }

        public boolean isZero() {
            if (isConst == null) {
                return false;
            }
            return isConst && (ZERO.equals(cst));
        }
    }
}
