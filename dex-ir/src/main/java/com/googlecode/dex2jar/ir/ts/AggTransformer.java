package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;

import java.util.*;

public class AggTransformer extends StatedTransformer {
    @Override
    public boolean transformReportChanged(IrMethod method) {
        boolean changed = false;


        Set<Stmt> locationSensitiveStmts = new HashSet<>();
        // 1. merge location Insensitive stmts
        changed = simpleMergeLocals(method, changed, locationSensitiveStmts);

        if (locationSensitiveStmts.size() == 0) {
            return changed;
        }

        ReplaceX replaceX = new ReplaceX();
        Queue<Stmt> q = new UniqueQueue<>();
        q.addAll(locationSensitiveStmts);

        // 2. merge location sensitive stmts
        while (!q.isEmpty()) {
            Stmt stmt = q.poll();
            Local local = (Local) stmt.getOp1();
            Stmt next = stmt.getNext();

            switch (next.st) {
                case LABEL:
                case GOTO:
                case IDENTITY:
                case FILL_ARRAY_DATA:
                case NOP:
                case RETURN_VOID:
                    continue;
                default:
            }
            try {
                localCanExecFirst(local, next);
                throw new RuntimeException(); // impossible here
            } catch (MergeResult e) {
                if (e == SUCCESS) {
                    replaceX.local = local;
                    replaceX.replaceWith = stmt.getOp2();
                    method.locals.remove(local);
                    method.stmts.remove(stmt);

                    Cfg.travelMod(next, replaceX, false);

                    Stmt pre = next.getPre();
                    if (pre != null && locationSensitiveStmts.contains(pre)) {
                        q.add(pre);
                    }

                }
            }
        }


        return changed;
    }

    /**
     * dfs find find local and the first locationInsensitive Value
     * // TODO if can not merge, try adjust the stmt to fit the local
     */
    private static void localCanExecFirst(Local local, Stmt target) throws MergeResult {

        switch (target.et) {
            case E0: // impossible
            case En: // no EnStmt yet
                throw FAIL;
            case E1:
                localCanExecFirst(local, target.getOp());
                break;
            case E2:
                AssignStmt as = (AssignStmt) target;
                Value op1 = as.getOp1();
                Value op2 = as.getOp2();
                switch (op1.vt) {
                    case LOCAL:
                        localCanExecFirst(local, op2);
                        break;
                    case FIELD:
                        localCanExecFirst(local, op1.getOp());
                        // pass through
                    case STATIC_FIELD:
                        localCanExecFirst(local, op2);
                        break;
                    case ARRAY:
                        localCanExecFirst(local, op1.getOp1());
                        localCanExecFirst(local, op1.getOp2());
                        localCanExecFirst(local, op2);
                        break;
                    default:
                }
                break;
        }
        throw FAIL;
    }

    private static MergeResult FAIL = new MergeResult();
    private static MergeResult SUCCESS = new MergeResult();

    /**
     * dfs searching, if local is appear before first location-insensitive value, throws SUCCESS, or throws FAIL
     */
    private static void localCanExecFirst(Local local, Value op) throws MergeResult {
        switch (op.et) {
            case E0:
                if (local.vt == Value.VT.LOCAL) {
                    if (op == local) {
                        throw SUCCESS;
                    }
                }
                break;
            case E1:
                localCanExecFirst(local, op.getOp());
                break;
            case E2:
                localCanExecFirst(local, op.getOp1());
                localCanExecFirst(local, op.getOp2());
                break;
            case En:
                for (Value v : op.getOps()) {
                    localCanExecFirst(local, v);
                }
        }

        boolean shouldExclude = false;
        if (op.vt == Value.VT.INVOKE_STATIC) {
            InvokeExpr ie = (InvokeExpr) op;
            if (ie.name.equals("valueOf") && ie.owner.startsWith("Ljava/lang/") && ie.args.length == 1 && ie.args[0].length() == 1) {
                shouldExclude = true;
            }
        }

        if (!isLocationInsensitive(op.vt) && !shouldExclude) {  // this is the first insensitive Value
            throw FAIL;
        }
    }

    static class MergeResult extends Throwable {

    }

    static class ReplaceX implements Cfg.TravelCallBack {
        Local local;
        Value replaceWith;

        @Override
        public Value onAssign(Local v, AssignStmt as) {
            return v;
        }

        @Override
        public Value onUse(Local v) {
            if (v == local) {
                return replaceWith;
            }
            return v;
        }
    }

