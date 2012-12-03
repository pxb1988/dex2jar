/*
 * dex2jar - Tools to work with android .dex and java .class files
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E0Expr;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.TypeRef;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.BinopExpr;
import com.googlecode.dex2jar.ir.expr.CastExpr;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.FilledArrayExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.NewExpr;
import com.googlecode.dex2jar.ir.expr.NewMutiArrayExpr;
import com.googlecode.dex2jar.ir.expr.RefExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.expr.UnopExpr;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

/**
 * Analyze Expr types
 * 
 * @author Panxiaobo
 * 
 */
public class TypeAnalyze {

    public static class DefTypeRef implements TypeRef {
        static Comparator<Type> c = new Comparator<Type>() {
            int x[] = { 999, 1, 4, 2, 3, 5, 6, 7, 8, 10, 9 };

            @Override
            public int compare(Type o1, Type o2) {
                if (o1.equals(o2)) {
                    return 0;
                }
                int s1 = o1.getSort();
                int s2 = o2.getSort();
                if (s1 == s2) {
                    switch (s1) {
                    case Type.OBJECT:
                        if (o1.equals(Type.getType(Object.class))) {
                            return 1;
                        } else if (o2.equals(Type.getType(Object.class))) {
                            return -1;
                        } else {
                            return o1.getDescriptor().compareTo(o2.getDescriptor());
                        }
                    case Type.ARRAY:
                        int a = o1.getDimensions();
                        int b = o2.getDimensions();
                        if (a == b) {
                            if (o1.getElementType().equals(Type.getType(Object.class))) {
                                return 1;
                            } else if (o2.getElementType().equals(Type.getType(Object.class))) {
                                return -1;
                            } else {
                                return o1.getElementType().getDescriptor()
                                        .compareTo(o2.getElementType().getDescriptor());
                            }
                        } else {
                            return b - a;
                        }
                    }
                } else {
                    return x[s2] - x[s1];
                }
                return 0;
            }
        };

        // static Comparator<Type> c = new Comparator<Type>() {
        //
        // @Override
        // public int compare(Type o1, Type o2) {
        // return o1.getDescriptor().compareTo(o2.getDescriptor());
        // }
        // };

        private static String getShort(String desc) {
            char c = desc.charAt(0);
            if (c == '[') {
                StringBuilder sb = new StringBuilder();
                sb.append(c);

                for (int i = 1; i < desc.length(); i++) {
                    c = desc.charAt(i);
                    if (c == '[') {
                        sb.append(c);
                    } else {
                        break;
                    }
                }
                sb.append(c);
                return sb.toString();
            } else {
                return Character.toString(c);
            }
        }

        public Set<DefTypeRef> sameValues = new HashSet<DefTypeRef>();
        /**
         * reference to values
         */
        public Set<DefTypeRef> arrayValues = new HashSet<DefTypeRef>();
        /**
         * reference to roots
         */
        public Set<DefTypeRef> arryRoots = new HashSet<DefTypeRef>();

        public Set<DefTypeRef> froms = new HashSet<DefTypeRef>();
        public Set<Type> providerAs = new TreeSet<Type>(c);
        public Set<DefTypeRef> tos = new HashSet<DefTypeRef>();
        public Type type;
        public Set<Type> useAs = new TreeSet<Type>(c);

        Value value;

        @Override
        public Type get() {
            return type;
        }

        @Override
        public String toString() {
            if (type != null) {
                return type.toString();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(value).append(": ");
            if (providerAs != null) {
                for (Type t : providerAs) {
                    sb.append(getShort(t.getDescriptor()));
                }
            }
            sb.append(" > ");
            if (useAs != null) {
                for (Type t : useAs) {
                    sb.append(getShort(t.getDescriptor()));
                }
            }
            return sb.toString();
        }
    }

    protected IrMethod method;
    List<DefTypeRef> refs = new ArrayList<DefTypeRef>();

    public TypeAnalyze(IrMethod method) {
        super();
        this.method = method;
    }

    public List<DefTypeRef> analyze() {
        sxStmt();
        {// fix DefTypeRef

            fixProvidAs();
        }
        return refs;
    }

