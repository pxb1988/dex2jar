package com.googlecode.dex2jar.ir.ts.array;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Cfg;
import com.googlecode.dex2jar.ir.ts.StatedTransformer;
import com.googlecode.dex2jar.ir.ts.UniqueQueue;

import java.lang.reflect.Array;
import java.util.*;

/**
 * require SSA, usually run after ConstTransformer 1. array is fixed size. 2. array object init and use once, (exclude
 * the element assignment) 3. all elements are init with fixed index before use. 4. the array is not in PhiExpr 5. and
 * for array object init at A, use at B; A and B must in same loop/path, so G(A->B), G(A->C->B), G(A->C,A->D,C->B,D->B),
 * G(A->C,C->D,D->C,C->B) and G(A->C,C->A,C->B) is ok to transform, but for G(A->C,C->B,B->D,D->C), B is in a loop
 * (B->D->C->B), should not transformed.
 * <p/>
 * transform
 * 
 * <pre>
 *     a=new String[3]
 *     a[0]="123"
 *     a[2]="1234"
 *     a[1]="12345"
 *     return a
 * </pre>
 * 
 * to
 * 
 * <pre>
 *     return new String[3] { "123", "12345", "1234" }
 * </pre>
 * 
 * 1. This Transformer is useful when cleanup the tool-injected reflection code
 * 
 * <pre>
 *     // before transform
 *     ...
 *     Class a[]=new Class[2]
 *     a[0]=String.class
 *     a[1]=int.class
 *     Method m=x.getMethod("methodA",a)
 *     Object b[]=new Object[2]
 *     b[0]="123";
 *     b[1]=Integer.valueOf(1);
 *     m.invoke(c,b)
 *     // after transform
 *     Method m=x.getMethod("methodA", new Class[2] { String.class ,int.class });
 *     m.invoke(b,new Object[]{"123",Integer.valueOf(1)})
 * </pre>
 * 
 * 2. Suggest decompilers generate better code
 * 
 * <pre>
 *     // for following code, before transform, the decompiler generate same source
 *     Object[]a=new Object[2];
 *     a[0]=b;
 *     a[1]=c
 *     String.format("b is %s, c is %s",a)
 *     // after transform, then decompile generate the following source
 *     String.format("b is %s, c is %s",b,c)
 * </pre>
 * 
 * FIXME also handle not full filled array
 * 
 * <pre>
 * int a[] = new int[5];
 * // a[0]=0;
 * a[1] = 1;
 * a[2] = 3;
 * a[3] = 4;
 * a[4] = 7;
 * </pre>
 */
public class FillArrayTransformer extends StatedTransformer {
    private static class ArrayObject {
        int size;
        String type;
        AssignStmt init;
        List<Stmt> putItem = new ArrayList<>();
        List<Stmt> used = new ArrayList<>();

        private ArrayObject(int size, String type, AssignStmt init) {
            this.size = size;
            this.type = type;
            this.init = init;
        }
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
        new FillArrayTransformer().transform(m);
        System.out.println(m);
    }

    @Override
    public boolean transformReportChanged(IrMethod method) {

        // find array match fixed size,fixed index, not in phi
        final Map<Local, ArrayObject> arraySizes = searchForArrayObject(method);

        if (arraySizes.size() == 0) {
            return false;
        }

        makeSureAllElementAreAssigned(arraySizes);
        if (arraySizes.size() == 0) {
            return false;
        }

        makeSureArrayUsedAfterAllElementAssigned(method, arraySizes);

        if (arraySizes.size() == 0) {
            return false;
        }

        replace(method, arraySizes);

        return true;
    }

