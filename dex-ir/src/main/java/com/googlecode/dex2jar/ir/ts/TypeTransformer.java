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

import com.googlecode.d2j.DexType;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.TypeClass;
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.expr.Value.*;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

import java.util.*;

/**
 * Type and correct Exprs
 *
 * @author Bob Pan
 */
public class TypeTransformer implements Transformer {

    private static final String[] possibleIntTypes = new String[]{"B", "S", "C", "I"};

    @Override
    public void transform(IrMethod irMethod) {
        TypeAnalyze ta = new TypeAnalyze(irMethod);
        List<TypeRef> refs = ta.analyze();

        for (TypeRef ref : refs) {
            String type = ref.getType();

            if (type == null) {
                System.err.println(ref);
                continue;
            }

            if (ref.value.vt == VT.CONSTANT) {
                Constant cst = (Constant) ref.value;
                switch (type.charAt(0)) {
                    case '[':
                    case 'L':
                        if (Integer.valueOf(0).equals(cst.value)) {
                            cst.value = Constant.Null;
                        }
                        if (type.equals("[F") && cst.value instanceof int[]) {
                            int x[] = (int[]) cst.value;
                            float f[] = new float[x.length];
                            for (int i = 0; i < x.length; i++) {
                                f[i] = Float.intBitsToFloat(x[i]);
                            }
                            cst.value = f;
                        }
                        if (type.equals("[D") && cst.value instanceof long[]) {
                            long x[] = (long[]) cst.value;
                            double f[] = new double[x.length];
                            for (int i = 0; i < x.length; i++) {
                                f[i] = Double.longBitsToDouble(x[i]);
                            }
                            cst.value = f;
                        }
                        break;
                    case 'F':
                        if (!(cst.value instanceof Float)) {
                            cst.value = Float.intBitsToFloat(((Number) cst.value).intValue());
                        }
                        break;
                    case 'D':
                        if (!(cst.value instanceof Double)) {
                            cst.value = Double.longBitsToDouble(((Number) cst.value).longValue());
                        }
                        break;
                    default:
                }
            }
            Value value = ref.value;
            value.valueType = type;
            value.tag = null;
            ref.clear();
        }
    }

    enum Relation {

        R_sameValues {
            @Override
            Set<TypeRef> get(TypeRef obj) {
                return obj.sameValues;
            }

            @Override
            void set(TypeRef obj, Set<TypeRef> v) {
                obj.sameValues = v;
            }
        }, R_gArrayValues {
            @Override
            Set<TypeRef> get(TypeRef obj) {
                return obj.gArrayValues;
            }

            @Override
            void set(TypeRef obj, Set<TypeRef> v) {
                obj.gArrayValues = v;
            }
        }, R_sArrayValues {
            @Override
            Set<TypeRef> get(TypeRef obj) {
                return obj.sArrayValues;
            }

            @Override
            void set(TypeRef obj, Set<TypeRef> v) {
                obj.sArrayValues = v;
            }
        }, R_arrayRoots {
            @Override
            Set<TypeRef> get(TypeRef obj) {
                return obj.arrayRoots;
            }

            @Override
            void set(TypeRef obj, Set<TypeRef> v) {
                obj.arrayRoots = v;
            }
        }, R_parents {
            @Override
            Set<TypeRef> get(TypeRef obj) {
                return obj.parents;
            }

            @Override
            void set(TypeRef obj, Set<TypeRef> v) {
                obj.parents = v;
            }
        }, R_children {
            @Override
            Set<TypeRef> get(TypeRef obj) {
                return obj.children;
            }

            @Override
            void set(TypeRef obj, Set<TypeRef> v) {
                obj.children = v;
            }
        };

        abstract Set<TypeRef> get(TypeRef obj);

        abstract void set(TypeRef obj, Set<TypeRef> v);
    }

    public static class TypeRef {

        public final Value value;
        /**
         * same use, have same
         */
        public Set<TypeRef> sameValues = null;
        /**
         * reference to values
         */
        public Set<TypeRef> gArrayValues = null;
        public Set<TypeRef> sArrayValues = null;
        /**
         * reference to root
         */
        public Set<TypeRef> arrayRoots = null;

        public Set<TypeRef> parents = null;
        public Set<TypeRef> children = null;

        public TypeClass clz = TypeClass.UNKNOWN;
        public String provideDesc = null;
        public Set<String> uses;

