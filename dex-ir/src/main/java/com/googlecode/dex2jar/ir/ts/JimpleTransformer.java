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

import java.util.ArrayList;
import java.util.List;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.Stmts;

/**
 * transforme IR to simple 3-addr format
 * 
 * a=b+c+d; => e=b+c; a=e+d;
 * 
 * @author bob
 * 
 */
public class JimpleTransformer implements Transformer {

    static class N {
        public List<Stmt> tmp;
        int nextIdx;
        private List<Local> locals;

        public N(List<Stmt> tmp, List<Local> locals) {
            super();
            this.tmp = tmp;
            this.locals = locals;
            nextIdx = locals.size();
        }

        Value newAssign(Value x) {
            Local loc = Exprs.nLocal(nextIdx++);
            loc.valueType = x.valueType;
            locals.add(loc);
            tmp.add(Stmts.nAssign(loc, x));
            return loc;
        }

    }

    @Override
    public void transform(IrMethod method) {
        List<Stmt> tmp = new ArrayList<>();
        N n = new N(tmp, method.locals);
        for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
            tmp.clear();
            convertStmt(p, n);
            for (Stmt t : tmp) {
                method.stmts.insertBefore(p, t);
            }

        }
    }

    private Value convertExpr(Value x, boolean keep, N tmp) {
        switch (x.et) {
        case E0:
            if (!keep) {
                switch (x.vt) {
                case CONSTANT:
                    Constant cst = (Constant) x;
                    if (cst.value instanceof String || cst.value instanceof Constant.Type
                            || cst.value.getClass().isArray()) {
                        return tmp.newAssign(x);
                    }
                    break;
                case NEW:
                case STATIC_FIELD:
                    return tmp.newAssign(x);
                default:
                }
            }
            break;
        case E1:
            x.setOp(convertExpr(x.getOp(), false, tmp));
            if (!keep) {
                return tmp.newAssign(x);
            }
            break;
        case E2:
            x.setOp1(convertExpr(x.getOp1(), false, tmp));
            x.setOp2(convertExpr(x.getOp2(), false, tmp));
            if (!keep) {
                return tmp.newAssign(x);
            }
            break;
        case En:
            Value[] ops = x.getOps();
            for (int i = 0; i < ops.length; i++) {
                ops[i] = convertExpr(ops[i], false, tmp);
            }
            if (!keep) {
                return tmp.newAssign(x);
            }
            break;
        }

        return x;
    }

    private void convertStmt(Stmt p, N tmp) {
        switch (p.et) {
        case E0:
            return;
        case E1:
            boolean keep;
            switch (p.st) {
            case LOOKUP_SWITCH:
            case TABLE_SWITCH:
            case RETURN:
            case THROW:
                keep = false;
                break;
            default:
                keep = true;
                break;
            }
            p.setOp(convertExpr(p.getOp(), keep, tmp));
            break;
        case E2:
            if (p.st == ST.IDENTITY) {
                return;
            } else if (p.st == ST.FILL_ARRAY_DATA) {
                p.setOp1(convertExpr(p.getOp1(), false, tmp));
                p.setOp2(convertExpr(p.getOp2(), true, tmp));
            } else {
                p.setOp1(convertExpr(p.getOp1(), true, tmp));
                p.setOp2(convertExpr(p.getOp2(), p.getOp1().vt == VT.LOCAL, tmp));
            }
            break;
        case En:
            Value[] ops = p.getOps();
            for (int i = 0; i < ops.length; i++) {
                ops[i] = convertExpr(ops[i], true, tmp);
            }
            break;
        }
    }
}
