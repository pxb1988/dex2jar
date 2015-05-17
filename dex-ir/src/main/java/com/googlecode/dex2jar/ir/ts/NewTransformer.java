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

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;

import java.util.*;

import static com.googlecode.dex2jar.ir.expr.Value.VT.*;
import static com.googlecode.dex2jar.ir.stmt.Stmt.ST.*;

/**
 * simply merge
 * <p/>
 * <pre>
 *     a=NEW Labc;
 *     a.<init>();
 * </pre>
 * <p/>
 * to
 * <p/>
 * <pre>
 * a = new abc();
 * </pre>
 * <p/>
 * Run after [SSATransformer, RemoveLocalFromSSA]
 */
public class NewTransformer implements Transformer {

    static Vx IGNORED = new Vx(null, true);

    @Override
    public void transform(IrMethod method) {

        // 1. replace
        // =========
        // a=NEW Abc;
        // b=a
        // b.<init>()
        // to ======
        // a=new Abc();
        // b=a;
        // =========
        replaceX(method);

        // 2. replace NEW Abc;.<init>() -> new Abc();
        replaceAST(method);

    }

    void replaceX(IrMethod method) {
        final Map<Local, TObject> init = new HashMap<>();
        for (Stmt p : method.stmts) {
            if (p.st == ASSIGN && p.getOp1().vt == LOCAL && p.getOp2().vt == NEW) {
                // the stmt is a new assign stmt
                Local local = (Local) p.getOp1();
                init.put(local, new TObject(local, (AssignStmt) p));
            }
        }

        if (init.size() > 0) {
            final int size = Cfg.reIndexLocal(method);
            makeSureUsedBeforeConstructor(method, init, size);
            if (init.size() > 0) {
                replace0(method, init, size);
            }
            for (Stmt stmt : method.stmts) {
                stmt.frame = null;
            }
        }
    }