    private void e0expr(E0Expr op) {
        switch (op.vt) {
        case LOCAL:
            break;
        case NEW:
            NewExpr newExpr = (NewExpr) op;
            provideAs(newExpr, newExpr.type);
            break;
        case THIS_REF:
        case PARAMETER_REF:
        case EXCEPTION_REF:
            RefExpr refExpr = (RefExpr) op;
            Type refType = refExpr.type;
            if (refType == null && op.vt == VT.EXCEPTION_REF) {
                refType = Type.getType(Throwable.class);
            }
            provideAs(refExpr, refType);
            break;
        case CONSTANT:
            Constant cst = (Constant) op;
            // Type type = cst.type;
            Object value = cst.value;
            if (value instanceof String) {
                provideAs(cst, Type.getType(String.class));
            } else if (value instanceof Type) {
                provideAs(cst, Type.getType(Class.class));
            } else if (value instanceof Number) {
                if (value instanceof Integer || value instanceof Byte || value instanceof Short
                        || value instanceof Character) {
                    int a = ((Number) value).intValue();
                    if (a >= 0 && a <= 1) {
                        provideAs(cst, Type.BOOLEAN_TYPE);
                    } else if (a >= Byte.MIN_VALUE && a <= Byte.MAX_VALUE) {
                        provideAs(cst, Type.BYTE_TYPE);
                    } else if (a >= Short.MIN_VALUE && a <= Short.MAX_VALUE) {
                        provideAs(cst, Type.SHORT_TYPE);
                    } else if (a >= Character.MIN_VALUE && a <= Character.MAX_VALUE) {
                        provideAs(cst, Type.CHAR_TYPE);
                    } else {
                        provideAs(cst, Type.INT_TYPE);
                    }
                } else if (value instanceof Long) {
                    provideAs(cst, Type.LONG_TYPE);
                } else if (value instanceof Float) {
                    provideAs(cst, Type.FLOAT_TYPE);
                } else if (value instanceof Double) {
                    provideAs(cst, Type.DOUBLE_TYPE);
                }
            }
            break;
        }
    }

    private void e1expr(E1Expr e1, boolean getValue) {
        Value v = e1.op != null ? e1.op.value : null;
        switch (e1.vt) {
        case CAST:
            CastExpr ce = (CastExpr) e1;
            useAs(v, ce.from);
            provideAs(e1, ce.to);
            break;
        case FIELD:
            FieldExpr fe = (FieldExpr) e1;
            if (getValue) {// getfield
                provideAs(fe, fe.fieldType);
            } else {// putfield
                useAs(fe, fe.fieldType);
            }
            if (v != null) {
                useAs(v, fe.fieldOwnerType);
            }
            break;

        case CHECK_CAST: {
            TypeExpr te = (TypeExpr) e1;
            provideAs(te, te.type);
            useAs(v, Type.getType(Object.class));
        }
            break;
        case INSTANCE_OF: {
            TypeExpr te = (TypeExpr) e1;
            provideAs(te, Type.BOOLEAN_TYPE);
            useAs(v, Type.getType(Object.class));
        }
            break;
        case NEW_ARRAY: {
            TypeExpr te = (TypeExpr) e1;
            provideAs(te, Type.getType("[" + te.type.getDescriptor()));
            useAs(v, Type.INT_TYPE);
        }
            break;
        case LENGTH: {
            UnopExpr ue = (UnopExpr) e1;
            provideAs(ue, Type.INT_TYPE);
            useAs(v, Type.getType(Object.class));// FIXME type it
        }
            break;
        case NEG:
        case NOT: {
            UnopExpr ue = (UnopExpr) e1;
            provideAs(ue, ue.type);
            useAs(v, ue.type);
        }
            break;
        }
        if (v != null) {
            exExpr(v);
        }
    }