    private void replace(IrMethod method, Map<Local, ArrayObject> arraySizes) {
        final List<FilledArrayExpr> filledArrayExprs = new ArrayList<>();
        for (Map.Entry<Local, ArrayObject> e : arraySizes.entrySet()) {
            final Local local0 = e.getKey();
            final ArrayObject ao = e.getValue();
            final Value t[] = new Value[ao.size];
            for (Iterator<Stmt> it = ao.putItem.iterator(); it.hasNext();) {
                Stmt p = it.next();
                if (p.st == Stmt.ST.FILL_ARRAY_DATA) {
                    Local local = (Local) p.getOp1();
                    if (local == local0) {
                        Object vs = ((Constant) p.getOp2()).value;
                        int endPos = Array.getLength(vs);
                        for (int j = 0; j < endPos; j++) {
                            t[j] = Exprs.nConstant(Array.get(vs, j));
                        }
                    }
                } else { // ASSIGN
                    ArrayExpr ae = (ArrayExpr) p.getOp1();
                    Local local = (Local) ae.getOp1();
                    if (local == local0) {
                        int idx = ((Number) ((Constant) ae.getOp2()).value).intValue();
                        Value op2 = p.getOp2();
                        if (op2.vt != Value.VT.LOCAL && op2.vt != Value.VT.CONSTANT) {
                            Local n = new Local(-1);
                            method.locals.add(n);
                            method.stmts.insertBefore(p, Stmts.nAssign(n, op2));
                            op2 = n;
                        }
                        t[idx] = op2;
                    }
                }
            }

            // for code
            // b=new Object[1]
            // b[0]=null
            // a =new Object[1]
            // a =b;
            // use(a)
            // if a is replace before b, the code
            // b=new Object[1]
            // b[0]=null
            // use(new Object[]{b})
            // the used stmt of b is outdated, so we have to search pre replaced arrays

            method.locals.remove(local0);
            method.stmts.remove(ao.init);
            for (Stmt p : ao.putItem) {
                method.stmts.remove(p);
            }
            Cfg.TravelCallBack tcb = new Cfg.TravelCallBack() {
                @Override
                public Value onAssign(Local v, AssignStmt as) {
                    return v;
                }

                @Override
                public Value onUse(Local v) {
                    if (local0 == v) {
                        FilledArrayExpr fae = Exprs.nFilledArray(ao.type, t);
                        filledArrayExprs.add(fae);
                        return fae;
                    }
                    return v;
                }
            };

            if (ao.used.size() == 1) {
                Stmt stmt = ao.used.get(0);
                if (method.stmts.contains(stmt)) { // the stmt is not removed by pre array replacement
                    Cfg.travelMod(stmt, tcb, false);
                } else {
                    int size = filledArrayExprs.size();
                    for (int i = 0; i < size; i++) {
                        Cfg.travelMod(filledArrayExprs.get(i), tcb);
                    }
                }
            } else if (ao.used.size() == 0) {
                // the array is never used, ignore
            } else {
                throw new RuntimeException("array is used multiple times");
            }
        }
    }

    // FIXME poor performance
    private void makeSureArrayUsedAfterAllElementAssigned(IrMethod method, final Map<Local, ArrayObject> arraySizes) {

        for (Local local : method.locals) {
            local._ls_index = -1;
        }
        final int MAX = 50;
        if (arraySizes.size() < MAX) {
            makeSureArrayUsedAfterAllElementAssigned0(method, arraySizes);
        } else {

            // this method consumes too many memory, case 'java.lang.OutOfMemoryError: Java heap space', we have to cut
            // it
            Map<Local, ArrayObject> keptInAll = new HashMap<>();
            Map<Local, ArrayObject> keptInPart = new HashMap<>();
            List<Local> arrays = new ArrayList<>(MAX);

            Iterator<Map.Entry<Local, ArrayObject>> it = arraySizes.entrySet().iterator();
            while (it.hasNext()) {
                for (int i = 0; i < MAX && it.hasNext(); i++) {
                    Map.Entry<Local, ArrayObject> e = it.next();
                    keptInPart.put(e.getKey(), e.getValue());
                    it.remove();
                    arrays.add(e.getKey());
                }
                makeSureArrayUsedAfterAllElementAssigned0(method, keptInPart);
                for (Local local : arrays) {
                    local._ls_index = -1;
                }
                arrays.clear();
                keptInAll.putAll(keptInPart);
                keptInPart.clear();
            }
            arraySizes.putAll(keptInAll);
        }

        Cfg.reIndexLocal(method);

    }