    void replaceAST(IrMethod method) {
        for (Iterator<Stmt> it = method.stmts.iterator(); it.hasNext(); ) {
            Stmt p = it.next();

            InvokeExpr ie = findInvokeExpr(p, null);

            if (ie != null) {
                if ("<init>".equals(ie.name) && "V".equals(ie.ret)) {
                    Value[] orgOps = ie.getOps();
                    if (orgOps[0].vt == NEW) {
                        NewExpr newExpr = (NewExpr) ie.getOps()[0];
                        if (newExpr != null) {
                            Value[] nOps = new Value[orgOps.length - 1];
                            System.arraycopy(orgOps, 1, nOps, 0, nOps.length);
                            InvokeExpr invokeNew = Exprs.nInvokeNew(nOps, ie.args, ie.owner);
                            method.stmts.insertBefore(p, Stmts.nVoidInvoke(invokeNew));
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    void replace0(IrMethod method, Map<Local, TObject> init, int size) {
        Set<Local> toDelete = new HashSet<>();

        Local locals[] = new Local[size];
        for (Local local : method.locals) {
            locals[local._ls_index] = local;
        }

        // find all locals to delete
        for (TObject obj : init.values()) {
            Vx[] frame = (Vx[]) obj.invokeStmt.frame;
            for (int i = 0; i < frame.length; i++) {
                Vx s = frame[i];
                if (s != null && s.obj == obj) {
                    toDelete.add(locals[i]);
                }
            }
        }
        // delete the locals
        for (Iterator<Stmt> it = method.stmts.iterator(); it.hasNext(); ) {
            Stmt p = it.next();
            if (p.st == ASSIGN && p.getOp1().vt == LOCAL) {
                if (toDelete.contains((Local) p.getOp1())) {
                    it.remove();
                }
            }
        }
        // add the locals back
        for (TObject obj : init.values()) {
            Vx[] frame = (Vx[]) obj.invokeStmt.frame;
            for (int i = 0; i < frame.length; i++) {
                Vx s = frame[i];
                if (s != null && s.obj == obj) {
                    Local b = locals[i];
                    if (b != obj.local) {
                        method.stmts.insertAfter(obj.invokeStmt, Stmts.nAssign(b, obj.local));
                    }
                }
            }
            InvokeExpr ie = findInvokeExpr(obj.invokeStmt, null);
            Value[] orgOps = ie.getOps();
            Value[] nOps = new Value[orgOps.length - 1];
            System.arraycopy(orgOps, 1, nOps, 0, nOps.length);
            InvokeExpr invokeNew = Exprs.nInvokeNew(nOps, ie.args, ie.owner);
            method.stmts.replace(obj.invokeStmt, Stmts.nAssign(obj.local, invokeNew));
        }
    }

    void makeSureUsedBeforeConstructor(IrMethod method, final Map<Local, TObject> init, final int size) {
        Cfg.createCFG(method);
        Cfg.dfs(method.stmts, new Cfg.FrameVisitor<Vx[]>() {

            boolean keepFrame = false;
            Vx[] tmp = new Vx[size];
            StmtTraveler stmtTraveler = new StmtTraveler() {
                Stmt current;

                @Override
                public Stmt travel(Stmt stmt) {

                    this.current = stmt;
                    if (stmt.et == ET.E2) {
                        if (stmt.getOp1().vt == LOCAL) {
                            Local op1 = (Local) stmt.getOp1();
                            if (stmt.getOp2().vt == LOCAL) {
                                Local op2 = (Local) stmt.getOp2();
                                tmp[op1._ls_index] = tmp[op2._ls_index];
                                return stmt;
                            } else if (stmt.getOp2().vt == NEW) {
                                tmp[op1._ls_index] = new Vx(init.get(op1), false);
                                return stmt;
                            } else {
                                travel(stmt.getOp2());
                                tmp[op1._ls_index] = IGNORED;
                                return stmt;
                            }
                        }
                    }
                    if (stmt.st == LABEL) {
                        LabelStmt labelStmt = (LabelStmt) stmt;
                        if (labelStmt.phis != null) {
                            for (AssignStmt phi : labelStmt.phis) {
                                Local local = (Local) phi.getOp1();
                                tmp[local._ls_index] = IGNORED;
                            }
                        }
                        return stmt;
                    }
                    return super.travel(stmt);
                }

                @Override
                public Value travel(Value op) {
                    if (op.vt == INVOKE_SPECIAL) {
                        if (op.getOps().length >= 1) {
                            InvokeExpr ie = (InvokeExpr) op;
                            if ("<init>".equals(ie.name)) {
                                Value thiz = op.getOps()[0];
                                if (thiz.vt == LOCAL) {
                                    Local local = (Local) thiz;
                                    Vx vx = tmp[local._ls_index];
                                    TObject object = vx.obj;
                                    if (object != null) {
                                        if (object.invokeStmt != null) {
                                            object.useBeforeInit = true;
                                        } else {
                                            vx.init = true;
                                            object.invokeStmt = current;
                                            for (int i = 0; i < tmp.length; i++) {
                                                Vx s = tmp[i];
                                                if (s != null && s.obj == object) {
                                                    tmp[i] = IGNORED;
                                                }
                                            }
                                            keepFrame = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    op = super.travel(op);

                    if (op.vt == LOCAL) {
                        use((Local) op);
                    }

                    return op;
                }
            };

            @Override
            public Vx[] merge(Vx[] srcFrame, Vx[] distFrame, Stmt src, Stmt dist) {
                if (distFrame == null) {
                    distFrame = new Vx[size];
                    System.arraycopy(srcFrame, 0, distFrame, 0, size);
                } else {
                    for (int i = 0; i < size; i++) {
                        Vx s = srcFrame[i];
                        Vx d = distFrame[i];
                        if (s != null) {
                            if (d == null) {
                                distFrame[i] = s;
                            } else {
                                if (s != d) {
                                    TObject obj = s.obj;
                                    if (obj != null) {
                                        obj.useBeforeInit = true;
                                    }
                                    obj = d.obj;
                                    if (obj != null) {
                                        obj.useBeforeInit = true;
                                    }
                                }
                            }
                        }
                    }
                }

                if (dist.st == LABEL) {
                    List<AssignStmt> phis = ((LabelStmt) dist).phis;
                    if (phis != null && phis.size() > 0) {
                        for (AssignStmt phi : phis) {
                            for (Value value : phi.getOp2().getOps()) {
                                Local local = (Local) value;
                                int i = local._ls_index;
                                Vx s = srcFrame[i];
                                Vx d = distFrame[i];
                                if (d != null) {
                                    if (!d.init) {
                                        TObject obj = d.obj;
                                        if (obj != null) {
                                            obj.useBeforeInit = true;
                                        }
                                    }
                                } else if (s != null) {
                                    if (!s.init) {
                                        TObject obj = s.obj;
                                        if (obj != null) {
                                            obj.useBeforeInit = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return distFrame;
            }

            @Override
            public Vx[] initFirstFrame(Stmt first) {
                return new Vx[size];
            }

            @Override
            public Vx[] exec(Vx[] frame, Stmt stmt) {
                keepFrame = false;
                System.arraycopy(frame, 0, tmp, 0, size);
                stmtTraveler.travel(stmt);
                if (stmt._cfg_froms.size() > 1) {
                    keepFrame = true;
                }

                if (!keepFrame) {
                    stmt.frame = null;
                }
                return tmp;
            }

            void use(Local local) {
                Vx vx = tmp[local._ls_index];
                if (!vx.init) {
                    TObject object = vx.obj;
                    if (object != null) {
                        object.useBeforeInit = true;
                    }

                    tmp[local._ls_index] = IGNORED;
                }

            }
        });
        for (Iterator<Map.Entry<Local, TObject>> iterator = init.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Local, TObject> e = iterator.next();
            boolean keep = true;
            TObject obj = e.getValue();
            if (obj.useBeforeInit) {
                keep = false;
            }
            if (obj.invokeStmt == null) {
                keep = false;
            }
            if (!keep) {
                iterator.remove();
            }
        }
    }

    InvokeExpr findInvokeExpr(Stmt p, InvokeExpr ie) {
        if (p.st == ASSIGN) {
            if (p.getOp2().vt == INVOKE_SPECIAL) {
                ie = (InvokeExpr) p.getOp2();
            }
        } else if (p.st == VOID_INVOKE) {
            ie = (InvokeExpr) p.getOp();
        }
        return ie;
    }

    static class TObject {
        public Stmt invokeStmt;
        Local local;
        boolean useBeforeInit;
        private AssignStmt init;

        TObject(Local local, AssignStmt init) {
            this.local = local;
            this.init = init;
        }
    }

    static class Vx {
        boolean init;
        TObject obj;


        public Vx(TObject obj, boolean init) {
            this.obj = obj;
            this.init = init;
        }
    }


}
