package com.googlecode.dex2jar.ir.ts.array;


import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Cfg;
import com.googlecode.dex2jar.ir.ts.StatedTransformer;

import java.lang.reflect.Array;
import java.util.*;

/**
 * require SSA, element index are const
 * <p/>
 * transformer
 * <pre>
 *     ...
 *     a[4]="abc"
 *     return a[4]
 * </pre>
 * to
 * <pre>
 *     ...
 *     a[4]="abc"
 *     return "abc"
 * </pre>
 */
public class ArrayElementTransformer extends StatedTransformer {
    @Override
    public boolean transformReportChanged(IrMethod method) {

        Set<Local> arrays = searchForArrayObject(method);
        if (arrays.size() == 0) {
            return false;
        }
        for (Local local : method.locals) {
            local._ls_index = -1;
        }
        int i = 0;
        for (Local local : arrays) {
            local._ls_index = i++;
        }
        final int size = i;
        Cfg.createCFG(method);
        final List<ArrayValue> values = new ArrayList<>();
        final List<Stmt> used = new ArrayList<>();
        Cfg.dfs(method.stmts, new Cfg.FrameVisitor<ArrayValue[]>() {

            Set<Integer> phis = new HashSet<>();

            @Override
            public ArrayValue[] merge(ArrayValue[] srcFrame, ArrayValue[] distFrame, Stmt src, Stmt dist) {
                if (dist.st == Stmt.ST.LABEL) {
                    LabelStmt labelStmt = (LabelStmt) dist;
                    if (labelStmt.phis != null) {
                        for (AssignStmt phi : labelStmt.phis) {
                            int idx = ((Local) phi.getOp1())._ls_index;
                            if (idx >= 0) {
                                phis.add(idx);
                            }
                        }
                    }
                }
                if (distFrame == null) {
                    distFrame = new ArrayValue[size];
                    for (int i = 0; i < size; i++) {
                        if (phis.contains(i)) {
                            ArrayValue aov = new ArrayValue();
                            values.add(aov);
                            aov.s = ArrayValue.S.UNKNOWN;
                            aov.indexType = ArrayValue.IndexType.NONE;
                            aov.stmt = dist;
                            distFrame[i] = aov;
                        } else {
                            ArrayValue arc = srcFrame[i];
                            if (arc != null) {
                                ArrayValue aov = new ArrayValue();
                                values.add(aov);
                                aov.s = ArrayValue.S.INHERIT;
                                aov.indexType = ArrayValue.IndexType.NONE;
                                aov.stmt = dist;
                                aov.parent = arc;
                                distFrame[i] = aov;
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        if (phis.contains(i)) {
                            continue;
                        }
                        ArrayValue arc = srcFrame[i];
                        ArrayValue aov = distFrame[i];
                        if (arc != null && aov != null) {
                            if (aov.parent == null) {
                                aov.parent = arc;
                            } else if (!aov.parent.equals(arc)) {
                                if (aov.otherParents == null) {
                                    aov.otherParents = new HashSet<>();
                                }
                                aov.otherParents.add(arc);
                            }
                        }
                    }
                }
                phis.clear();
                return distFrame;
            }

            @Override
            public ArrayValue[] initFirstFrame(Stmt first) {
                return new ArrayValue[size];
            }

            ArrayValue[] tmp = new ArrayValue[size];
            Stmt currentStmt;


            @Override
            public ArrayValue[] exec(ArrayValue[] frame, Stmt stmt) {
                currentStmt = stmt;
                System.arraycopy(frame, 0, tmp, 0, size);
                if (stmt.st == Stmt.ST.ASSIGN) {
                    // create an array
                    if (stmt.getOp1().vt == Value.VT.LOCAL) {
                        Local local = (Local) stmt.getOp1();
                        use(stmt.getOp2());
                        if (local._ls_index >= 0) {
                            Value op2 = stmt.getOp2();
                            if (op2.vt == Value.VT.NEW_ARRAY) {
                                ArrayValue av = new ArrayValue();
                                av.s = ArrayValue.S.DEFAULT;
                                av.size = op2.getOp();
                                values.add(av);
                                tmp[local._ls_index] = av;
                            } else if (op2.vt == Value.VT.FILLED_ARRAY) {
                                ArrayValue av = new ArrayValue();
                                av.s = ArrayValue.S.DEFAULT;
                                av.indexType = ArrayValue.IndexType.CONST;
                                av.stmt = stmt;
                                FilledArrayExpr fae = (FilledArrayExpr) stmt.getOp2();
                                av.size = Exprs.nInt(fae.getOps().length);
                                Value[] ops = fae.getOps();
                                for (int i = 0; i < ops.length; i++) {
                                    av.elements1.put(i, ops[i]);
                                }
                                values.add(av);
                                tmp[local._ls_index] = av;
                            } else if (op2.vt == Value.VT.CONSTANT) {
                                Object cst = ((Constant) op2).value;
                                if (cst != null && !cst.equals(Constant.Null) && cst.getClass().isArray()) {
                                    ArrayValue av = new ArrayValue();
                                    av.s = ArrayValue.S.DEFAULT;
                                    av.indexType = ArrayValue.IndexType.CONST;
                                    av.stmt = stmt;
                                    int size = Array.getLength(cst);
                                    av.size = Exprs.nInt(size);
                                    for (int i = 0; i < size; i++) {
                                        av.elements1.put(i, Exprs.nConstant(Array.get(cst, size)));
                                    }
                                    values.add(av);
                                    tmp[local._ls_index] = av;
                                } else {
                                    ArrayValue av = new ArrayValue();
                                    values.add(av);
                                    av.s = ArrayValue.S.UNKNOWN;
                                    av.indexType = ArrayValue.IndexType.NONE;
                                    av.stmt = stmt;
                                    tmp[local._ls_index] = av;
                                }
                            } else {
                                ArrayValue av = new ArrayValue();
                                values.add(av);
                                av.s = ArrayValue.S.UNKNOWN;
                                av.indexType = ArrayValue.IndexType.NONE;
                                av.stmt = stmt;
                                tmp[local._ls_index] = av;
                            }
                        }
                        // assign index1
                    } else if (stmt.getOp1().vt == Value.VT.ARRAY) {
                        use(stmt.getOp2());
                        ArrayExpr ae = (ArrayExpr) stmt.getOp1();
                        if (ae.getOp1().vt == Value.VT.LOCAL) {
                            Local local = (Local) ae.getOp1();
                            Value index = ae.getOp2();
                            if (local._ls_index >= 0) {
                                if (index.vt == Value.VT.CONSTANT) {
                                    ArrayValue parent = tmp[local._ls_index];
                                    ArrayValue av = new ArrayValue();
                                    values.add(av);
                                    av.parent = parent;
                                    av.elements1.put(((Number) (((Constant) index).value)).intValue(), stmt.getOp2());
                                    av.indexType = ArrayValue.IndexType.CONST;
                                    av.s = ArrayValue.S.INHERIT;
                                    av.stmt = stmt;
                                    tmp[local._ls_index] = av;
                                } else if (index.vt == Value.VT.LOCAL) {
                                    ArrayValue parent = tmp[local._ls_index];
                                    ArrayValue av = new ArrayValue();
                                    values.add(av);
                                    av.parent = parent;
                                    av.elements1.put(index, stmt.getOp2());
                                    av.indexType = ArrayValue.IndexType.LOCAL;
                                    av.s = ArrayValue.S.INHERIT;
                                    av.stmt = stmt;
                                    tmp[local._ls_index] = av;
                                } else {
                                    ArrayValue av = new ArrayValue();
                                    values.add(av);
                                    av.s = ArrayValue.S.UNKNOWN;
                                    av.indexType = ArrayValue.IndexType.NONE;
                                    av.stmt = stmt;
                                    tmp[local._ls_index] = av;
                                }
                            } else {
                                use(stmt.getOp1());
                            }
                        } else {
                            use(stmt.getOp1());
                        }

                    } else {
                        use(stmt.getOp1());
                        use(stmt.getOp2());
                    }
                    // assign index2
                } else if (stmt.st == Stmt.ST.FILL_ARRAY_DATA) {
                    if (stmt.getOp1().vt == Value.VT.LOCAL) {
                        Local local = (Local) stmt.getOp1();
                        if (local._ls_index >= 0) {
                            Object array = ((Constant) stmt.getOp2()).value;
                            ArrayValue parent = tmp[local._ls_index];
                            ArrayValue av = new ArrayValue();
                            values.add(av);
                            av.parent = parent;
                            int size = Array.getLength(array);
                            av.size = Exprs.nInt(size);
                            for (int i = 0; i < size; i++) {
                                av.elements1.put(i, Exprs.nConstant(Array.get(array, i)));
                            }
                            av.indexType = ArrayValue.IndexType.CONST;
                            av.s = ArrayValue.S.INHERIT;
                            av.stmt = stmt;
                            tmp[local._ls_index] = av;
                        }
                    } else {
                        use(stmt.getOp1());
                    }
                } else {
                    switch (stmt.et) {
                        case E0:
                            break;
                        case E1:
                            use(stmt.getOp());
                            break;
                        case E2:
                            use(stmt.getOp1());
                            use(stmt.getOp2());
                            break;
                        case En:
                            throw new RuntimeException();
                    }
                }

                return tmp;
            }

            private void use(Value v) {
                switch (v.et) {
                    case E0:
                        break;
                    case E1:
                        use(v.getOp());
                        break;
                    case E2:
                        Value op1 = v.getOp1();
                        Value op2 = v.getOp2();
                        use(op1);
                        use(op2);
                        if (v.vt == Value.VT.ARRAY) {
                            if (op1.vt == Value.VT.LOCAL && (op2.vt == Value.VT.LOCAL || op2.vt == Value.VT.CONSTANT)) {
                                Local local = (Local) op1;
                                if (local._ls_index > 0) {
                                    used.add(currentStmt);
                                }
                            }
                        }
                        break;
                    case En:
                        for (Value op : v.getOps()) {
                            use(op);
                        }
                        break;
                }
            }
        });


        // TODO travel stmt to find must-be array element

        for (Stmt p : method.stmts) {

        }
        new StmtTraveler() {
            @Override
            public Value travel(Value op) {
                op = super.travel(op);
                if (op.vt == Value.VT.ARRAY) {

                }
                return op;
            }
        }.travel(method.stmts);

        return false;
    }

    public static void main(String... args) {
        IrMethod m = new IrMethod();
        m.isStatic = true;
        m.name = "a";
        m.args = new String[0];
        m.ret = "[Ljava/lang/String;";
        m.owner = "La;";

        Local array = Exprs.nLocal(1);
        m.locals.add(array);

        m.stmts.add(Stmts.nAssign(array, Exprs.nNewArray("Ljava/lang/String;", Exprs.nInt(2))));
        m.stmts.add(Stmts.nAssign(Exprs.nArray(array, Exprs.nInt(1), "Ljava/lang/String;"), Exprs.nString("123")));
        m.stmts.add(Stmts.nAssign(Exprs.nArray(array, Exprs.nInt(0), "Ljava/lang/String;"), Exprs.nString("456")));
        m.stmts.add(Stmts.nReturn(array));
        new ArrayElementTransformer().transform(m);
    }

    private Set<Local> searchForArrayObject(IrMethod method) {
        final Set<Local> arrays = new HashSet<>();
        for (Stmt stmt : method.stmts) {
            if (stmt.st == Stmt.ST.ASSIGN) {
                // create an array
                if (stmt.getOp1().vt == Value.VT.LOCAL) {
                    Local local = (Local) stmt.getOp1();
                    if (stmt.getOp2().vt == Value.VT.NEW_ARRAY || stmt.getOp2().vt == Value.VT.FILLED_ARRAY) {
                        arrays.add(local);
                    }
                    // assign index1
                } else if (stmt.getOp1().vt == Value.VT.ARRAY) {
                    ArrayExpr ae = (ArrayExpr) stmt.getOp1();
                    if (ae.getOp1().vt == Value.VT.LOCAL) {
                        Local local = (Local) ae.getOp1();
                        arrays.add(local);
                    }

                }
                // assign index2
            } else if (stmt.st == Stmt.ST.FILL_ARRAY_DATA) {
                if (stmt.getOp1().vt == Value.VT.LOCAL) {
                    Local local = (Local) stmt.getOp1();
                    arrays.add(local);
                }
            }
        }
        return arrays;
    }

    static class ArrayValue {
        enum S {
            /**
             * all element are default value. that is null for object and 0 for primitive
             */
            DEFAULT,
            /**
             * all element are unknown value
             */
            UNKNOWN,
            /**
             * the element value is based on its parent
             */
            INHERIT
        }

        enum IndexType {
            CONST, LOCAL, NONE
        }


        IndexType indexType = IndexType.NONE;
        S s = S.INHERIT;
        ArrayValue parent;
        Value size;
        Set<ArrayValue> otherParents;
        Map<Object, Value> elements1 = new HashMap<>();
        Stmt stmt;
    }
}