        private TypeRef next;

        public void merge(TypeRef other) {
            assert this.next == null;
            TypeRef a = this;
            TypeRef b = other.getReal();
            if (a == b) {
                return;
            }

            b.next = a;

            relationMerge(a, b, Relation.R_sameValues);
            relationMerge(a, b, Relation.R_gArrayValues);
            relationMerge(a, b, Relation.R_sArrayValues);
            relationMerge(a, b, Relation.R_arrayRoots);
            relationMerge(a, b, Relation.R_parents);
            relationMerge(a, b, Relation.R_children);

            if (a.provideDesc == null) {
                a.provideDesc = b.provideDesc;
            } else if (b.provideDesc != null) {
                a.provideDesc = TypeAnalyze.mergeProviderType(a.provideDesc, b.provideDesc);
            }
            b.provideDesc = null;
            if (b.uses != null) {
                if (a.uses == null) {
                    a.uses = b.uses;
                } else {
                    a.uses.addAll(b.uses);
                }
                b.uses = null;
            }

        }

        private static void relationMerge(TypeRef a, TypeRef b, Relation r) {
            Set<TypeRef> bv = r.get(b);
            if (bv != null) {
                Set<TypeRef> av = r.get(a);
                Set<TypeRef> merged;
                if (av == null) {
                    merged = bv;
                    r.set(a, merged);
                } else {
                    merged = av;
                    merged.addAll(bv);
                }
                merged.remove(a);
                merged.remove(b);
                r.set(b, null);
            }
        }

        private TypeRef getReal() {
            TypeRef x = this;
            while (x.next != null) {
                x = x.next;
            }
            if (x != this) {
                this.next = x;
            }
            return x;
        }

        public TypeRef(Value value) {
            super();
            this.value = value;
        }

        @Override
        public String toString() {
            TypeRef real = getReal();
            String p = real.uses == null ? "[]" : real.uses.toString();
            return real.clz + "::" + value + ": " + real.provideDesc + " > {" + p.substring(1, p.length() - 1) + "}";
        }

        public String getType() {
            TypeRef thiz = getReal();
            TypeClass clz = thiz.clz;
            if (clz == TypeClass.OBJECT) {
                if (thiz.provideDesc.length() == 1) {
                    return "Ljava/lang/Object;";
                } else {
                    return thiz.provideDesc;
                }
            }
            if (clz.fixed && clz != TypeClass.INT) {
                if (thiz.provideDesc == null) {
                    throw new RuntimeException();
                }
                return thiz.provideDesc;
            }
            if (clz == TypeClass.JD) { // prefere Long if wide
                return "J";
            }
            if (thiz.uses != null) {
                for (String t : possibleIntTypes) {
                    if (thiz.uses.contains(t)) {
                        return t;
                    }
                }
            }

            switch (clz) {
                case ZI:
                    return "I";
                case ZIFL:
                case ZIF:
                case ZIL:
                    return "Z";
                case INT:
                case IF:
                    return "I";
                default:
            }
            throw new RuntimeException();
        }

        public boolean updateTypeClass(TypeClass clz) {
            assert this.next == null;
            TypeClass thizClz = this.clz;
            TypeClass merged = TypeClass.merge(thizClz, clz);
            if (merged == thizClz) {
                return false;
            }
            this.clz = merged;
            return true;
        }

        public void clear() {
            this.sArrayValues = null;
            this.gArrayValues = null;
            this.arrayRoots = null;
            this.parents = null;
            this.children = null;
            this.sameValues = null;
        }

        String getProvideDesc() {
            return getReal().provideDesc;
        }

        public boolean addUses(String ele) {
            assert this.next == null;
            TypeRef t = this;
            if (t.uses != null) {
                return t.uses.add(ele);
            } else {
                t.uses = new HashSet<>();
                return t.uses.add(ele);
            }
        }

        public boolean addAllUses(Set<String> uses) {
            assert this.next == null;
            if (uses != null) {
                return uses.addAll(uses);
            } else {
                uses = new HashSet<>();
                return uses.addAll(uses);
            }
        }
    }

    private static class TypeAnalyze {
        protected IrMethod method;
        private List<TypeRef> refs = new ArrayList<>();

        public TypeAnalyze(IrMethod method) {
            super();
            this.method = method;
        }

        public List<TypeRef> analyze() {
            sxStmt();
            fixTypes();
            return refs;
        }

