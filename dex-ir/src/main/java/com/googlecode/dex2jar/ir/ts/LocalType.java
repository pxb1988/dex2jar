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
package com.googlecode.dex2jar.ir.ts;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.RefExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;

/**
 * TODO DOC
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class LocalType implements Transformer {

    static class TypeBox {
        XType type = new XType();

        public String toString() {
            return "" + type;
        }
    }

    static class XType {
        Type type;

        public String toString() {
            return "" + type;
        }
    }

    TypeBox get(Value v) {
        TypeBox tb = (TypeBox) v._lt_type;
        if (tb == null) {
            v._lt_type = tb = new TypeBox();
        }
        return tb;
    }

    void type(TypeBox tb, Type t) {
        if (tb.type.type == null) {
            tb.type.type = t;
            return;
        }
        if (tb.type.type.equals(t)) {
            return;
        }
        tb.type.type = merge(tb.type.type, t);
    }

    public static Type type(Value v) {
        TypeBox tb = (TypeBox) (v._lt_type);
        return tb == null ? null : tb.type.type;
    }

    public TypeBox exec(Value v) {
        if (v == null)
            return null;
        TypeBox tb = get(v);
        switch (v.et) {
        case E0:
            switch (v.vt) {
            case CONSTANT:
                Type cstType = ((Constant) v).type;
                if (cstType.equals(Type.getType(Class.class)) || cstType.equals(Type.getType(String.class))) {
                    type(tb, ((Constant) v).type);
                }
                break;
            case LOCAL:
                break;
            case THIS_REF:
            case PARAMETER_REF:
            case EXCEPTION_REF:
                type(tb, ((RefExpr) v).type);
                break;
            }
            break;
        case E1:
            switch (v.vt) {
            case FIELD:
                FieldExpr fe = (FieldExpr) v;
                type(tb, fe.fieldType);
                if (fe.op != null) {
                    type(exec(fe.op.value), fe.fieldOwnerType);
                }
                break;
            case NEW_ARRAY: {
                TypeExpr te = (TypeExpr) v;
                type(exec(te.op.value), Type.INT_TYPE);
                type(tb, Type.getType("[" + te.type.getDescriptor()));
            }
                break;
            case CHECK_CAST:
            case CAST: {
                TypeExpr te = (TypeExpr) v;
                exec(te.op.value);
                type(tb, te.type);
            }
                break;
            case INSTANCE_OF: {
                TypeExpr te = (TypeExpr) v;
                type(exec(te.op.value), Type.getType(Object.class));
                type(tb, Type.BOOLEAN_TYPE);
            }
                break;
            case LENGTH: {
                TypeExpr te = (TypeExpr) v;
                type(exec(te.op.value), Type.getType(Object.class));
                type(tb, Type.INT_TYPE);
            }
                break;
            case NEG: {
                TypeExpr te = (TypeExpr) v;
                merge(tb, exec(te.op.value));
            }
                break;
            }
            break;
        case E2:
            TypeBox tb1 = exec(((E2Expr) v).op1.value);
            TypeBox tb2 = exec(((E2Expr) v).op2.value);
            switch (v.vt) {
            case ARRAY:
                break;
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case REM:
            case AND:
            case OR:
            case XOR:
                merge(tb1, tb2);
                merge(tb1, tb);
                break;
            case SHL:
            case SHR:
            case USHR:
                merge(tb, tb1);
                type(tb2, Type.INT_TYPE);
                break;
            case CMP:
            case CMPG:
            case CMPL:
                merge(tb1, tb2);
                type(tb, Type.INT_TYPE);
                break;
            case GE:
            case GT:
            case LE:
            case LT:
            case EQ:
            case NE:
                merge(tb1, tb2);
                type(tb, Type.BOOLEAN_TYPE);
                break;
            }
        case En:
            switch (v.vt) {
            case INVOKE_NEW:
            case INVOKE_STATIC: {
                InvokeExpr ie = (InvokeExpr) v;
                for (int i = 0; i < ie.ops.length; i++) {
                    type(exec(ie.ops[i].value), ie.argmentTypes[i]);
                }
                if (v.vt == VT.INVOKE_NEW) {
                    type(tb, ie.methodOwnerType);
                } else {
                    type(tb, ie.methodReturnType);
                }
            }
                break;
            case INVOKE_INTERFACE:
            case INVOKE_SPECIAL:
            case INVOKE_VIRTUAL: {
                InvokeExpr ie = (InvokeExpr) v;
                type(exec(ie.ops[0].value), ie.methodOwnerType);
                for (int i = 1; i < ie.ops.length; i++) {
                    type(exec(ie.ops[i].value), ie.argmentTypes[i - 1]);
                }
                type(tb, ie.methodReturnType);
            }
                break;
            case NEW_MUTI_ARRAY:
                throw new RuntimeException();
            }
        }
        return tb;
    }

    Type merge(Type t2, Type t1) {
        return t2;
    }

    @Override
    public void transform(IrMethod irMethod) {
        // type detect
        for (Stmt st : irMethod.stmts) {
            switch (st.et) {
            case E0:
            case En:
                break;
            case E1:
                E1Stmt s1 = (E1Stmt) st;
                switch (st.st) {
                case GOTO:
                    break;
                case IF:
                    type(exec(s1.op.value), Type.BOOLEAN_TYPE);
                    break;
                case THROW:
                    type(exec(s1.op.value), Type.getType(Throwable.class));
                    break;
                case RETURN:
                    type(exec(s1.op.value), irMethod.ret);
                    break;
                case LOCK:
                case UNLOCK:
                    type(exec(s1.op.value), Type.getType(Object.class));
                    break;
                case LOOKUP_SWITCH:
                case TABLE_SWITCH:
                    type(exec(s1.op.value), Type.INT_TYPE);
                    break;
                }
                break;
            case E2:
                E2Stmt s2 = (E2Stmt) st;
                TypeBox tb1 = exec(s2.op1.value);
                TypeBox tb2 = exec(s2.op2.value);
                merge(tb1, tb2);
                break;
            }
        }
    }

    private void merge(TypeBox tb1, TypeBox tb2) {
        if (tb1.type.type == null) {
            tb1.type = tb2.type;
            return;
        }
        if (tb2.type.type == null) {
            tb2.type = tb1.type;
            return;
        }
        Type nt = merge(tb1.type.type, tb2.type.type);
        tb1.type = tb2.type;
        tb2.type.type = nt;
    }
}