    private void makeSureArrayUsedAfterAllElementAssigned0(IrMethod method, final Map<Local, ArrayObject> arraySizes) {
        int i = 0;
        for (Local local : arraySizes.keySet()) {
            local._ls_index = i++;
        }

        final int size = i;
        final List<ArrayObjectValue> values = new ArrayList<>();
        Cfg.dfs(method.stmts, new Cfg.FrameVisitor<ArrayObjectValue[]>() {

            @Override
            public ArrayObjectValue[] merge(ArrayObjectValue[] srcFrame, ArrayObjectValue[] distFrame, Stmt src,
                    Stmt dist) {
                if (distFrame == null) {
                    distFrame = new ArrayObjectValue[size];
                    for (int i = 0; i < size; i++) {
                        ArrayObjectValue arc = srcFrame[i];
                        if (arc != null) {
                            ArrayObjectValue aov = new ArrayObjectValue(arc.local);
                            values.add(aov);
                            aov.array = arc.array;
                            aov.parent = arc;
                            aov.pos = (BitSet) arc.pos.clone();
                            distFrame[i] = aov;
                        }
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        ArrayObjectValue arc = srcFrame[i];
                        ArrayObjectValue aov = distFrame[i];
                        if (arc != null && aov != null) {
                            if (aov.otherParent == null) {
                                aov.otherParent = new HashSet<>();
                            }
                            aov.otherParent.add(arc);
                        }
                    }
                }
                return distFrame;
            }

            @Override
            public ArrayObjectValue[] initFirstFrame(Stmt first) {
                return new ArrayObjectValue[size];
            }

            ArrayObjectValue tmp[] = initFirstFrame(null);
            Stmt currentStmt;

            @Override
            public ArrayObjectValue[] exec(ArrayObjectValue[] frame, Stmt stmt) {
                currentStmt = stmt;
                System.arraycopy(frame, 0, tmp, 0, size);
                if (stmt.st == Stmt.ST.FILL_ARRAY_DATA) {
                    if (stmt.getOp1().vt == Value.VT.LOCAL) {
                        Local local = (Local) stmt.getOp1();
                        if (local._ls_index >= 0) {
                            ArrayObjectValue av = tmp[local._ls_index];
                            Constant cst = (Constant) stmt.getOp2();
                            int endPos = Array.getLength(cst.value);
                            av.pos.set(0, endPos);
                        }
                    } else {
                        use(stmt.getOp1());
                    }
                } else if (stmt.st == Stmt.ST.ASSIGN && stmt.getOp1().vt == Value.VT.ARRAY) {
                    use(stmt.getOp2());
                    ArrayExpr ae = (ArrayExpr) stmt.getOp1();
                    if (ae.getOp1().vt == Value.VT.LOCAL) {
                        Local local = (Local) ae.getOp1();
                        if (local._ls_index >= 0) {
                            int index = ((Number) ((Constant) ae.getOp2()).value).intValue();
                            ArrayObjectValue av = tmp[local._ls_index];
                            av.pos.set(index);
                        } else {
                            use(ae);
                        }
                    } else {
                        use(ae);
                    }
                } else if (stmt.st == Stmt.ST.ASSIGN && stmt.getOp1().vt == Value.VT.LOCAL) {
                    Local local = (Local) stmt.getOp1();
                    use(stmt.getOp2());

                    if (local._ls_index >= 0) {
                        ArrayObjectValue aov = new ArrayObjectValue(local);
                        aov.array = arraySizes.get(local);
                        aov.pos = new BitSet();
                        values.add(aov);
                        tmp[local._ls_index] = aov;
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
                    if (v.vt == Value.VT.LOCAL) {
                        Local local = (Local) v;
                        if (local._ls_index >= 0) {
                            ArrayObjectValue aov = tmp[local._ls_index];
                            aov.array.used.add(currentStmt);
                            aov.used = true;
                        }
                    }
                    break;
                case E1:
                    use(v.getOp());
                    break;
                case E2:
                    use(v.getOp1());
                    use(v.getOp2());
                    break;
                case En:
                    for (Value op : v.getOps()) {
                        use(op);
                    }
                    break;
                }
            }
        });

        Set<ArrayObjectValue> used = markUsed(values);

        // check if ArrayObjectValue have different parent assignment
        for (ArrayObjectValue avo : used) {
            if (avo.array.used.size() > 1) {
                arraySizes.remove(avo.local);
            } else {
                if (avo.parent != null && avo.otherParent != null) {
                    // BitSet bs = avo.pos;
                    BitSet p = avo.parent.pos;
                    for (ArrayObjectValue ps : avo.otherParent) {
                        if (!p.equals(ps.pos)) {
                            arraySizes.remove(avo.local);
                            break;
                        }
                    }
                }
            }
        }
        // check for un full init array
        for (Iterator<Map.Entry<Local, ArrayObject>> it = arraySizes.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Local, ArrayObject> e = it.next();
            Local local = e.getKey();
            ArrayObject arrayObject = e.getValue();
            for (Stmt use : arrayObject.used) {
                ArrayObjectValue frame[] = (ArrayObjectValue[]) use.frame;
                ArrayObjectValue aov = frame[local._ls_index];
                BitSet pos = aov.pos;
                if (pos.nextClearBit(0) < arrayObject.size || pos.nextSetBit(arrayObject.size) >= 0) {
                    it.remove();
                    break;
                }
            }
        }

        // clean up
        for (Stmt stmt : method.stmts) {
            stmt.frame = null;
        }
    }

    protected Set<ArrayObjectValue> markUsed(Collection<ArrayObjectValue> values) {
        Set<ArrayObjectValue> used = new HashSet<>(values.size() / 2);
        Queue<ArrayObjectValue> q = new UniqueQueue<>();
        q.addAll(values);
        values.clear();
        while (!q.isEmpty()) {
            ArrayObjectValue v = q.poll();
            if (v.used) {
                if (used.contains(v)) {
                    continue;
                }
                used.add(v);
                {
                    ArrayObjectValue p = v.parent;
                    if (p != null) {
                        if (!p.used) {
                            p.used = true;
                            q.add(p);
                        }
                    }
                }
                if (v.otherParent != null) {
                    for (ArrayObjectValue p : v.otherParent) {
                        if (!p.used) {
                            p.used = true;
                            q.add(p);
                        }
                    }
                }
            }
        }

        return used;
    }

    private void makeSureAllElementAreAssigned(Map<Local, ArrayObject> arraySizes) {
        BitSet pos = new BitSet();
        for (Iterator<Map.Entry<Local, ArrayObject>> it = arraySizes.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Local, ArrayObject> e = it.next();
            ArrayObject arrayObject = e.getValue();
            boolean needRemove = false;
            for (Stmt p : arrayObject.putItem) {
                if (p.st == Stmt.ST.FILL_ARRAY_DATA) {
                    int endPos = Array.getLength(((Constant) p.getOp2()).value);
                    int next = pos.nextSetBit(0);
                    if (next < 0 || next >= endPos) {// not set in range
                        pos.set(0, endPos);
                    } else {// setted in range
                        needRemove = true;
                        break;
                    }
                } else { // ASSIGN
                    ArrayExpr ae = (ArrayExpr) p.getOp1();
                    int idx = ((Number) ((Constant) ae.getOp2()).value).intValue();
                    if (!pos.get(idx)) {
                        pos.set(idx);
                    } else {
                        needRemove = true;
                        break;
                    }
                }
            }
            if (needRemove || pos.nextClearBit(0) < arrayObject.size || pos.nextSetBit(arrayObject.size) >= 0) {
                it.remove();
            }
            pos.clear();
        }
    }

    private Map<Local, ArrayObject> searchForArrayObject(IrMethod method) {

        final Map<Local, ArrayObject> arraySizes = new HashMap<>();
        if (method.locals.size() == 0) {
            return arraySizes;
        }
        Cfg.createCFG(method);
        Cfg.dfsVisit(method, new Cfg.DfsVisitor() {
            @Override
            public void onVisit(Stmt p) {
                if (p.st == Stmt.ST.ASSIGN) {
                    if (p.getOp2().vt == Value.VT.NEW_ARRAY && p.getOp1().vt == Value.VT.LOCAL) {
                        TypeExpr ae = (TypeExpr) p.getOp2();
                        if (ae.getOp().vt == Value.VT.CONSTANT) {
                            int size = ((Number) ((Constant) ae.getOp()).value).intValue();
                            arraySizes.put((Local) p.getOp1(), new ArrayObject(size, ae.type, (AssignStmt) p));
                        }
                    } else if (p.getOp1().vt == Value.VT.ARRAY) {
                        ArrayExpr ae = (ArrayExpr) p.getOp1();
                        if (ae.getOp1().vt == Value.VT.LOCAL) {
                            Local local = (Local) ae.getOp1();
                            ArrayObject arrayObject = arraySizes.get(local);
                            if (arrayObject != null) {
                                if (ae.getOp2().vt == Value.VT.CONSTANT) {
                                    arrayObject.putItem.add(p);
                                } else {
                                    arraySizes.remove(local);
                                }
                            }
                        }
                    }
                } else if (p.st == Stmt.ST.FILL_ARRAY_DATA) {
                    if (p.getOp1().vt == Value.VT.LOCAL) {
                        Local local = (Local) p.getOp1();
                        ArrayObject arrayObject = arraySizes.get(local);
                        if (arrayObject != null) {
                            arrayObject.putItem.add(p);
                        }
                    }
                }
            }
        });
        if (arraySizes.size() > 0) {
            Set<Local> set = new HashSet<Local>();
            if (method.phiLabels != null) {
                for (LabelStmt labelStmt : method.phiLabels) {
                    if (labelStmt.phis != null) {
                        for (AssignStmt as : labelStmt.phis) {
                            set.add((Local) as.getOp1());
                            for (Value v : as.getOp2().getOps()) {
                                set.add((Local) v);
                            }
                        }
                    }
                }
            }
            if (set.size() > 0) {
                for (Local local : set) {
                    arraySizes.remove(local);
                }
            }
        }
        return arraySizes;
    }

    static class ArrayObjectValue {
        BitSet pos;
        Local local;
        ArrayObject array;
        ArrayObjectValue parent;
        Set<ArrayObjectValue> otherParent;
        boolean used;

        public ArrayObjectValue(Local local) {
            this.local = local;
        }
    }
}