        private void fixTypes() {
            // 1. collect all Array Roots
            Set<TypeRef> arrayRoots = new HashSet<>();
            for (TypeRef ref : refs) {
                ref = ref.getReal();
                if (ref.gArrayValues != null || ref.sArrayValues != null) {
                    arrayRoots.add(ref);
                }
                mergeArrayRelation(ref, Relation.R_gArrayValues);
                mergeArrayRelation(ref, Relation.R_sArrayValues);
                mergeArrayRelation(ref, Relation.R_arrayRoots);
            }

            UniqueQueue<TypeRef> q = new UniqueQueue<>();
            q.addAll(refs);
            while (!q.isEmpty()) {
                // 2. merge provided type to children. merge uses to parent. merge TypeClass to sameValues
                while (!q.isEmpty()) {
                    TypeRef ref = q.poll();
                    copyTypes(q, ref);
                }
                // 3. merge type from Array Roots to Array Values
                for (TypeRef ref : arrayRoots) {
                    ref = ref.getReal();
                    String provideDesc = ref.provideDesc;
                    if (provideDesc != null && provideDesc.charAt(0) == '[') {
                        String ele = provideDesc.substring(1);

                        TypeClass clz = TypeClass.clzOf(ele);
                        if (ref.gArrayValues != null) {
                            for (TypeRef p : ref.gArrayValues) {
                                p = p.getReal();
                                if (p.updateTypeClass(clz)) {
                                    q.add(p);
                                }
                                mergeTypeToArrayGetValue(ele, p, q);
                            }
                        }
                        if (ref.sArrayValues != null) {
                            for (TypeRef p : ref.sArrayValues) {
                                p = p.getReal();
                                if (p.updateTypeClass(clz)) {
                                    q.add(p);
                                }
                                if (p.addUses(ele)) {
                                    q.add(p);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void mergeArrayRelation(TypeRef ref, Relation r) {
            Set<TypeRef> v = r.get(ref);
            if (v != null && v.size() > 1) {
                List<TypeRef> copy = new ArrayList<>(v);
                TypeRef mergeTo = copy.get(0).getReal();
                for (int i = 1; i < copy.size(); i++) {
                    mergeTo.merge(copy.get(i));
                }
            }
        }

        private static void mergeTypeToArrayGetValue(String type, TypeRef target, UniqueQueue<TypeRef> q) {
            target = target.getReal();
            if (target.provideDesc == null) {
                target.provideDesc = type;
                q.add(target);
            } else {
                String mergedType = mergeTypeEx(type, target.provideDesc);
                if (!mergedType.equals(target.provideDesc)) {
                    target.provideDesc = mergedType;
                    q.add(target);
                }
            }
        }

        private static void mergeTypeToSubRef(String type, TypeRef target, UniqueQueue<TypeRef> q) {
            if (target.provideDesc == null) {
                target.provideDesc = type;
                q.add(target);
            } else {
                String mergedType = mergeProviderType(type, target.provideDesc);
                if (!mergedType.equals(target.provideDesc)) {
                    target.provideDesc = mergedType;
                    q.add(target);
                }
            }
        }

        /**
         * [[B + [[D -> [L
         * [B + L -> [B
         * [[B + [B -> [[B
         *
         * @param a
         * @param b
         * @return
         */
        private static String mergeTypeEx(String a, String b) {
            if (a.equals(b)) {
                return a;
            }
            int as = countArrayDim(a);
            int bs = countArrayDim(b);
            if (as > bs) {
                return a;
            } else if (bs > as) {
                return b;
            } else { // as==bs;
                String elementTypeA = a.substring(as);
                String elementTypeB = a.substring(bs);
                TypeClass ta = TypeClass.clzOf(elementTypeA);
                TypeClass tb = TypeClass.clzOf(elementTypeB);
                if (ta.fixed && !tb.fixed) {
                    return a;
                } else if (!ta.fixed && tb.fixed) {
                    return b;
                } else if (ta.fixed && tb.fixed) {
                    if (ta != tb) {
                        if (as == 0) {
                            throw new RuntimeException();
                        }
                        return buildArray(as - 1, "L");
                    }
                    if (ta == TypeClass.INT) {
                        String chooseType = "I";
                        for (int i = possibleIntTypes.length - 1; i >= 0; i--) {
                            String t = possibleIntTypes[i];
                            if (a.equals(t) || b.equals(t)) {
                                chooseType = t;
                                break;
                            }
                        }
                        return buildArray(as, chooseType);
                    } else {
                        return buildArray(as, "L");
                    }
                } else { // !ta.fixed && !tb.fixed
                    return buildArray(as, TypeClass.merge(ta, tb).name);
                }
            }
        }

        private void copyTypes(UniqueQueue<TypeRef> q, TypeRef ref) {
            ref = ref.getReal();
            TypeClass clz = ref.clz;

            switch (clz) {
                case BOOLEAN:
                case FLOAT:
                case LONG:
                case DOUBLE:
                case VOID:
                    ref.provideDesc = clz.name;
                    break;
                default:
            }
            String provideDesc = ref.provideDesc;
            if (provideDesc == null && ref.parents != null && ref.parents.size() > 1) {
                if (isAllParentSetted(ref)) {
                    ref.provideDesc = provideDesc = mergeParentType(ref.parents);
                }
            }
            if (ref.parents != null) {
                for (TypeRef p : ref.parents) {
                    p = p.getReal();
                    if (p.updateTypeClass(clz)) {
                        q.add(p);
                    }
                    if (ref.uses != null) {
                        if (p.addAllUses(ref.uses)) {
                            q.add(p);
                        }
                    }
                }
            }
            if (ref.children != null) {
                for (TypeRef p : ref.children) {
                    p = p.getReal();
                    if (p.updateTypeClass(clz)) {
                        q.add(p);
                    }

                    if (provideDesc != null) {
                        mergeTypeToSubRef(provideDesc, p, q);
                    }
                }
            }
            if (ref.sameValues != null) {
                for (TypeRef p : ref.sameValues) {
                    p = p.getReal();
                    if (p.updateTypeClass(clz)) {
                        q.add(p);
                    }
                }
            }
        }

        private boolean isAllParentSetted(TypeRef ref) {
            boolean allAreSet = true;
            for (TypeRef p : ref.parents) {
                if (p.getProvideDesc() == null) {
                    allAreSet = false;
                    break;
                }
            }
            return allAreSet;
        }

        private static String mergeObjectType(String a, String b) {
            if (a.equals(b)) {
                return a;
            }
            if ("L".endsWith(a)) {
                return b;
            } else if ("L".equals(b)) {
                return a;
            }
            if (a.compareTo(b) > 0) {
                return a;
            } else {
                return b;
            }
        }

        private static String mergeProviderType(String a, String b) {
            if (a.equals(b)) {
                return a;
            }
            TypeClass ta = TypeClass.clzOf(a);
            TypeClass tb = TypeClass.clzOf(b);
            if (ta.fixed && !tb.fixed) {
                return a;
            } else if (!ta.fixed && tb.fixed) {
                return b;
            } else if (ta.fixed && tb.fixed) {
                // special allow merge of Z and I
                if ((ta == TypeClass.INT && tb == TypeClass.BOOLEAN) || (tb == TypeClass.INT && ta == TypeClass.BOOLEAN)) {
                    return "I";
                }
                if (ta != tb) {
                    throw new RuntimeException();
                }
                if (ta == TypeClass.INT) {
                    for (int i = possibleIntTypes.length - 1; i >= 0; i--) {
                        String t = possibleIntTypes[i];
                        if (a.equals(t) || b.equals(t)) {
                            return t;
                        }
                    }
                    return "I";
                } else if (ta == TypeClass.OBJECT) {
                    // [[B + [[C = [Ljava/langObject;
                    int as = countArrayDim(a);
                    int bs = countArrayDim(b);
                    if (as == 0 || bs == 0) {
                        return mergeObjectType(a, b);
                    } else {
                        String elementTypeA = a.substring(as);
                        String elementTypeB = a.substring(bs);
                        if (as < bs) {
                            return buildArray(elementTypeB.charAt(0) == 'L' ? bs : bs - 1, "L");
                        } else if (bs > as) {
                            return buildArray(elementTypeA.charAt(0) == 'L' ? as : as - 1, "L");
                        } else { // as==bs
                            if (elementTypeA.charAt(0) != 'L' || elementTypeB.charAt(0) != 'L') {
                                return buildArray(as - 1, "L");
                            } else {
                                return buildArray(as, "L");
                            }
                        }
                    }
                } else {
                    throw new RuntimeException();
                }
            } else { // !ta.fixed && !tb.fixed
                return TypeClass.merge(ta, tb).name;
            }
        }

        private static String buildArray(int dim, String s) {
            if (dim == 0) {
                return s;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dim; i++) {
                sb.append('[');
            }
            sb.append(s);
            return sb.toString();
        }

        private static int countArrayDim(String a) {
            int i = 0;
            while (a.charAt(i) == '[') {
                i++;
            }
            return i;
        }

        private String mergeParentType(Set<TypeRef> parents) {
            Iterator<TypeRef> it = parents.iterator();
            String a = it.next().getProvideDesc();
            while (it.hasNext()) {
                a = mergeProviderType(a, it.next().getProvideDesc());
            }
            return a;
        }

        private void e0expr(E0Expr op, boolean getValue) {
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
                    String refType = refExpr.type;
                    if (refType == null && op.vt == VT.EXCEPTION_REF) {
                        refType = "Ljava/lang/Throwable;";
                    }
                    provideAs(refExpr, refType);
                    break;
                case STATIC_FIELD:
                    StaticFieldExpr fe = (StaticFieldExpr) op;
                    if (getValue) {// getfield
                        provideAs(fe, fe.type);
                    } else {// putfield
                        useAs(fe, fe.type);
                    }
                    break;
                case CONSTANT:
                    Constant cst = (Constant) op;
                    Object value = cst.value;
                    if (value instanceof String) {
                        provideAs(cst, "Ljava/lang/String;");
                    } else if (value instanceof DexType) {
                        provideAs(cst, "Ljava/lang/Class;");
                    } else if (value instanceof Number) {
                        if (value instanceof Integer || value instanceof Byte || value instanceof Short) {
                            int a = ((Number) value).intValue();
                            if (a == 0) {
                                provideAs(cst, TypeClass.ZIFL.name); // zero, false or, float
                            } else if (a == 1) {
                                provideAs(cst, TypeClass.ZIF.name);
                            } else {
                                provideAs(cst, TypeClass.IF.name);
                            }
                        } else if (value instanceof Long) {
                            provideAs(cst, "w");
                        } else if (value instanceof Float) {
                            provideAs(cst, "F");
                        } else if (value instanceof Double) {
                            provideAs(cst, "D");
                        }
                    } else if (value instanceof Character) {
                        provideAs(cst, "C");
                    } else {
                        provideAs(cst, "L");
                    }
                    break;
                default:
            }
        }

        private void e1expr(E1Expr e1, boolean getValue) {
            Value v = e1.op;
            switch (e1.vt) {
                case CAST:
                    CastExpr ce = (CastExpr) e1;
                    if (ce.to.equals("B")) { // special case for I2B
                        useAs(v, TypeClass.ZI.name);
                        provideAs(e1, TypeClass.ZI.name);
                    } else {
                        useAs(v, ce.from);
                        provideAs(e1, ce.to);
                    }
                    break;
                case FIELD:
                    FieldExpr fe = (FieldExpr) e1;
                    if (getValue) {// getfield
                        provideAs(fe, fe.type);
                    } else {// putfield
                        useAs(fe, fe.type);
                    }
                    if (v != null) {
                        useAs(v, fe.owner);
                    }
                    break;

                case CHECK_CAST: {
                    TypeExpr te = (TypeExpr) e1;
                    provideAs(te, te.type);
                    useAs(v, "L");
                }
                break;
                case INSTANCE_OF: {
                    TypeExpr te = (TypeExpr) e1;
                    provideAs(te, "Z");
                    useAs(v, "L");
                }
                break;
                case NEW_ARRAY: {
                    TypeExpr te = (TypeExpr) e1;
                    provideAs(te, "[" + te.type);
                    useAs(v, "I");
                }
                break;
                case LENGTH: {
                    UnopExpr ue = (UnopExpr) e1;
                    provideAs(ue, "I");
                    useAs(v, "[?");
                }
                break;
                case NEG:
                case NOT: {
                    UnopExpr ue = (UnopExpr) e1;
                    provideAs(ue, ue.type);
                    useAs(v, ue.type);
                }
                break;
                default:
            }
            if (v != null) {
                exExpr(v);
            }
        }

        private void e2expr(E2Expr e2, boolean getValue) {
            Value a = e2.op1.trim();
            Value b = e2.op2.trim();
            switch (e2.vt) {
                case ARRAY:
                    useAs(b, "I");
                    String elementType = ((ArrayExpr) e2).elementType;
                    // TypeClass ts = TypeClass.clzOf(elementType);
                    useAs(a, "[" + elementType);
                    if (getValue) {
                        provideAs(e2, elementType);

                        linkGetArray(a, e2);
                    } else {
                        useAs(e2, elementType);

                        linkSetArray(a, e2);
                    }
                    break;
                case LCMP:
                case FCMPG:
                case FCMPL:
                case DCMPG:
                case DCMPL: {
                    BinopExpr be = (BinopExpr) e2;
                    useAs(a, be.type);
                    useAs(b, be.type);
                    provideAs(e2, "I");
                }
                break;
                case EQ:
                case NE: {
                    useAs(e2.getOp2(), TypeClass.ZIL.name);
                    useAs(e2.getOp1(), TypeClass.ZIL.name);
                    linkSameAs(e2.getOp1(), e2.getOp2());
                    provideAs(e2, "Z");
                }
                break;
                case GE:
                case GT:
                case LE:
                case LT: {
                    BinopExpr be = (BinopExpr) e2;
                    useAs(a, be.type);
                    useAs(b, be.type);
                    provideAs(e2, "Z");
                }
                break;
                case ADD:
                case SUB:
                case IDIV:
                case LDIV:
                case FDIV:
                case DDIV:
                case MUL:
                case REM: {
                    BinopExpr be = (BinopExpr) e2;
                    useAs(a, be.type);
                    useAs(b, be.type);
                    provideAs(e2, be.type);
                }
                break;
                case OR:
                case AND:
                case XOR: {
                    BinopExpr be = (BinopExpr) e2;
                    useAs(a, be.type);
                    useAs(b, be.type);
                    // linkSameAs(a, b);
                    if ("J".equals(be.type) || "w".equals(be.type)) {
                        provideAs(e2, be.type);
                    } else {
                        provideAs(e2, TypeClass.ZI.name);
                    }
                }
                break;
                case SHL:
                case SHR:
                case USHR: {
                    BinopExpr be = (BinopExpr) e2;
                    useAs(a, be.type);
                    useAs(b, "I");
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

        private void linkSameAs(Value a, Value b) {
            TypeRef aa = getDefTypeRef(a);
            TypeRef bb = getDefTypeRef(b);
            if (aa.sameValues == null) {
                aa.sameValues = new HashSet<>(3);
            }
            if (bb.sameValues == null) {
                bb.sameValues = new HashSet<>(3);
            }
            aa.sameValues.add(bb);
            bb.sameValues.add(aa);
        }

        private void enexpr(EnExpr enExpr) {
            Value vbs[] = enExpr.ops;
            switch (enExpr.vt) {
                case INVOKE_NEW:
                case INVOKE_INTERFACE:
                case INVOKE_SPECIAL:
                case INVOKE_STATIC:
                case INVOKE_VIRTUAL:
                case INVOKE_POLYMORPHIC:
                case INVOKE_CUSTOM: {
                    AbstractInvokeExpr ice = (AbstractInvokeExpr) enExpr;
                    String type = ice.getProto().getReturnType();
                    provideAs(enExpr, type);
                    useAs(enExpr, type); // no one else will use it

                    String argTypes[] = ice.getProto().getParameterTypes();
                    if (argTypes.length == vbs.length) {
                        for (int i = 0; i < vbs.length; i++) {
                            useAs(vbs[i], argTypes[i]);
                        }
                    } else if (argTypes.length + 1 == vbs.length) {
                        useAs(vbs[0], "L");
                        for (int i = 1; i < vbs.length; i++) {
                            useAs(vbs[i], argTypes[i - 1]);
                        }
                    } else {
                        throw new RuntimeException();
                    }
                }
                break;

                case FILLED_ARRAY:
                    FilledArrayExpr fae = (FilledArrayExpr) enExpr;
                    for (Value vb : vbs) {
                        useAs(vb, fae.type);
                    }
                    provideAs(fae, "[" + fae.type);
                    break;
                case NEW_MUTI_ARRAY:
                    NewMutiArrayExpr nmae = (NewMutiArrayExpr) enExpr;
                    for (Value vb : vbs) {
                        useAs(vb, "I");
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < nmae.dimension; i++) {
                        sb.append('[');
                    }
                    sb.append(nmae.baseType);
                    provideAs(nmae, sb.toString());
                    break;
                case PHI:
                    for (Value vb : vbs) {
                        linkFromTo(vb, enExpr);
                    }
                    break;
                default:
            }
            for (Value vb : enExpr.ops) {
                exExpr(vb);
            }
        }

        private void exExpr(Value op) {
            exExpr(op, true);
        }

        private void exExpr(Value op, boolean getValue) {

            switch (op.et) {
                case E0:
                    e0expr((E0Expr) op, getValue);
                    break;
                case E1:
                    e1expr((E1Expr) op, getValue);
                    break;
                case E2:
                    e2expr((E2Expr) op, getValue);
                    break;
                case En:
                    enexpr((EnExpr) op);
                    break;
            }
        }

        private TypeRef getDefTypeRef(Value v) {
            Object object = v.tag;
            TypeRef typeRef;
            if (object == null || !(object instanceof TypeRef)) {
                typeRef = new TypeRef(v);
                refs.add(typeRef);
                v.tag = typeRef;
            } else {
                typeRef = (TypeRef) object;
            }
            return typeRef;
        }

        private void linkGetArray(Value array, Value v) {
            TypeRef root = getDefTypeRef(array);
            TypeRef value = getDefTypeRef(v);
            if (root.gArrayValues == null) {
                root.gArrayValues = new HashSet<>(3);
            }
            root.gArrayValues.add(value);
            if (value.arrayRoots == null) {
                value.arrayRoots = new HashSet<>(3);
            }
            value.arrayRoots.add(root);
        }

        private void linkSetArray(Value array, Value v) {
            TypeRef root = getDefTypeRef(array);
            TypeRef value = getDefTypeRef(v);
            if (root.sArrayValues == null) {
                root.sArrayValues = new HashSet<>(3);
            }
            root.sArrayValues.add(value);
            if (value.arrayRoots == null) {
                value.arrayRoots = new HashSet<>(3);
            }
            value.arrayRoots.add(root);
        }

        private void linkFromTo(Value from, Value to) {
            TypeRef tFrom = getDefTypeRef(from);
            TypeRef tTo = getDefTypeRef(to);
            if (tFrom.children == null) {
                tFrom.children = new HashSet<>();
            }
            tFrom.children.add(tTo);
            if (tTo.parents == null) {
                tTo.parents = new HashSet<>();
            }
            tTo.parents.add(tFrom);
        }

        private void provideAs(Value op, String type) {
            TypeRef typeRef = getDefTypeRef(op).getReal();
            typeRef.provideDesc = (type);
            typeRef.updateTypeClass(TypeClass.clzOf(type));
        }

        private void s1stmt(E1Stmt s) {
            if (s.st == ST.GOTO) {
                return;
            }
            Value op = s.op;
            switch (s.st) {
                case LOOKUP_SWITCH:
                case TABLE_SWITCH:
                    useAs(op, "I");
                    break;
                case GOTO:
                    break;
                case IF:
                    useAs(op, "Z");
                    break;
                case LOCK:
                case UNLOCK:
                    useAs(op, "L");
                    break;
                case THROW:
                    useAs(op, "Ljava/lang/Throwable;");
                    break;
                case RETURN:
                    useAs(op, method.ret);
                    break;
                default:
            }
            exExpr(op);
        }

        private void s2stmt(E2Stmt s) {
            if (s.st == ST.FILL_ARRAY_DATA) {
                linkFromTo(s.op1, s.op2);
            } else {
                Value from = s.op2;
                Value to = s.op1;
                linkFromTo(from, to);
                exExpr(from);
                exExpr(to, false);
            }
        }

        private void sxStmt() {
            for (Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
                switch (p.et) {
                    case E0:
                        // label, nop and return-void
                        if (p.st == ST.LABEL) {
                            LabelStmt labelStmt = (LabelStmt) p;
                            if (labelStmt.phis != null) {
                                for (AssignStmt phi : labelStmt.phis) {
                                    s2stmt(phi);
                                }
                            }
                        }
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
            for (TypeRef ref : refs) {
                sb.append(ref).append("\n");
            }
            return sb.toString();
        }

        private void useAs(Value op, String type) {
            TypeRef typeRef = getDefTypeRef(op);
            typeRef.addUses(type);
            typeRef.updateTypeClass(TypeClass.clzOf(type));
        }
    }
}