    private void e2expr(E2Expr e2) {
        Value a = e2.op1.value;
        Value b = e2.op2.value;
        switch (e2.vt) {
        case ARRAY:
            useAs(b, Type.INT_TYPE);
            useAs(a, Type.getType(Object.class));
            linkArray(a, e2);
            break;
        case LCMP:
        case FCMPG:
        case FCMPL:
        case DCMPG:
        case DCMPL: {
            BinopExpr be = (BinopExpr) e2;
            useAs(a, be.type);
            useAs(b, be.type);
            provideAs(e2, Type.INT_TYPE);
        }
            break;
        case EQ:
        case NE: {
            if (//
            (a.vt == VT.CONSTANT && (Integer.valueOf(0).equals(((Constant) a).value)))// a is zero
                    || (b.vt == VT.CONSTANT && (Integer.valueOf(0).equals(((Constant) b).value)))// b is zero
            ) {
                useAs(a, Type.BOOLEAN_TYPE);
                useAs(b, Type.BOOLEAN_TYPE);
                sameAs(a, b);
            } else {
                BinopExpr be = (BinopExpr) e2;
                useAs(a, be.type);
                useAs(b, be.type);
            }
            provideAs(e2, Type.BOOLEAN_TYPE);
        }
            break;
        case GE:
        case GT:
        case LE:
        case LT: {
            BinopExpr be = (BinopExpr) e2;
            useAs(a, be.type);
            useAs(b, be.type);
            provideAs(e2, Type.BOOLEAN_TYPE);
        }
            break;
        case ADD:
        case SUB:
        case AND:
        case DIV:
        case MUL:
        case OR:
        case REM:
        case XOR: {
            BinopExpr be = (BinopExpr) e2;
            useAs(a, be.type);
            useAs(b, be.type);
            provideAs(e2, be.type);
        }
            break;
        case SHL:
        case SHR:
        case USHR: {
            BinopExpr be = (BinopExpr) e2;
            useAs(a, be.type);
            useAs(b, Type.INT_TYPE);
            provideAs(e2, be.type);
        }
            break;
        default:
            throw new UnsupportedOperationException();
        }
        if (a != null) {
            exExpr(a);
        }
        if (b != null) {
            exExpr(b);
        }
    }

    private void sameAs(Value a, Value b) {
        DefTypeRef aa = getDefTypeRef(a);
        DefTypeRef bb = getDefTypeRef(b);
        aa.sameValues.add(bb);
        bb.sameValues.add(aa);
    }

    private void enexpr(EnExpr enExpr) {
        ValueBox vbs[] = enExpr.ops;
        switch (enExpr.vt) {
        case INVOKE_NEW:
        case INVOKE_INTERFACE:
        case INVOKE_SPECIAL:
        case INVOKE_STATIC:
        case INVOKE_VIRTUAL:
            InvokeExpr ie = (InvokeExpr) enExpr;
            provideAs(enExpr, ie.vt == VT.INVOKE_NEW ? ie.methodOwnerType : ie.methodReturnType);
            int start = 0;
            if (ie.vt != VT.INVOKE_STATIC && ie.vt != VT.INVOKE_NEW) {
                start = 1;
                useAs(vbs[0].value, ie.methodOwnerType);
            }
            for (int i = 0; start < vbs.length; start++, i++) {
                useAs(vbs[start].value, ie.argmentTypes[i]);
            }
            break;
        case FILLED_ARRAY:
            FilledArrayExpr fae = (FilledArrayExpr) enExpr;
            for (ValueBox vb : vbs) {
                useAs(vb.value, fae.type);
            }
            provideAs(fae, Type.getType("[" + fae.type.getDescriptor()));
            break;
        case NEW_MUTI_ARRAY:
            NewMutiArrayExpr nmae = (NewMutiArrayExpr) enExpr;
            for (ValueBox vb : vbs) {
                useAs(vb.value, Type.INT_TYPE);
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nmae.dimension; i++) {
                sb.append('[');
            }
            sb.append(nmae.baseType.getDescriptor());
            provideAs(nmae, Type.getType(sb.toString()));
            break;
        }
        for (ValueBox vb : enExpr.ops) {
            exExpr(vb.value);
        }
    }

    private void exExpr(Value op) {
        exExpr(op, true);
    }

    private void exExpr(Value op, boolean getValue) {

        switch (op.et) {
        case E0:
            e0expr((E0Expr) op);
            break;
        case E1:
            e1expr((E1Expr) op, getValue);
            break;
        case E2:
            e2expr((E2Expr) op);
            break;
        case En:
            enexpr((EnExpr) op);
            break;
        }
    }

