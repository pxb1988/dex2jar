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
package com.googlecode.d2j.converter;

import com.googlecode.d2j.asm.LdcOptimizeAdapter;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.*;
import com.googlecode.dex2jar.ir.expr.Value.E1Expr;
import com.googlecode.dex2jar.ir.expr.Value.E2Expr;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.*;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("incomplete-switch")
public class IR2JConverter implements Opcodes {

    private boolean optimizeSynchronized = false;

    public IR2JConverter() {
        super();
    }

    public IR2JConverter(boolean optimizeSynchronized) {
        super();
        this.optimizeSynchronized = optimizeSynchronized;
    }

    public void convert(IrMethod ir, MethodVisitor asm) {
        mapLabelStmt(ir);
        reBuildInstructions(ir, asm);
        reBuildTryCatchBlocks(ir, asm);
    }

    private void mapLabelStmt(IrMethod ir) {
        for (Stmt p : ir.stmts) {
            if (p.st == ST.LABEL) {
                LabelStmt labelStmt = (LabelStmt) p;
                labelStmt.tag = new Label();
            }
        }
    }

    /**
     * an empty try-catch block will cause other crash, we check this by finding non-label stmts between
     * {@link Trap#start} and {@link Trap#end}. if find we add the try-catch or we drop the try-catch.
     * 
     * @param ir
     * @param asm
     */
    private void reBuildTryCatchBlocks(IrMethod ir, MethodVisitor asm) {
        for (Trap trap : ir.traps) {
            boolean needAdd = false;
            for (Stmt p = trap.start.getNext(); p != null && p != trap.end; p = p.getNext()) {
                if (p.st != ST.LABEL) {
                    needAdd = true;
                    break;
                }
            }
            if (needAdd) {
                for (int i = 0; i < trap.handlers.length; i++) {
                    String type = trap.types[i];
                    asm.visitTryCatchBlock((Label) trap.start.tag, (Label) trap.end.tag, (Label) trap.handlers[i].tag,
                            type == null ? null : toInternal(type));
                }
            }
        }
    }

    static String toInternal(String n) {
        // TODO replace
        return Type.getType(n).getInternalName();
    }

    

