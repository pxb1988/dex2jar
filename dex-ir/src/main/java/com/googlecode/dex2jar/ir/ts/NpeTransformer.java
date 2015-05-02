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
import com.googlecode.dex2jar.ir.StmtSearcher;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;

/**
 * Replace MUST be NullPointerException stmt to 'throw new NullPointerException()'
 *
 * Replace MUST be 'divide by zero' stmt to 'throw new ArithmeticException("divide by zero")'
 */
public class NpeTransformer extends StatedTransformer {
    private static class MustThrowException extends RuntimeException {
    }

    private static final MustThrowException NPE = new MustThrowException();
    private static final MustThrowException DIVE = new MustThrowException();

    @Override
    public boolean transformReportChanged(IrMethod method) {
        boolean changed = false;
        if (method.locals.size() == 0) {
            return false;
        }
        StmtSearcher st = new StmtSearcher() {
            @Override
            public void travel(Stmt stmt) {
                if (stmt.st == Stmt.ST.FILL_ARRAY_DATA) {
                    if (isNull(stmt.getOp1())) {
                        throw NPE;
                    }
                }
                super.travel(stmt);
            }

            @Override
            public void travel(Value op) {
                switch (op.vt) {
                case INVOKE_VIRTUAL:
                case INVOKE_SPECIAL:
                case INVOKE_INTERFACE: {
                    if (isNull(op.getOps()[0])) {
                        throw NPE;
                    }
                }
                    break;
                case ARRAY: {
                    if (isNull(op.getOp1())) {
                        throw NPE;
                    }
                }
                    break;
                case FIELD: {
                    if (isNull(op.getOp())) {
                        throw NPE;
                    }
                }
                    break;
                    case IDIV:
                        if (op.getOp2().vt == Value.VT.CONSTANT) {
                            Constant constant = (Constant) op.getOp2();
                            if (((Number) constant.value).intValue() == 0) {
                                throw DIVE;
                            }
                        }
                        break;
                    case LDIV:
                        if (op.getOp2().vt == Value.VT.CONSTANT) {
                            Constant constant = (Constant) op.getOp2();
                            if (((Number) constant.value).longValue() == 0) {
                                throw DIVE;
                            }
                        }
                        break;
                default:
                }
            }

        };
        for (Stmt p = method.stmts.getFirst(); p != null;) {
            try {
                st.travel(p);
                p = p.getNext();
            } catch (MustThrowException e) {
                replace(method, p);
                Stmt q = p.getNext();
                method.stmts.remove(p);
                changed = true;
                p = q;
            }
        }
        return changed;
    }

    private void replace(final IrMethod m, final Stmt p) {
        StmtTraveler traveler = new StmtTraveler() {
            @Override
            public Value travel(Value op) {
                switch (op.vt) {
                case INVOKE_VIRTUAL:
                case INVOKE_SPECIAL:
                case INVOKE_INTERFACE: {
                    Value ops[] = op.getOps();
                    if (isNull(ops[0])) {
                        for (int i = 1; i < ops.length; i++) {
                            travel(ops[i]);
                        }
                        throw NPE;
                    }
                }
                    break;
                case ARRAY: {
                    if (isNull(op.getOp1())) {
                        travel(op.getOp2());
                        throw NPE;
                    }
                }
                    break;
                case FIELD: {
                    if (isNull(op.getOp())) {
                        throw NPE;
                    }
                }
                    break;
                    case IDIV:
                        if (op.getOp2().vt == Value.VT.CONSTANT) {
                            Constant constant = (Constant) op.getOp2();
                            if (((Number) constant.value).intValue() == 0) {
                                travel(op.getOp1());
                                throw DIVE;
                            }
                        }
                        break;
                    case LDIV:
                        if (op.getOp2().vt == Value.VT.CONSTANT) {
                            Constant constant = (Constant) op.getOp2();
                            if (((Number) constant.value).longValue() == 0) {
                                travel(op.getOp1());
                                throw DIVE;
                            }
                        }
                        break;
                default:
                }
                Value sop = super.travel(op);
                if (sop.vt == Value.VT.LOCAL || sop.vt == Value.VT.CONSTANT) {
                    return sop;
                } else {
                    Local local = new Local();
                    m.locals.add(local);
                    m.stmts.insertBefore(p, Stmts.nAssign(local, sop));
                    return local;
                }
            }
        };
        try {
            switch (p.et) {
            case E0:
                // impossible
                break;
            case E1:
                traveler.travel(p.getOp());
                break;
            case E2:
                if (p.st == Stmt.ST.ASSIGN) {
                    switch (p.getOp1().vt) {
                    case ARRAY:
                        traveler.travel(p.getOp1().getOp1());
                        traveler.travel(p.getOp1().getOp2());
                        traveler.travel(p.getOp2());
                        break;
                    case FIELD:
                        traveler.travel(p.getOp1().getOp());
                        traveler.travel(p.getOp2());
                        break;
                    case STATIC_FIELD:
                    case LOCAL:
                        traveler.travel(p.getOp2());
                        break;
                    default:
                        // impossible
                    }
                } else if (p.st == Stmt.ST.FILL_ARRAY_DATA) {
                    if (isNull(p.getOp1())) {
                        throw NPE;
                    } else {
                        traveler.travel(p.getOp1());
                    }
                }
                break;
            case En:
            }
        } catch (MustThrowException e) {
            if (e == NPE) {
                m.stmts.insertBefore(p,
                        Stmts.nThrow(Exprs.nInvokeNew(new Value[0], new String[0], "Ljava/lang/NullPointerException;")));
            } else {
                m.stmts.insertBefore(p,
                        Stmts.nThrow(Exprs.nInvokeNew(new Value[]{Exprs.nString("divide by zero")}, new String[]{"Ljava/lang/String;"}, "Ljava/lang/ArithmeticException;")));
            }
        }
    }

    static boolean isNull(Value v) {
        if (v.vt == Value.VT.CONSTANT) {
            Constant cst = (Constant) v;
            if (Constant.Null.equals(cst.value)) {
                return true;
            } else if (cst.value instanceof Number) {
                return ((Number) cst.value).intValue() == 0;
            }
        }
        return false;
    }
}
