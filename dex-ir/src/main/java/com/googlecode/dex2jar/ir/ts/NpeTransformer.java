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
 * <p>
 * Replace MUST be 'divide by zero' stmt to 'throw new ArithmeticException("divide by zero")'
 */
public class NpeTransformer extends StatedTransformer {

    private static class MustThrowException extends RuntimeException {

        private static final long serialVersionUID = 7501197864919305696L;

    }

    private static final MustThrowException NPE = new MustThrowException();

    private static final MustThrowException DIVE = new MustThrowException();

    private static final MustThrowException NEGATIVE_ARRAY_SIZE = new MustThrowException();

    @Override
    public boolean transformReportChanged(IrMethod method) {
        boolean changed = false;
        if (method.locals.isEmpty()) {
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
                case NEW_ARRAY:
                    if (op.getOp().vt == Value.VT.CONSTANT) {
                        Constant constant = (Constant) op.getOp();
                        if (((Number) constant.value).intValue() < 0) {
                            throw NEGATIVE_ARRAY_SIZE;
                        }
                    }
                    break;
                case NEW_MUTI_ARRAY:
                    for (Value size : op.getOps()) {
                        if (size.vt == Value.VT.CONSTANT) {
                            Constant constant = (Constant) size;
                            if (((Number) constant.value).intValue() < 0) {
                                throw NEGATIVE_ARRAY_SIZE;
                            }
                        }
                    }
                    break;
                default:
                }
            }

        };
        Stmt p = method.stmts.getFirst();
        while (p != null) {
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
                    Value[] ops = op.getOps();
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
                case NEW_ARRAY:
                    if (op.getOp().vt == Value.VT.CONSTANT) {
                        Constant constant = (Constant) op.getOp();
                        if (((Number) constant.value).intValue() < 0) {
                            throw NEGATIVE_ARRAY_SIZE;
                        }
                    }
                    break;
                case NEW_MUTI_ARRAY:
                    for (Value size : op.getOps()) {
                        if (size.vt == Value.VT.CONSTANT) {
                            Constant constant = (Constant) size;
                            if (((Number) constant.value).intValue() < 0) {
                                throw NEGATIVE_ARRAY_SIZE;
                            } else {
                                travel(size);
                            }
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
                break;
            default:
                break;
            }
        } catch (MustThrowException e) {
            if (e == NPE) {
                m.stmts.insertBefore(p,
                        Stmts.nThrow(Exprs.nInvokeNew(new Value[0], new String[0],
                                "Ljava/lang/NullPointerException;")));
            } else if (e == DIVE) {
                m.stmts.insertBefore(p,
                        Stmts.nThrow(Exprs.nInvokeNew(new Value[]{Exprs.nString("divide by zero")}, new String[]{
                                "Ljava/lang/String;"}, "Ljava/lang/ArithmeticException;")));
            } else if (e == NEGATIVE_ARRAY_SIZE) {
                m.stmts.insertBefore(p,
                        Stmts.nThrow(Exprs.nInvokeNew(new Value[0], new String[0], "Ljava/lang"
                                + "/NegativeArraySizeException;")));
            }
        }
    }

    static boolean isNull(Value v) {
        if (v.vt == Value.VT.CONSTANT) {
            Constant cst = (Constant) v;
            if (Constant.NULL.equals(cst.value)) {
                return true;
            } else if (cst.value instanceof Number) {
                return ((Number) cst.value).intValue() == 0;
            }
        }
        return false;
    }

}