    private void reBuildInstructions(IrMethod ir, MethodVisitor asm) {
        asm = new LdcOptimizeAdapter(asm);
        int maxLocalIndex = 0;
        for (Local local : ir.locals) {
            maxLocalIndex = Math.max(maxLocalIndex, local._ls_index);
        }
        Map<String, Integer> lockMap = new HashMap<String, Integer>();
        for (Stmt st : ir.stmts) {
            switch (st.st) {
            case LABEL:
                LabelStmt labelStmt = (LabelStmt) st;
                Label label = (Label) labelStmt.tag;
                asm.visitLabel(label);
                if (labelStmt.lineNumber >= 0) {
                    asm.visitLineNumber(labelStmt.lineNumber, label);
                }
                break;
            case ASSIGN: {
                E2Stmt e2 = (E2Stmt) st;
                Value v1 = e2.op1;
                Value v2 = e2.op2;
                switch (v1.vt) {
                case LOCAL:

                    Local local = ((Local) v1);
                    int i = local._ls_index;

                    boolean skipOrg = false;
                    if (v2.vt == VT.LOCAL && (i == ((Local) v2)._ls_index)) {// check for a=a
                        skipOrg = true;
                    } else if (v1.valueType.charAt(0) == 'I') {// check for IINC
                        if (v2.vt == VT.ADD) {
                            if (isLocalWithIndex(v2.getOp1(), i) && v2.getOp2().vt == VT.CONSTANT) { // a=a+1;
                                int increment = (Integer) ((Constant) v2.getOp2()).value;
                                if (increment >= Short.MIN_VALUE && increment <= Short.MAX_VALUE) {
                                    asm.visitIincInsn(i, increment);
                                    skipOrg = true;
                                }
                            } else if (isLocalWithIndex(v2.getOp2(), i) && v2.getOp1().vt == VT.CONSTANT) { // a=1+a;
                                int increment = (Integer) ((Constant) v2.getOp1()).value;
                                if (increment >= Short.MIN_VALUE && increment <= Short.MAX_VALUE) {
                                    asm.visitIincInsn(i, increment);
                                    skipOrg = true;
                                }
                            }
                        } else if (v2.vt == VT.SUB) {
                            if (isLocalWithIndex(v2.getOp1(), i) && v2.getOp2().vt == VT.CONSTANT) { // a=a-1;
                                int increment = -(Integer) ((Constant) v2.getOp2()).value;
                                if (increment >= Short.MIN_VALUE && increment <= Short.MAX_VALUE) {
                                    asm.visitIincInsn(i, increment);
                                    skipOrg = true;
                                }
                            }
                        }
                    }
                    if (!skipOrg) {
                        accept(v2, asm);
                        if (i >= 0) {
                            asm.visitVarInsn(getOpcode(v1, ISTORE), i);
                        } else if (!v1.valueType.equals("V")) { // skip void type locals
                            switch (v1.valueType.charAt(0)) {
                            case 'J':
                            case 'D':
                                asm.visitInsn(POP2);
                                break;
                            default:
                                asm.visitInsn(POP);
                                break;
                            }
                        }
                    }
                    break;
                case STATIC_FIELD: {
                    StaticFieldExpr fe = (StaticFieldExpr) v1;
                    accept(v2, asm);
                    insertI2x(v2.valueType, fe.type, asm);
                    asm.visitFieldInsn(PUTSTATIC, toInternal(fe.owner), fe.name, fe.type);
                    break;
                }
                case FIELD: {
                    FieldExpr fe = (FieldExpr) v1;
                    accept(fe.op, asm);
                    accept(v2, asm);
                    insertI2x(v2.valueType, fe.type, asm);
                    asm.visitFieldInsn(PUTFIELD, toInternal(fe.owner), fe.name, fe.type);
                    break;
                }
                case ARRAY:
                    ArrayExpr ae = (ArrayExpr) v1;
                    accept(ae.op1, asm);
                    accept(ae.op2, asm);
                    accept(v2, asm);
                    String tp1 = ae.op1.valueType;
                    String tp2 = ae.valueType;
                    if (tp1.charAt(0) == '[') {
                        String arrayElementType = tp1.substring(1);
                        insertI2x(v2.valueType, arrayElementType, asm);
                        asm.visitInsn(getOpcode(arrayElementType, IASTORE));
                    } else {
                        asm.visitInsn(getOpcode(tp2, IASTORE));
                    }
                    break;
                }
            }
                break;
            case IDENTITY: {
                E2Stmt e2 = (E2Stmt) st;
                if (e2.op2.vt == VT.EXCEPTION_REF) {
                    int index = ((Local) e2.op1)._ls_index;
                    if (index >= 0) {
                        asm.visitVarInsn(ASTORE, index);
                    } else {
                        asm.visitInsn(POP);
                    }
                }
            }
                break;

            case FILL_ARRAY_DATA:{
                E2Stmt e2 = (E2Stmt) st;
                if (e2.getOp2().vt == VT.CONSTANT) {
                    Object arrayData = ((Constant) e2.getOp2()).value;
                    int arraySize = Array.getLength(arrayData);
                    String arrayValueType = e2.getOp1().valueType;
                    String elementType;
                    if (arrayValueType.charAt(0) == '[') {
                        elementType = arrayValueType.substring(1);
                    } else {
                        elementType = "I";
                    }
                    int iastoreOP = getOpcode(elementType, IASTORE);
                    accept(e2.getOp1(), asm);
                    for (int i = 0; i < arraySize; i++) {
                        asm.visitInsn(DUP);
                        asm.visitLdcInsn(i);
                        asm.visitLdcInsn(Array.get(arrayData, i));
                        asm.visitInsn(iastoreOP);
                    }
                    asm.visitInsn(POP);
                } else {
                    FilledArrayExpr filledArrayExpr = (FilledArrayExpr) e2.getOp2();
                    int arraySize = filledArrayExpr.ops.length;
                    String arrayValueType = e2.getOp1().valueType;
                    String elementType;
                    if (arrayValueType.charAt(0) == '[') {
                        elementType = arrayValueType.substring(1);
                    } else {
                        elementType = "I";
                    }
                    int iastoreOP = getOpcode(elementType, IASTORE);
                    accept(e2.getOp1(), asm);
                    for (int i = 0; i < arraySize; i++) {
                        asm.visitInsn(DUP);
                        asm.visitLdcInsn(i);
                        accept(filledArrayExpr.ops[i], asm);
                        asm.visitInsn(iastoreOP);
                    }
                    asm.visitInsn(POP);
                }
            }
            break;
            case GOTO:
                asm.visitJumpInsn(GOTO, (Label) ((GotoStmt) st).target.tag);
                break;
            case IF:
                reBuildJumpInstructions((IfStmt) st, asm);
                break;
            case LOCK: {
                Value v = ((UnopStmt) st).op;
                accept(v, asm);
                if (optimizeSynchronized) {
                    switch (v.vt) {
                    case LOCAL:
                        // FIXME do we have to disable local due to OptSyncTest ?
                        // break;
                    case CONSTANT: {
                        String key;
                        if (v.vt == VT.LOCAL) {
                            key = "L" + ((Local) v)._ls_index;
                        } else {
                            key = "C" + ((Constant) v).value;
                        }
                        Integer integer = lockMap.get(key);
                        int nIndex = integer != null ? integer : ++maxLocalIndex;
                        asm.visitInsn(DUP);
                        asm.visitVarInsn(getOpcode(v, ISTORE), nIndex);
                        lockMap.put(key, nIndex);
                    }
                        break;
                    default:
                        throw new RuntimeException();
                    }
                }
                asm.visitInsn(MONITORENTER);
            }
                break;
            case UNLOCK: {
                Value v = ((UnopStmt) st).op;
                if (optimizeSynchronized) {
                    switch (v.vt) {
                    case LOCAL:
                    case CONSTANT: {
                        String key;
                        if (v.vt == VT.LOCAL) {
                            key = "L" + ((Local) v)._ls_index;
                        } else {
                            key = "C" + ((Constant) v).value;
                        }
                        Integer integer = lockMap.get(key);
                        if (integer != null) {
                            asm.visitVarInsn(getOpcode(v, ILOAD), integer);
                        } else {
                            accept(v, asm);
                        }
                    }
                        break;
                    // TODO other
                    default: {
                        accept(v, asm);
                        break;
                    }
                    }
                } else {
                    accept(v, asm);
                }
                asm.visitInsn(MONITOREXIT);
            }
                break;
            case NOP:
                break;
            case RETURN: {
                Value v = ((UnopStmt) st).op;
                accept(v, asm);
                insertI2x(v.valueType, ir.ret, asm);
                asm.visitInsn(getOpcode(v, IRETURN));
            }
                break;
            case RETURN_VOID:
                asm.visitInsn(RETURN);
                break;
            case LOOKUP_SWITCH: {
                LookupSwitchStmt lss = (LookupSwitchStmt) st;
                accept(lss.op, asm);
                Label targets[] = new Label[lss.targets.length];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = (Label) lss.targets[i].tag;
                }
                asm.visitLookupSwitchInsn((Label) lss.defaultTarget.tag, lss.lookupValues, targets);
            }
                break;
            case TABLE_SWITCH: {
                TableSwitchStmt tss = (TableSwitchStmt) st;
                accept(tss.op, asm);
                Label targets[] = new Label[tss.targets.length];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = (Label) tss.targets[i].tag;
                }
                asm.visitTableSwitchInsn(tss.lowIndex, tss.lowIndex + targets.length - 1,
                        (Label) tss.defaultTarget.tag, targets);
            }
                break;
            case THROW:
                accept(((UnopStmt) st).op, asm);
                asm.visitInsn(ATHROW);
                break;
            case VOID_INVOKE:
                Value op = st.getOp();
                accept(op, asm);

                String ret = op.valueType;
                if (op.vt == VT.INVOKE_NEW) {
                    asm.visitInsn(POP);
                } else if (!"V".equals(ret)) {
                    switch (ret.charAt(0)) {
                        case 'J':
                        case 'D':
                            asm.visitInsn(POP2);
                            break;
                        default:
                            asm.visitInsn(POP);
                            break;
                    }
                }
                break;
            default:
                throw new RuntimeException("not support st: " + st.st);
            }

        }
    }

    private static boolean isLocalWithIndex(Value v, int i) {
        return v.vt == VT.LOCAL && ((Local) v)._ls_index == i;
    }

    /**
     * insert I2x instruction
     * 
     * @param tos
     * @param expect
     * @param mv
     */
    private static void insertI2x(String tos, String expect, MethodVisitor mv) {
        switch (expect.charAt(0)) {
        case 'B':
            switch (tos.charAt(0)) {
            case 'S':
            case 'C':
            case 'I':
                mv.visitInsn(I2B);
            }
            break;
        case 'S':
            switch (tos.charAt(0)) {
            case 'C':
            case 'I':
                mv.visitInsn(I2S);
            }
            break;
        case 'C':
            switch (tos.charAt(0)) {
            case 'I':
                mv.visitInsn(I2C);
            }
            break;
        }
    }

    static boolean isZeroOrNull(Value v1) {
        if (v1.vt == VT.CONSTANT) {
            Object v = ((Constant) v1).value;
            return Integer.valueOf(0).equals(v) || Constant.Null.equals(v);
        }
        return false;
    }

    private void reBuildJumpInstructions(IfStmt st, MethodVisitor asm) {
        Label target = (Label) st.target.tag;
        Value v = st.op;
        Value v1 = v.getOp1();
        Value v2 = v.getOp2();

        String type = v1.valueType;

        switch (type.charAt(0)) {
        case '[':
        case 'L':
            // IF_ACMPx
            // IF[non]null
            if (isZeroOrNull(v1) || isZeroOrNull(v2)) { // IF[non]null
                if (isZeroOrNull(v2)) {// v2 is null
                    accept(v1, asm);
                } else {
                    accept(v2, asm);
                }
                asm.visitJumpInsn(v.vt == VT.EQ ? IFNULL : IFNONNULL, target);
            } else {
                accept(v1, asm);
                accept(v2, asm);
                asm.visitJumpInsn(v.vt == VT.EQ ? IF_ACMPEQ : IF_ACMPNE, target);
            }
            break;
        default:
            // IFx
            // IF_ICMPx
            if (isZeroOrNull(v1) || isZeroOrNull(v2)) { // IFx
                if (isZeroOrNull(v2)) {// v2 is zero
                    accept(v1, asm);
                } else {
                    accept(v2, asm);
                }
                switch (v.vt) {
                case NE:
                    asm.visitJumpInsn(IFNE, target);
                    break;
                case EQ:
                    asm.visitJumpInsn(IFEQ, target);
                    break;
                case GE:
                    asm.visitJumpInsn(IFGE, target);
                    break;
                case GT:
                    asm.visitJumpInsn(IFGT, target);
                    break;
                case LE:
                    asm.visitJumpInsn(IFLE, target);
                    break;
                case LT:
                    asm.visitJumpInsn(IFLT, target);
                    break;
                }
            } else { // IF_ICMPx
                accept(v1, asm);
                accept(v2, asm);
                switch (v.vt) {
                case NE:
                    asm.visitJumpInsn(IF_ICMPNE, target);
                    break;
                case EQ:
                    asm.visitJumpInsn(IF_ICMPEQ, target);
                    break;
                case GE:
                    asm.visitJumpInsn(IF_ICMPGE, target);
                    break;
                case GT:
                    asm.visitJumpInsn(IF_ICMPGT, target);
                    break;
                case LE:
                    asm.visitJumpInsn(IF_ICMPLE, target);
                    break;
                case LT:
                    asm.visitJumpInsn(IF_ICMPLT, target);
                    break;
                }
            }
            break;
        }
    }

    /**
     * 
     * @param v
     * @param op
     *            DUP
     * @return
     */
    static int getOpcode(Value v, int op) {
        return getOpcode(v.valueType, op);
    }

    static int getOpcode(String v, int op) {
        return Type.getType(v).getOpcode(op);
    }

    private static void accept(Value value, MethodVisitor asm) {

        switch (value.et) {
        case E0:
            switch (value.vt) {
            case LOCAL:
                asm.visitVarInsn(getOpcode(value, ILOAD), ((Local) value)._ls_index);
                break;
            case CONSTANT:
                Constant cst = (Constant) value;
                if (cst.value.equals(Constant.Null)) {
                    asm.visitInsn(ACONST_NULL);
                } else if (cst.value instanceof Constant.Type) {
                    asm.visitLdcInsn(Type.getType(((Constant.Type) cst.value).desc));
                } else {
                    asm.visitLdcInsn(cst.value);
                }
                break;
            case NEW:
                asm.visitTypeInsn(NEW, toInternal(((NewExpr) value).type));
                break;
            case STATIC_FIELD:
                StaticFieldExpr sfe= (StaticFieldExpr) value;
                asm.visitFieldInsn(GETSTATIC,toInternal(sfe.owner),sfe.name,sfe.type);
                break;
            }
            break;
        case E1:
            reBuildE1Expression((E1Expr) value, asm);
            break;
        case E2:
            reBuildE2Expression((E2Expr) value, asm);
            break;
        case En:
            reBuildEnExpression((EnExpr) value, asm);
            break;
        }
    }

    private static void reBuildEnExpression(EnExpr value, MethodVisitor asm) {
        if (value.vt == VT.FILLED_ARRAY) {
            FilledArrayExpr fae = (FilledArrayExpr) value;
            reBuildE1Expression(Exprs.nNewArray(fae.type, Exprs.nInt(fae.ops.length)), asm);
            String tp1 = fae.valueType;
            int xastore = IASTORE;
            String elementType = null;
            if (tp1.charAt(0) == '[') {
                elementType = tp1.substring(1);
                xastore = getOpcode(elementType, IASTORE);
            }

            for (int i = 0; i < fae.ops.length; i++) {
                if (fae.ops[i] == null)
                    continue;
                asm.visitInsn(DUP);
                asm.visitLdcInsn(i);
                accept(fae.ops[i], asm);
                String tp2 = fae.ops[i].valueType;
                if (elementType != null) {
                    insertI2x(tp2, elementType, asm);
                }
                asm.visitInsn(xastore);
            }
            return;
        }

        switch (value.vt) {
        case NEW_MUTI_ARRAY:
            for (Value vb : value.ops) {
                accept(vb, asm);
            }
            NewMutiArrayExpr nmae = (NewMutiArrayExpr) value;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nmae.dimension; i++) {
                sb.append('[');
            }
            sb.append(nmae.baseType);
            asm.visitMultiANewArrayInsn(sb.toString(), nmae.dimension);
            break;
        case INVOKE_NEW:
            asm.visitTypeInsn(NEW, toInternal(((InvokeExpr) value).owner));
            asm.visitInsn(DUP);
            // pass through
        case INVOKE_INTERFACE:
        case INVOKE_SPECIAL:
        case INVOKE_STATIC:
        case INVOKE_VIRTUAL:
            InvokeExpr ie = (InvokeExpr) value;
            int i = 0;
            if (value.vt != VT.INVOKE_STATIC && value.vt != VT.INVOKE_NEW) {
                i = 1;
                accept(value.ops[0], asm);
            }
            for (int j = 0; i < value.ops.length; i++, j++) {
                Value vb = value.ops[i];
                accept(vb, asm);
                insertI2x(vb.valueType, ie.args[j], asm);
            }

            int opcode;
            switch (value.vt) {
            case INVOKE_VIRTUAL:
                opcode = INVOKEVIRTUAL;
                break;
            case INVOKE_INTERFACE:
                opcode = INVOKEINTERFACE;
                break;
            case INVOKE_NEW:
            case INVOKE_SPECIAL:
                opcode = INVOKESPECIAL;
                break;
            case INVOKE_STATIC:
                opcode = INVOKESTATIC;
                break;
            default:
                opcode = -1;
            }

            asm.visitMethodInsn(opcode, toInternal(ie.owner), ie.name,
                    buildMethodDesc(ie.vt == VT.INVOKE_NEW ? "V" : ie.ret, ie.args));
            break;
        }
    }

    static String buildMethodDesc(String ret, String... ps) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (String p : ps) {
            sb.append(p);
        }
        sb.append(')');
        sb.append(ret);
        return sb.toString();
    }

    private static void reBuildE1Expression(E1Expr e1, MethodVisitor asm) {
        accept(e1.getOp(), asm);
        switch (e1.vt) {
        case STATIC_FIELD: {
            FieldExpr fe = (FieldExpr) e1;
            asm.visitFieldInsn(GETSTATIC, toInternal(fe.owner), fe.name, fe.type);
            break;
        }
        case FIELD: {
            FieldExpr fe = (FieldExpr) e1;
            asm.visitFieldInsn(GETFIELD, toInternal(fe.owner), fe.name, fe.type);
            break;
        }
        case NEW_ARRAY: {
            TypeExpr te = (TypeExpr) e1;
            switch (te.type.charAt(0)) {
            case '[':
            case 'L':
                asm.visitTypeInsn(ANEWARRAY, toInternal(te.type));
                break;
            case 'Z':
                asm.visitIntInsn(NEWARRAY, T_BOOLEAN);
                break;
            case 'B':
                asm.visitIntInsn(NEWARRAY, T_BYTE);
                break;
            case 'S':
                asm.visitIntInsn(NEWARRAY, T_SHORT);
                break;
            case 'C':
                asm.visitIntInsn(NEWARRAY, T_CHAR);
                break;
            case 'I':
                asm.visitIntInsn(NEWARRAY, T_INT);
                break;
            case 'F':
                asm.visitIntInsn(NEWARRAY, T_FLOAT);
                break;
            case 'J':
                asm.visitIntInsn(NEWARRAY, T_LONG);
                break;
            case 'D':
                asm.visitIntInsn(NEWARRAY, T_DOUBLE);
                break;
            }
        }
            break;
        case CHECK_CAST:
        case INSTANCE_OF: {
            TypeExpr te = (TypeExpr) e1;
            asm.visitTypeInsn(e1.vt == VT.CHECK_CAST ? CHECKCAST : INSTANCEOF, toInternal(te.type));
        }
            break;
        case CAST: {
            CastExpr te = (CastExpr) e1;
            cast2(e1.op.valueType, te.to, asm);
        }
            break;
        case LENGTH:
            asm.visitInsn(ARRAYLENGTH);
            break;
        case NEG:
            asm.visitInsn(getOpcode(e1, INEG));
            break;
        }
    }

    private static void reBuildE2Expression(E2Expr e2, MethodVisitor asm) {
        String type = e2.op2.valueType;
        accept(e2.op1, asm);
        if ((e2.vt == VT.ADD || e2.vt == VT.SUB) && e2.op2.vt == VT.CONSTANT) {
            // [x + (-1)] to [x - 1]
            // [x - (-1)] to [x + 1]
            Constant constant = (Constant) e2.op2;
            String t = constant.valueType;
            switch (t.charAt(0)) {
            case 'S':
            case 'B':
            case 'I': {
                int s = (Integer) constant.value;
                if (s < 0) {
                    asm.visitLdcInsn(-s);
                    asm.visitInsn(getOpcode(type, e2.vt == VT.ADD ? ISUB : IADD));
                    return;
                }
            }
                break;
            case 'F': {
                float s = (Float) constant.value;
                if (s < 0) {
                    asm.visitLdcInsn(-s);
                    asm.visitInsn(getOpcode(type, e2.vt == VT.ADD ? ISUB : IADD));
                    return;
                }
            }
                break;
            case 'J': {
                long s = (Long) constant.value;
                if (s < 0) {
                    asm.visitLdcInsn(-s);
                    asm.visitInsn(getOpcode(type, e2.vt == VT.ADD ? ISUB : IADD));
                    return;
                }
            }
                break;
            case 'D': {
                double s = (Double) constant.value;
                if (s < 0) {
                    asm.visitLdcInsn(-s);
                    asm.visitInsn(getOpcode(type, e2.vt == VT.ADD ? ISUB : IADD));
                    return;
                }
            }
                break;
            }
        }

        accept(e2.op2, asm);

        String tp1 = e2.op1.valueType;
        switch (e2.vt) {
        case ARRAY:
            String tp2 = e2.valueType;
            if (tp1.charAt(0) == '[') {
                asm.visitInsn(getOpcode(tp1.substring(1), IALOAD));
            } else {
                asm.visitInsn(getOpcode(tp2, IALOAD));
            }
            break;
        case ADD:
            asm.visitInsn(getOpcode(type, IADD));
            break;
        case SUB:
            asm.visitInsn(getOpcode(type, ISUB));
            break;
        case IDIV:
        case LDIV:
        case FDIV:
        case DDIV:
            asm.visitInsn(getOpcode(type, IDIV));
            break;
        case MUL:
            asm.visitInsn(getOpcode(type, IMUL));
            break;
        case REM:
            asm.visitInsn(getOpcode(type, IREM));
            break;
        case AND:
            asm.visitInsn(getOpcode(type, IAND));
            break;
        case OR:
            asm.visitInsn(getOpcode(type, IOR));
            break;
        case XOR:
            asm.visitInsn(getOpcode(type, IXOR));
            break;

        case SHL:
            asm.visitInsn(getOpcode(tp1, ISHL));
            break;
        case SHR:
            asm.visitInsn(getOpcode(tp1, ISHR));
            break;
        case USHR:
            asm.visitInsn(getOpcode(tp1, IUSHR));
            break;
        case LCMP:
            asm.visitInsn(LCMP);
            break;
        case FCMPG:
            asm.visitInsn(FCMPG);
            break;
        case DCMPG:
            asm.visitInsn(DCMPG);
            break;
        case FCMPL:
            asm.visitInsn(FCMPL);
            break;
        case DCMPL:
            asm.visitInsn(DCMPL);
            break;
        }
    }

    private static void cast2(String t1, String t2, MethodVisitor asm) {
        if (t1.equals(t2)) {
            return;
        }
        switch (t1.charAt(0)) {
        case 'Z':
        case 'B':
        case 'C':
        case 'S':
        case 'I': {
            switch (t2.charAt(0)) {
            case 'F':
                asm.visitInsn(I2F);
                break;
            case 'J':
                asm.visitInsn(I2L);
                break;
            case 'D':
                asm.visitInsn(I2D);
                break;
            case 'C':
                asm.visitInsn(I2C);
                break;
            case 'B':
                asm.visitInsn(I2B);
                break;
            case 'S':
                asm.visitInsn(I2S);
                break;
            }
        }
            break;
        case 'J': {
            switch (t2.charAt(0)) {
            case 'I':
                asm.visitInsn(L2I);
                break;
            case 'F':
                asm.visitInsn(L2F);
                break;
            case 'D':
                asm.visitInsn(L2D);
                break;
            }
        }
            break;
        case 'D': {
            switch (t2.charAt(0)) {
            case 'I':
                asm.visitInsn(D2I);
                break;
            case 'F':
                asm.visitInsn(D2F);
                break;
            case 'J':
                asm.visitInsn(D2L);
                break;
            }
        }
            break;
        case 'F': {
            switch (t2.charAt(0)) {
            case 'I':
                asm.visitInsn(F2I);
                break;
            case 'J':
                asm.visitInsn(F2L);
                break;
            case 'D':
                asm.visitInsn(F2D);
                break;
            }
            break;
        }
        }
    }
}