    void fixProvidAs() {
        LinkedList<DefTypeRef> queue = new LinkedList<DefTypeRef>();
        queue.addAll(refs);
        Set<DefTypeRef> cache = new HashSet<DefTypeRef>();
        while (!queue.isEmpty()) {
            DefTypeRef ref = queue.poll();
            if (ref.providerAs.size() > 0) {
                Type t = ref.providerAs.iterator().next();
                for (DefTypeRef subref : ref.tos) {
                    if (subref.providerAs.addAll(ref.providerAs)) {
                        cache.add(subref);
                    }
                }
                if (ref.arrayValues.size() > 0) {
                    if (t.getSort() == Type.ARRAY) {
                        Type elementType = Type.getType(t.getDescriptor().substring(1));
                        boolean object = false;
                        switch (elementType.getSort()) {
                        case Type.OBJECT:
                        case Type.ARRAY:
                        case Type.FLOAT:
                        case Type.DOUBLE:
                            object = true;
                        }
                        for (DefTypeRef subref : ref.arrayValues) {
                            boolean needAdd = subref.providerAs.add(elementType);
                            if (object) {
                                needAdd = subref.useAs.add(elementType) || needAdd;
                            }
                            if (needAdd) {
                                cache.add(subref);
                            }
                        }
                    }
                }
                switch (t.getSort()) {
                case Type.OBJECT:
                case Type.ARRAY:
                    t = Type.getType(Object.class);
                case Type.FLOAT:
                case Type.DOUBLE:
                    ref.useAs.add(t);
                    break;
                }

            }
            if (ref.useAs.size() > 0) {
                for (DefTypeRef subref : ref.froms) {
                    if (subref.useAs.addAll(ref.useAs)) {
                        cache.add(subref);
                    }
                }
                Type t = ref.useAs.iterator().next();
                for (DefTypeRef subref : ref.sameValues) {
                    if (subref.useAs.add(t)) {
                        cache.add(subref);
                    }
                }
            }
            for (DefTypeRef subref : cache) {
                if (!queue.contains(subref)) {
                    queue.add(subref);
                }
            }
            cache.clear();

        }
    }

    private DefTypeRef getDefTypeRef(Value v) {
        DefTypeRef typeRef = (DefTypeRef) v.typeRef;
        if (typeRef == null) {
            typeRef = new DefTypeRef();
            typeRef.value = v;
            refs.add(typeRef);
            v.typeRef = typeRef;
        }
        return typeRef;
    }

    private void linkArray(Value array, Value v) {
        DefTypeRef root = getDefTypeRef(array);
        DefTypeRef value = getDefTypeRef(v);
        root.arrayValues.add(value);
        value.arryRoots.add(root);
    }

    private void linkFromTo(Value from, Value to) {
        DefTypeRef tFrom = getDefTypeRef(from);
        DefTypeRef tTo = getDefTypeRef(to);
        tFrom.tos.add(tTo);
        tTo.froms.add(tFrom);
    }

    private void provideAs(Value op, Type type) {
        getDefTypeRef(op).providerAs.add(type);
    }

    private void s1stmt(E1Stmt s) {
        if (s.st == ST.GOTO) {
            return;
        }
        Value op = s.op.value;
        switch (s.st) {
        case LOOKUP_SWITCH:
        case TABLE_SWITCH:
            useAs(op, Type.INT_TYPE);
            break;
        case GOTO:
            break;
        case IF:
            useAs(op, Type.BOOLEAN_TYPE);
            break;
        case LOCK:
        case UNLOCK:
            useAs(op, Type.getType(Object.class));
            break;
        case THROW:
            useAs(op, Type.getType(Throwable.class));
            break;
        case RETURN:
            useAs(op, method.ret);
            break;
        }
        if (op != null) {
            exExpr(op);
        }
    }

    private void s2stmt(E2Stmt s) {
        Value from = s.op2.value;
        Value to = s.op1.value;
        linkFromTo(from, to);
        exExpr(from);
        exExpr(to, false);
    }

    private void sxStmt() {
        for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
            switch (p.et) {
            case E0:
                // label, nop and return-void
                break;
            case E1:
                s1stmt((E1Stmt) p);
                break;
            case E2:
                s2stmt((E2Stmt) p);
                break;
            case En:
                // no stmt yet
                // enstmt((EnStmt) p, refs, relationRefs);
                break;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DefTypeRef ref : refs) {
            sb.append(ref).append("\n");
        }
        return sb.toString();
    }

    private void useAs(Value op, Type type) {
        getDefTypeRef(op).useAs.add(type);
    }
}