    /**
     * if a local is only used in one place, and the value is isLocationInsensitive,
     * remove the local and replace it with its value
     * <pre>
     *     a=b+c
     *     d=a+e
     * </pre>
     * to
     * <pre>
     *     d=(b+c)+e
     * </pre>
     */
    private boolean simpleMergeLocals(IrMethod method, boolean changed, Set<Stmt> locationSensitiveStmts) {
        if (method.locals.size() == 0) {
            return false;
        }
        final int[] readCounts = Cfg.countLocalReads(method);
        Set<Local> useInPhi = collectLocalUsedInPhi(method);
        final Map<Local, Value> toReplace = new HashMap<>();
        for (Iterator<Stmt> it = method.stmts.iterator(); it.hasNext(); ) {
            Stmt p = it.next();
            if (p.st == Stmt.ST.ASSIGN && p.getOp1().vt == Value.VT.LOCAL) {
                Local local = (Local) p.getOp1();
                if (useInPhi.contains(local)) {
                    continue;
                }
                if (readCounts[local._ls_index] < 2) {
                    Value op2 = p.getOp2();
                    if (isLocationInsensitive(op2)) {
                        method.locals.remove(local);
                        toReplace.put(local, op2);
                        it.remove();
                        changed = true;
                    } else {
                        locationSensitiveStmts.add(p);
                    }
                }
            }
        }
        Cfg.TravelCallBack tcb = new Cfg.TravelCallBack() {
            @Override
            public Value onAssign(Local v, AssignStmt as) {
                return v;
            }

            @Override
            public Value onUse(Local v) {
                Value v2 = toReplace.get(v);
                if (v2 != null) {
                    return v2;
                }
                return v;
            }
        };

        modReplace(toReplace, tcb);

        Cfg.travelMod(method.stmts, tcb, false);
        return changed;
    }

    private Set<Local> collectLocalUsedInPhi(IrMethod method) {
        Set<Local> useInPhi = new HashSet<>();
        if (method.phiLabels != null) {
            for (LabelStmt labelStmt : method.phiLabels) {
                if (labelStmt.phis != null) {
                    for (AssignStmt phi : labelStmt.phis) {
                        useInPhi.add((Local) phi.getOp1());
                        for (Value op : phi.getOp2().getOps()) {
                            useInPhi.add((Local) op);
                        }
                    }
                }
            }
        }
        return useInPhi;
    }

    private void modReplace(Map<Local, Value> toReplace, Cfg.TravelCallBack tcb) {
        for (Map.Entry<Local, Value> e : toReplace.entrySet()) {
            Value v = e.getValue();
            if (v.vt == Value.VT.LOCAL) {
                while (true) {
                    Value v2 = toReplace.get(v);
                    if (v2 == null) {
                        break;
                    }
                    v = v2;
                    if (v.vt != Value.VT.LOCAL) {
                        break;
                    }
                }
                e.setValue(v);
            } else {
                Cfg.travelMod(v, tcb);
            }
        }
    }

    static boolean isLocationInsensitive(Value.VT vt) {
        switch (vt) {
            case LOCAL:
            case CONSTANT:
                // +-*/
            case ADD:
            case SUB:
            case MUL:
                // case DIV:   // div is not
            case REM:
                // logical
            case AND:
            case OR:
            case XOR:

            case SHL:
            case SHR:
            case USHR:
                //cmp
            case GE:
            case GT:
            case LE:
            case LT:
            case EQ:
            case NE:
            case DCMPG:
            case DCMPL:
            case LCMP:
            case FCMPG:
            case FCMPL:
            case NOT:
                return true;
            default:
        }
        return false;
    }

    static boolean isLocationInsensitive(Value op) {
        switch (op.et) {
            case E0:
                return isLocationInsensitive(op.vt);
            case E1:
                return isLocationInsensitive(op.vt) && isLocationInsensitive(op.getOp());
            case E2:
                return isLocationInsensitive(op.vt) && isLocationInsensitive(op.getOp1()) && isLocationInsensitive(op.getOp2());
            case En:
                if (op.vt == Value.VT.INVOKE_STATIC) {
                    InvokeExpr ie = (InvokeExpr) op;
                    if (ie.name.equals("valueOf") && ie.owner.startsWith("Ljava/lang/") && ie.args.length == 1 && ie.args[0].length() == 1) {
                        for (Value v : op.getOps()) {
                            if (!isLocationInsensitive(v)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    return false;
                }
                if (isLocationInsensitive(op.vt)) {
                    for (Value v : op.getOps()) {
                        if (!isLocationInsensitive(v)) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
        }
        return false;
    }
}
