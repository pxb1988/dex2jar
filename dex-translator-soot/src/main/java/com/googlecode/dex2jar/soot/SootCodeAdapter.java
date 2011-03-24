/*
 *  dex2jar - A tool for converting Android's .dex format to Java's .class format
 *  Copyright (c) 2009-2011 Panxiaobo
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package com.googlecode.dex2jar.soot;

import static com.googlecode.dex2jar.soot.SootUtil.toSootType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.BodyTransformer;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FastHierarchy;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.UnknownType;
import soot.Value;
import soot.baf.Baf;
import soot.baf.BafBody;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.EqExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NeExpr;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JInterfaceInvokeExpr;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.internal.JThrowStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.toolkits.base.Aggregator;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.LocalNameStandardizer;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.typing.TypeAssigner;
import soot.toolkits.scalar.LocalPacker;
import soot.toolkits.scalar.LocalSplitter;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.util.Chain;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class SootCodeAdapter implements DexCodeVisitor, Opcodes, DexOpcodes {

    static {

        Scene.v().setFastHierarchy(new FastHierarchy() {

            @Override
            protected boolean canStoreClass(SootClass child, SootClass parent) {
                return true;
            }

        });

    }

    static Jimple ji = Jimple.v();
    private static final Logger log = LoggerFactory.getLogger(SootCodeAdapter.class);
    static Scene s = Scene.v();

    static String x(String x) {
        return Type.getType(x).getClassName();
    }

    Map<Label, Unit> _label2Unit = new HashMap<Label, Unit>();
    JimpleBody body;

    Map<Integer, Local> locals = new HashMap<Integer, Local>();

    Method method;

    Chain<Unit> units;

    /**
     * <pre>
     * 
     * 
     * </pre>
     * 
     * @param method
     * @param sootMethod
     */
    public SootCodeAdapter(Method method, SootMethod sootMethod) {
        this.method = method;
        this.body = ji.newBody();

        this.body.setMethod(sootMethod);
        units = body.getUnits();
        sootMethod.setActiveBody(body);
    }

    Unit label2Unit(Label label) {
        Unit unit = _label2Unit.get(label);
        if (unit == null) {
            unit = ji.newNopStmt();
            _label2Unit.put(label, unit);
        }
        return unit;
    }

    private Local safeGetLocal(int reg) {
        Local local = locals.get(reg);
        if (local == null) {
            local = ji.newLocal("R" + reg, UnknownType.v());
            locals.put(reg, local);
            body.getLocals().add(local);
        }
        return local;
    }

    @Override
    public void visitArguments(int total, int[] args) {
        int i = 0;
        if (!Modifier.isStatic(body.getMethod().getModifiers())) {
            Local arg = ji.newLocal("R" + args[i] + "_this", SootUtil.toSootType(method.getOwner()));
            body.getLocals().add(arg);
            locals.put(args[i], arg);
            units.add(ji.newIdentityStmt(arg, ji.newThisRef((RefType) arg.getType())));
            i++;
        }
        int j = 0;
        for (; i < args.length; i++) {
            soot.Type type = (soot.Type) body.getMethod().getParameterTypes().get(j);
            Local arg = ji.newLocal("R" + args[i], type);
            body.getLocals().add(arg);
            locals.put(args[i], arg);
            units.add(ji.newIdentityStmt(arg, ji.newParameterRef(type, j++)));
        }

    }

    @Override
    public void visitArrayStmt(int opcode, int formOrToReg, int arrayReg, int indexReg) {
        switch (opcode) {
        case OP_APUT:
            units.add(ji.newAssignStmt(ji.newArrayRef(safeGetLocal(arrayReg), safeGetLocal(indexReg)),
                    safeGetLocal(formOrToReg)));
            break;
        case OP_AGET:
            units.add(ji.newAssignStmt(safeGetLocal(formOrToReg),
                    ji.newArrayRef(safeGetLocal(arrayReg), safeGetLocal(indexReg))));
            break;
        }
    }

    @Override
    public void visitBinopLitXStmt(int opcode, int aA, int bB, int cC) {
        switch (opcode) {
        case OP_ADD_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newAddExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_RSUB_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newSubExpr(IntConstant.v(cC), safeGetLocal(bB))));
            break;
        case OP_MUL_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newMulExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_DIV_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newDivExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_REM_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newRemExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_AND_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newAndExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_OR_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newOrExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_XOR_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newXorExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_SHL_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newShlExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_SHR_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newShrExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;
        case OP_USHR_INT_LIT_X:
            units.add(ji.newAssignStmt(safeGetLocal(aA), ji.newUshrExpr(safeGetLocal(bB), IntConstant.v(cC))));
            break;

        }
    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2) {
        switch (opcode) {
        case OP_ADD_INT:
        case OP_ADD_LONG:
        case OP_ADD_FLOAT:
        case OP_ADD_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newAddExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_SUB_INT:
        case OP_SUB_LONG:
        case OP_SUB_FLOAT:
        case OP_SUB_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newSubExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_MUL_INT:
        case OP_MUL_LONG:
        case OP_MUL_FLOAT:
        case OP_MUL_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newMulExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_DIV_INT:
        case OP_DIV_LONG:
        case OP_DIV_FLOAT:
        case OP_DIV_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newDivExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_REM_INT:
        case OP_REM_LONG:
        case OP_REM_FLOAT:
        case OP_REM_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newRemExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_AND_INT:
        case OP_AND_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newAndExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_OR_INT:
        case OP_OR_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newOrExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_XOR_INT:
        case OP_XOR_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newXorExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_SHL_INT:
        case OP_SHL_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newShlExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_SHR_INT:
        case OP_SHR_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newShrExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        case OP_USHR_INT:
        case OP_USHR_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newUshrExpr(safeGetLocal(r1), safeGetLocal(r2))));
            break;
        }

    }

    @Override
    public void visitClassStmt(int opcode, int a, int b, String type) {
        switch (opcode) {
        case OP_INSTANCE_OF:
            units.add(ji.newAssignStmt(safeGetLocal(a),
                    ji.newInstanceOfExpr(safeGetLocal(b), SootUtil.toSootType(type))));
            break;
        case OP_NEW_ARRAY:
            units.add(ji.newAssignStmt(safeGetLocal(a),
                    ji.newNewArrayExpr(SootUtil.toSootType(Type.getType(type).getElementType()), safeGetLocal(b))));
            break;
        }
    }

    @Override
    public void visitClassStmt(int opcode, int saveTo, String type) {
        switch (opcode) {
        case OP_NEW_INSTANCE:
            units.add(ji.newAssignStmt(safeGetLocal(saveTo), ji.newNewExpr((RefType) SootUtil.toSootType(type))));
            break;
        case OP_CHECK_CAST:
            units.add(ji.newAssignStmt(safeGetLocal(saveTo),
                    ji.newCastExpr(safeGetLocal(saveTo), SootUtil.toSootType(type))));
            break;
        }
    }

    @Override
    public void visitCmpStmt(int opcode, int saveTo, int bB, int cC) {
        switch (opcode) {
        case OP_CMPL_FLOAT:
        case OP_CMPL_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(saveTo), ji.newCmplExpr(safeGetLocal(bB), safeGetLocal(cC))));
            break;

        case OP_CMPG_FLOAT:
        case OP_CMPG_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(saveTo), ji.newCmpgExpr(safeGetLocal(bB), safeGetLocal(cC))));
            break;
        case OP_CMP_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(saveTo), ji.newCmpExpr(safeGetLocal(bB), safeGetLocal(cC))));
            break;
        }
    }

    @Override
    public void visitConstStmt(int opcode, int toReg, Object value) {
        switch (opcode) {
        case OP_CONST:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), IntConstant.v((Integer) value)));
            break;
        case OP_CONST_WIDE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), LongConstant.v((Long) value)));
            break;
        case OP_CONST_STRING:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), StringConstant.v((String) value)));
            break;
        case OP_CONST_CLASS:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ClassConstant.v(SootUtil.toJavaClassName((String) value))));
            break;
        }

    }

    static BodyTransformer jb_dex_goto = new BodyTransformer() {

        @Override
        protected void internalTransform(Body b, String phaseName, Map options) {

            Chain<Unit> units = b.getUnits();
            Iterator<Unit> stmtIt = units.snapshotIterator();

            while (stmtIt.hasNext()) {
                Stmt u = (Stmt) stmtIt.next();
                if (u instanceof JGotoStmt) {
                    JGotoStmt jGotoStmt = (JGotoStmt) u;
                    Unit target = jGotoStmt.getTarget();
                    if ((target instanceof JReturnStmt) || (target instanceof JReturnVoidStmt)
                    // || (target instanceof JThrowStmt)
                    ) {
                        units.insertAfter((Unit) target.clone(), u);
                        units.remove(u);
                    }

                }
            }
        }
    };

    static BodyTransformer jb_tt = soot.toolkits.exceptions.TrapTightener.v();
    static BodyTransformer jb_ls = LocalSplitter.v();
    static BodyTransformer jb_a = Aggregator.v();
    static BodyTransformer jb_ule = UnusedLocalEliminator.v();
    static BodyTransformer jb_tr = TypeAssigner.v();

    static BodyTransformer jb_dex_jump = new BodyTransformer() {

        @Override
        protected void internalTransform(Body b, String phaseName, Map options) {

            Chain<Unit> units = b.getUnits();
            Iterator<Unit> stmtIt = units.snapshotIterator();

            while (stmtIt.hasNext()) {
                Stmt u = (Stmt) stmtIt.next();
                if (u instanceof IfStmt) {
                    Value v = ((IfStmt) u).getCondition();
                    if (v instanceof NeExpr) {
                        NeExpr ne = (NeExpr) v;
                        if ((ne.getOp1().getType() instanceof RefType) && (ne.getOp2() instanceof IntConstant)) {
                            ne.setOp2(NullConstant.v());
                        }
                        if ((ne.getOp2().getType() instanceof RefType) && (ne.getOp1() instanceof IntConstant)) {
                            ne.setOp1(NullConstant.v());
                        }
                    } else if (v instanceof EqExpr) {
                        EqExpr ne = (EqExpr) v;
                        if ((ne.getOp1().getType() instanceof RefType) && (ne.getOp2() instanceof IntConstant)) {
                            ne.setOp2(NullConstant.v());
                        }
                        if ((ne.getOp2().getType() instanceof RefType) && (ne.getOp1() instanceof IntConstant)) {
                            ne.setOp1(NullConstant.v());
                        }
                    }
                } else if (u instanceof AssignStmt) {
                    AssignStmt as = (AssignStmt) u;
                    if ((as.getLeftOp().getType() instanceof RefType) && (as.getRightOp() instanceof IntConstant)) {
                        IntConstant intConstant = (IntConstant) as.getRightOp();
                        if (intConstant.value == 0) {
                            as.setRightOp(NullConstant.v());
                        } else {
                            // FIXME
                            // throw new RuntimeException();
                        }
                    }
                }
            }
        }
    };

    static BodyTransformer jb_ulp = LocalPacker.v();
    static BodyTransformer jb_lns = LocalNameStandardizer.v();
    static BodyTransformer jb_cp = CopyPropagator.v();
    static BodyTransformer jb_dae = DeadAssignmentEliminator.v();
    static BodyTransformer jb_cp_ule = UnusedLocalEliminator.v();
    static BodyTransformer jb_lp = LocalPacker.v();
    static BodyTransformer jb_ne = NopEliminator.v();
    static BodyTransformer jb_uce = UnreachableCodeEliminator.v();

    @Override
    public void visitEnd() {

        // PackManager.v().getPack("jb").apply(body);

        if (method.getName().equals("run")) {
            System.out.println("");
        }
        try {
            for (BodyTransformer t : new BodyTransformer[] { jb_ne, jb_dex_goto, jb_tt, jb_ls, jb_a, jb_ule, jb_tr,
                    // new TypeA(),
                    jb_dex_jump, jb_ulp, jb_lns, jb_cp, jb_dae, jb_cp_ule, jb_lp, jb_ne }) {
                t.transform(body);
            }
        } catch (Exception e) {
            throw new DexException(e, "Error transform body");
        }
        // PackManager.v().getPack("jop").apply(body);
        BafBody bd = Baf.v().newBody(body);
        PackManager.v().getPack("bop").apply(bd);

        body.getMethod().setActiveBody(bd);

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field) {
        switch (opcode) {
        case OP_SPUT:
            units.add(ji.newAssignStmt(ji.newStaticFieldRef(s.makeFieldRef(s.getSootClass(x(field.getOwner())),
                    field.getName(), toSootType(field.getType()), true)), safeGetLocal(fromOrToReg)));
            break;
        case OP_SGET:
            units.add(ji.newAssignStmt(safeGetLocal(fromOrToReg), ji.newStaticFieldRef(s.makeFieldRef(
                    s.getSootClass(x(field.getOwner())), field.getName(), toSootType(field.getType()), true))));
            break;
        }
    }

    @Override
    public void visitFieldStmt(int opcode, int regFromOrTo, int ownerReg, Field field) {
        switch (opcode) {
        case OP_IGET:
            units.add(ji.newAssignStmt(
                    safeGetLocal(regFromOrTo),
                    ji.newInstanceFieldRef(
                            safeGetLocal(ownerReg),
                            s.makeFieldRef(s.getSootClass(x(field.getOwner())), field.getName(),
                                    toSootType(field.getType()), false))));
            break;
        case OP_IPUT:
            units.add(ji.newAssignStmt(
                    ji.newInstanceFieldRef(
                            safeGetLocal(ownerReg),
                            s.makeFieldRef(s.getSootClass(x(field.getOwner())), field.getName(),
                                    toSootType(field.getType()), false)), safeGetLocal(regFromOrTo)));
            break;
        }
    }

    @Override
    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {
        Value array = safeGetLocal(aA);

        switch (elemWidth) {
        case 1:
            units.add(ji.newAssignStmt(array, ji.newNewArrayExpr(ByteType.v(), IntConstant.v(initLength))));
            for (int j = 0; j < initLength; j++) {
                units.add(ji.newAssignStmt(ji.newArrayRef(array, IntConstant.v(j)), IntConstant.v((Byte) values[j])));
            }
            break;
        case 2:
            units.add(ji.newAssignStmt(array, ji.newNewArrayExpr(ShortType.v(), IntConstant.v(initLength))));
            for (int j = 0; j < initLength; j++) {
                units.add(ji.newAssignStmt(ji.newArrayRef(array, IntConstant.v(j)), IntConstant.v((Short) values[j])));
            }
            break;
        case 4:
            units.add(ji.newAssignStmt(array, ji.newNewArrayExpr(IntType.v(), IntConstant.v(initLength))));
            for (int j = 0; j < initLength; j++) {
                units.add(ji.newAssignStmt(ji.newArrayRef(array, IntConstant.v(j)), IntConstant.v((Integer) values[j])));
            }
            break;
        case 8:
            units.add(ji.newAssignStmt(array, ji.newNewArrayExpr(LongType.v(), IntConstant.v(initLength))));
            for (int j = 0; j < initLength; j++) {
                units.add(ji.newAssignStmt(ji.newArrayRef(array, IntConstant.v(j)), LongConstant.v((Long) values[j])));
            }
            break;
        }

    }

    @Override
    public void visitFilledNewArrayStmt(int opcode, int[] args, String type) {
        Value array = safeGetLocal(Integer.MAX_VALUE);
        units.add(ji.newAssignStmt(array, ji.newNewArrayExpr(SootUtil.toSootType(type), IntConstant.v(args.length))));
        for (int i = 0; i < args.length; i++) {
            units.add(ji.newAssignStmt(ji.newArrayRef(array, IntConstant.v(i)), safeGetLocal(args[i])));
        }
    }

    @Override
    public void visitJumpStmt(int opcode, int a, int b, Label label) {
        switch (opcode) {
        case OP_IF_EQ:
            units.add(ji.newIfStmt(ji.newEqExpr(safeGetLocal(a), safeGetLocal(b)), label2Unit(label)));
            break;
        case OP_IF_NE:
            units.add(ji.newIfStmt(ji.newNeExpr(safeGetLocal(a), safeGetLocal(b)), label2Unit(label)));
            break;
        case OP_IF_LT:
            units.add(ji.newIfStmt(ji.newLtExpr(safeGetLocal(a), safeGetLocal(b)), label2Unit(label)));
            break;
        case OP_IF_GE:
            units.add(ji.newIfStmt(ji.newGeExpr(safeGetLocal(a), safeGetLocal(b)), label2Unit(label)));
            break;
        case OP_IF_GT:
            units.add(ji.newIfStmt(ji.newGtExpr(safeGetLocal(a), safeGetLocal(b)), label2Unit(label)));
            break;
        case OP_IF_LE:
            units.add(ji.newIfStmt(ji.newLeExpr(safeGetLocal(a), safeGetLocal(b)), label2Unit(label)));
            break;
        }

    }

    @Override
    public void visitJumpStmt(int opcode, int a, Label label) {
        switch (opcode) {
        case OP_IF_EQZ:
            units.add(ji.newIfStmt(ji.newEqExpr(safeGetLocal(a), IntConstant.v(0)), label2Unit(label)));
            break;
        case OP_IF_NEZ:
            units.add(ji.newIfStmt(ji.newNeExpr(safeGetLocal(a), IntConstant.v(0)), label2Unit(label)));
            break;
        case OP_IF_LTZ:
            units.add(ji.newIfStmt(ji.newLtExpr(safeGetLocal(a), IntConstant.v(0)), label2Unit(label)));
            break;
        case OP_IF_GEZ:
            units.add(ji.newIfStmt(ji.newGeExpr(safeGetLocal(a), IntConstant.v(0)), label2Unit(label)));
            break;
        case OP_IF_GTZ:
            units.add(ji.newIfStmt(ji.newGtExpr(safeGetLocal(a), IntConstant.v(0)), label2Unit(label)));
            break;
        case OP_IF_LEZ:
            units.add(ji.newIfStmt(ji.newLeExpr(safeGetLocal(a), IntConstant.v(0)), label2Unit(label)));
            break;
        }
    }

    @Override
    public void visitJumpStmt(int opcode, Label label) {
        units.add(ji.newGotoStmt(label2Unit(label)));
    }

    @Override
    public void visitLabel(Label label) {
        units.add(label2Unit(label));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void visitLookupSwitchStmt(int opcode, int aA, Label label, int[] cases, Label[] labels) {
        List lookupValues = new ArrayList();
        for (int i : cases) {
            lookupValues.add(IntConstant.v(i));
        }
        List targets = new ArrayList();
        for (Label l : labels) {
            targets.add(label2Unit(l));
        }
        units.add(ji.newLookupSwitchStmt(safeGetLocal(aA), lookupValues, targets, label2Unit(label)));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void visitMethodStmt(int opcode, int[] args, Method method) {
        Local _owner;

        int start = 0;
        if (opcode == OP_INVOKE_STATIC) {
            _owner = null;
        } else {
            start++;
            _owner = safeGetLocal(args[0]);
        }
        String methodType_str[] = method.getType().getParameterTypes();
        List methodParameterTypes = new ArrayList(methodType_str.length);
        List methodParameter = new ArrayList(methodType_str.length);
        for (int i = 0; i < methodType_str.length; i++) {
            methodParameter.add(safeGetLocal(args[start++]));
            methodParameterTypes.add(SootUtil.toSootType(methodType_str[i]));
            if (methodType_str[i].equals("D") || methodType_str[i].equals("J")) {
                start++;
            }
        }
        SootMethodRef methodRef = Scene.v().makeMethodRef(RefType.v(x(method.getOwner())).getSootClass(),
                method.getName(), methodParameterTypes, SootUtil.toSootType(method.getType().getReturnType()),
                opcode == OP_INVOKE_STATIC);
        Value value;
        switch (opcode) {
        case OP_INVOKE_VIRTUAL:
            value = new JVirtualInvokeExpr(_owner, methodRef, methodParameter);
            break;
        case OP_INVOKE_SUPER:
            value = new JSpecialInvokeExpr(_owner, methodRef, methodParameter);
            break;
        case OP_INVOKE_DIRECT:
            value = new JSpecialInvokeExpr(_owner, methodRef, methodParameter);
            break;
        case OP_INVOKE_STATIC:
            value = new JStaticInvokeExpr(methodRef, methodParameter);
            break;
        case OP_INVOKE_INTERFACE:
            value = new JInterfaceInvokeExpr(_owner, methodRef, methodParameter);
            break;
        default:
            throw new RuntimeException();
        }
        // if (method.getType().getReturnType().equals("V")) {
        // units.add(ji.newInvokeStmt(value));
        // } else {
        units.add(ji.newAssignStmt(safeGetLocal(Integer.MAX_VALUE), value));
        // }
    }

    @Override
    public void visitMonitorStmt(int opcode, int reg) {
        switch (opcode) {
        case OP_MONITOR_ENTER:
            units.add(ji.newEnterMonitorStmt(safeGetLocal(reg)));
            break;
        case OP_MONITOR_EXIT:
            units.add(ji.newExitMonitorStmt(safeGetLocal(reg)));
            break;
        }
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg) {
        switch (opcode) {
        case OP_MOVE_RESULT:
        case OP_MOVE_RESULT_WIDE:
        case OP_MOVE_RESULT_OBJECT:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), safeGetLocal(Integer.MAX_VALUE)));
            break;
        case OP_MOVE_EXCEPTION:
            units.add(ji.newIdentityStmt(safeGetLocal(Integer.MAX_VALUE), ji.newCaughtExceptionRef()));
            units.add(ji.newAssignStmt(safeGetLocal(toReg), safeGetLocal(Integer.MAX_VALUE)));
            break;

        }
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg) {
        switch (opcode) {
        case OP_MOVE:
        case OP_MOVE_WIDE:
        case OP_MOVE_OBJECT:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), safeGetLocal(fromReg)));
            break;
        }
    }

    @Override
    public void visitReturnStmt(int opcode) {
        units.add(ji.newReturnVoidStmt());
    }

    @Override
    public void visitReturnStmt(int opcode, int reg) {
        switch (opcode) {
        case OP_RETURN:
            units.add(ji.newReturnStmt(safeGetLocal(reg)));
            break;
        case OP_THROW:
            units.add(ji.newThrowStmt(safeGetLocal(reg)));
            break;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void visitTableSwitchStmt(int opcode, int aA, Label label, int first_case, int last_case, Label[] labels) {
        List targets = new ArrayList();
        for (Label l : labels) {
            targets.add(label2Unit(l));
        }
        units.add(ji.newTableSwitchStmt(safeGetLocal(aA), first_case, last_case, targets, label2Unit(label)));

    }

    @Override
    public void visitTryCatch(Label start, Label end, Label handler, String type) {
        body.getTraps().add(
                ji.newTrap((type == null ? RefType.v("java.lang.Throwable") : RefType.v(x(type))).getSootClass(),
                        label2Unit(start), label2Unit(end), label2Unit(handler)));
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg) {
        switch (opcode) {
        case OP_ARRAY_LENGTH:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newLengthExpr(safeGetLocal(fromReg))));
            break;
        case OP_NEG_INT:
        case OP_NEG_LONG:
        case OP_NEG_FLOAT:
        case OP_NEG_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newNegExpr(safeGetLocal(fromReg))));
            break;
        case OP_NOT_INT:
        case OP_NOT_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newNegExpr(safeGetLocal(fromReg))));
            break;

        case OP_INT_TO_LONG:
        case OP_FLOAT_TO_LONG:
        case OP_DOUBLE_TO_LONG:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newCastExpr(safeGetLocal(fromReg), LongType.v())));
            break;
        case OP_INT_TO_FLOAT:
        case OP_LONG_TO_FLOAT:
        case OP_DOUBLE_TO_FLOAT:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newCastExpr(safeGetLocal(fromReg), FloatType.v())));
            break;
        case OP_INT_TO_DOUBLE:
        case OP_LONG_TO_DOUBLE:
        case OP_FLOAT_TO_DOUBLE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newCastExpr(safeGetLocal(fromReg), DoubleType.v())));
            break;
        case OP_LONG_TO_INT:
        case OP_FLOAT_TO_INT:
        case OP_DOUBLE_TO_INT:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newCastExpr(safeGetLocal(fromReg), IntType.v())));
            break;

        case OP_INT_TO_BYTE:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newCastExpr(safeGetLocal(fromReg), ByteType.v())));
            break;
        case OP_INT_TO_CHAR:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newCastExpr(safeGetLocal(fromReg), CharType.v())));
            break;
        case OP_INT_TO_SHORT:
            units.add(ji.newAssignStmt(safeGetLocal(toReg), ji.newCastExpr(safeGetLocal(fromReg), ShortType.v())));
            break;

        }

    }

}
