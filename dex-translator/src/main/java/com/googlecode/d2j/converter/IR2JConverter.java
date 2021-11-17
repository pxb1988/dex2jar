package com.googlecode.d2j.converter;

import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.asm.LdcOptimizeAdapter;
import com.googlecode.d2j.dex.Dex2Asm;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.CastExpr;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.FilledArrayExpr;
import com.googlecode.dex2jar.ir.expr.InvokeCustomExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.InvokePolymorphicExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.NewExpr;
import com.googlecode.dex2jar.ir.expr.NewMutiArrayExpr;
import com.googlecode.dex2jar.ir.expr.StaticFieldExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.expr.Value.E1Expr;
import com.googlecode.dex2jar.ir.expr.Value.E2Expr;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.GotoStmt;
import com.googlecode.dex2jar.ir.stmt.IfStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class IR2JConverter implements Opcodes {

    public static final int MAX_FILL_ARRAY_BYTES = 500;

    private boolean optimizeSynchronized = false;

    Dex2Asm.ClzCtx clzCtx;

    IrMethod ir;

    MethodVisitor asm;

    public IR2JConverter() {
        super();
    }

    public IR2JConverter optimizeSynchronized(boolean optimizeSynchronized) {
        this.optimizeSynchronized = optimizeSynchronized;
        return this;
    }

    public IR2JConverter clzCtx(Dex2Asm.ClzCtx clzCtx) {
        this.clzCtx = clzCtx;
        return this;
    }

    public IR2JConverter ir(IrMethod ir) {
        this.ir = ir;
        return this;
    }

    public IR2JConverter asm(MethodVisitor asm) {
        this.asm = asm;
        return this;
    }

    public void convert() {
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
            maxLocalIndex = Math.max(maxLocalIndex, local.lsIndex);
        }
        Map<String, Integer> lockMap = new HashMap<>();
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
                    int i = local.lsIndex;

                    boolean skipOrg = false;
                    if (v2.vt == VT.LOCAL && (i == ((Local) v2).lsIndex)) { // check for a=a
                        skipOrg = true;
                    } else if (v1.valueType.charAt(0) == 'I') { // check for IINC
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
                default:
                    break;
                }
            }
            break;
            case IDENTITY: {
                E2Stmt e2 = (E2Stmt) st;
                if (e2.op2.vt == VT.EXCEPTION_REF) {
                    int index = ((Local) e2.op1).lsIndex;
                    if (index >= 0) {
                        asm.visitVarInsn(ASTORE, index);
                    } else {
                        asm.visitInsn(POP);
                    }
                }
            }
            break;

            case FILL_ARRAY_DATA: {
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
                    boolean genBig = false;
                    try {
                        if (this.clzCtx != null
                                && "BSIJ".contains(elementType)) {

                            byte[] data = toLittleEndianArray(arrayData);

                            if (data != null && data.length > MAX_FILL_ARRAY_BYTES) {
                                accept(e2.getOp1(), asm);
                                asm.visitLdcInsn(0);
                                constLargeArray(asm, data, elementType);
                                asm.visitLdcInsn(0);
                                asm.visitLdcInsn(arraySize);

                                asm.visitMethodInsn(Opcodes.INVOKESTATIC,
                                        "java/lang/System",
                                        "arraycopy",
                                        "(Ljava/lang/Object;ILjava/lang/Object;II)V",
                                        false
                                );
                                genBig = true;
                            }

                        }
                    } catch (Exception ignore) {
                        // any exception, revert to normal
                    }

                    if (!genBig) {
                        int iastoreOP = getOpcode(elementType, IASTORE);
                        accept(e2.getOp1(), asm);
                        for (int i = 0; i < arraySize; i++) {
                            asm.visitInsn(DUP);
                            asm.visitLdcInsn(i);
                            asm.visitLdcInsn(Array.get(arrayData, i));
                            asm.visitInsn(iastoreOP);
                        }
                        asm.visitInsn(POP);
                    }
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

                    boolean genBig = false;
                    try {
                        if (this.clzCtx != null
                                && "BSIJ".contains(elementType)
                                && isConstant(filledArrayExpr.ops)) {
                            // create a 500-len byte array, may cause 'Method code too large!'
                            // convert it to a base64 decoding
                            byte[] data = collectDataAsByteArray(filledArrayExpr.ops, elementType);
                            if (data != null && data.length > MAX_FILL_ARRAY_BYTES) {
                                accept(e2.getOp1(), asm);
                                asm.visitLdcInsn(0);
                                constLargeArray(asm, data, elementType);
                                asm.visitLdcInsn(0);
                                asm.visitLdcInsn(arraySize);

                                asm.visitMethodInsn(INVOKESTATIC,
                                        "java/lang/System",
                                        "arraycopy",
                                        "(Ljava/lang/Object;ILjava/lang/Object;II)V",
                                        false
                                );

                                genBig = true;
                            }
                        }
                    } catch (Exception ignore) {
                        // any exception, revert to normal
                    }

                    if (!genBig) {
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
                            key = "L" + ((Local) v).lsIndex;
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
                            key = "L" + ((Local) v).lsIndex;
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
                Label[] targets = new Label[lss.targets.length];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = (Label) lss.targets[i].tag;
                }
                asm.visitLookupSwitchInsn((Label) lss.defaultTarget.tag, lss.lookupValues, targets);
            }
            break;
            case TABLE_SWITCH: {
                TableSwitchStmt tss = (TableSwitchStmt) st;
                accept(tss.op, asm);
                Label[] targets = new Label[tss.targets.length];
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

    private void constLargeArray(MethodVisitor asm, byte[] data, String elementType) {
        String cst = hexEncode(data);
        if (cst.length() > 65535) { // asm have the limit
            asm.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            asm.visitInsn(Opcodes.DUP);
            asm.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

            for (int i = 0; i < cst.length(); i += 65500) {
                int a = Math.min(65500, cst.length() - i);
                asm.visitLdcInsn(cst.substring(i, i + a));
                asm.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                        "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                        false
                );
            }
            asm.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                    "toString",
                    "()Ljava/lang/String;",
                    false
            );
        } else {
            asm.visitLdcInsn(cst);
        }

        asm.visitMethodInsn(Opcodes.INVOKESTATIC, toInternal(this.clzCtx.classDescriptor),
                this.clzCtx.buildHexDecodeMethodName(elementType), "(Ljava/lang/String;)[" + elementType, false);
    }

    private static boolean isLocalWithIndex(Value v, int i) {
        return v.vt == VT.LOCAL && ((Local) v).lsIndex == i;
    }

    /**
     * insert I2x instruction
     */
    private static void insertI2x(String tos, String expect, MethodVisitor mv) {
        switch (expect.charAt(0)) {
        case 'B':
            switch (tos.charAt(0)) {
            case 'S':
            case 'C':
            case 'I':
                mv.visitInsn(I2B);
            default:
                break;
            }
            break;
        case 'S':
            switch (tos.charAt(0)) {
            case 'C':
            case 'I':
                mv.visitInsn(I2S);
            default:
                break;
            }
            break;
        case 'C':
            switch (tos.charAt(0)) {
            case 'I':
                mv.visitInsn(I2C);
            default:
                break;
            }
            break;
        default:
            break;
        }
    }

    static boolean isZeroOrNull(Value v1) {
        if (v1.vt == VT.CONSTANT) {
            Object v = ((Constant) v1).value;
            return Integer.valueOf(0).equals(v) || Constant.NULL.equals(v);
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
                if (isZeroOrNull(v2)) { // v2 is null
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
                if (isZeroOrNull(v2)) { // v2 is zero
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
                default:
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
                default:
                    break;
                }
            }
            break;
        }
    }

    /**
     * @param op DUP
     */
    static int getOpcode(Value v, int op) {
        return getOpcode(v.valueType, op);
    }

    static int getOpcode(String v, int op) {
        switch (v.charAt(0)) {
        case 'L':
        case '[':
            return Type.getType("La;").getOpcode(op);
        case 'Z':
            return Type.BOOLEAN_TYPE.getOpcode(op);
        case 'B':
            return Type.BYTE_TYPE.getOpcode(op);
        case 'S':
            return Type.SHORT_TYPE.getOpcode(op);
        case 'C':
            return Type.CHAR_TYPE.getOpcode(op);
        case 'I':
            return Type.INT_TYPE.getOpcode(op);
        case 'F':
            return Type.FLOAT_TYPE.getOpcode(op);
        case 'J':
            return Type.LONG_TYPE.getOpcode(op);
        case 'D':
            return Type.DOUBLE_TYPE.getOpcode(op);
        default:
            // FIXME handle undetected types
            return Type.INT_TYPE.getOpcode(op); // treat other as int
        }
    }

    private void accept(Value value, MethodVisitor asm) {

        switch (value.et) {
        case E0:
            switch (value.vt) {
            case LOCAL:
                asm.visitVarInsn(getOpcode(value, ILOAD), ((Local) value).lsIndex);
                break;
            case CONSTANT:
                Constant cst = (Constant) value;
                if (cst.value.equals(Constant.NULL)) {
                    asm.visitInsn(ACONST_NULL);
                } else if (cst.value instanceof DexType) {
                    asm.visitLdcInsn(Type.getType(((DexType) cst.value).desc));
                } else {
                    asm.visitLdcInsn(cst.value);
                }
                break;
            case NEW:
                asm.visitTypeInsn(NEW, toInternal(((NewExpr) value).type));
                break;
            case STATIC_FIELD:
                StaticFieldExpr sfe = (StaticFieldExpr) value;
                asm.visitFieldInsn(GETSTATIC, toInternal(sfe.owner), sfe.name, sfe.type);
                break;
            default:
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
        default:
            break;
        }
    }

    public static String hexEncode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    private void reBuildEnExpression(EnExpr value, MethodVisitor asm) {
        if (value.vt == VT.FILLED_ARRAY) {
            FilledArrayExpr fae = (FilledArrayExpr) value;
            String tp1 = fae.valueType;
            int xastore = IASTORE;
            String elementType = null;
            if (tp1.charAt(0) == '[') {
                elementType = tp1.substring(1);
                xastore = getOpcode(elementType, IASTORE);
            }

            try {
                if (this.clzCtx != null
                        && elementType != null
                        && "BSIJ".contains(elementType)
                        && isConstant(fae.ops)) {

                    byte[] data = collectDataAsByteArray(fae.ops, elementType);
                    if (data != null && data.length > MAX_FILL_ARRAY_BYTES) {
                        constLargeArray(asm, data, elementType);
                        return;
                    }
                }
            } catch (Exception ignore) {
                // any exception, revert to normal
            }

            reBuildE1Expression(Exprs.nNewArray(fae.type, Exprs.nInt(fae.ops.length)), asm);


            for (int i = 0; i < fae.ops.length; i++) {
                if (fae.ops[i] == null) {
                    continue;
                }
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
            asm.visitMultiANewArrayInsn(sb.toString(), value.ops.length);
            break;
        case INVOKE_NEW:
            asm.visitTypeInsn(NEW, toInternal(((InvokeExpr) value).getOwner()));
            asm.visitInsn(DUP);
            // pass through
        case INVOKE_INTERFACE:
        case INVOKE_SPECIAL:
        case INVOKE_STATIC:
        case INVOKE_VIRTUAL: {
            InvokeExpr ie = (InvokeExpr) value;
            int i = 0;
            if (value.vt != VT.INVOKE_STATIC && value.vt != VT.INVOKE_NEW) {
                i = 1;
                accept(value.ops[0], asm);
            }
            for (int j = 0; i < value.ops.length; i++, j++) {
                Value vb = value.ops[i];
                accept(vb, asm);
                insertI2x(vb.valueType, ie.getArgs()[j], asm);
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

            Proto p = ie.getProto();
            if (ie.vt == VT.INVOKE_NEW) {
                p = new Proto(p.getParameterTypes(), "V");
            }
            asm.visitMethodInsn(opcode, toInternal(ie.getOwner()), ie.getName(), p.getDesc(),
                    opcode == INVOKEINTERFACE);
        }
        break;
        case INVOKE_CUSTOM: {
            InvokeCustomExpr ice = (InvokeCustomExpr) value;
            String[] argTypes = ice.getProto().getParameterTypes();
            Value[] vbs = ice.getOps();
            if (argTypes.length == vbs.length) {
                for (int i = 0; i < vbs.length; i++) {
                    Value vb = vbs[i];
                    accept(vb, asm);
                    insertI2x(vb.valueType, argTypes[i], asm);
                }
            } else if (argTypes.length + 1 == vbs.length) {
                accept(vbs[0], asm);
                for (int i = 1; i < vbs.length; i++) {
                    Value vb = vbs[i];
                    accept(vb, asm);
                    insertI2x(vb.valueType, argTypes[i - 1], asm);
                }
            } else {
                throw new RuntimeException();
            }
            asm.visitInvokeDynamicInsn(ice.name, ice.proto.getDesc(),
                    (Handle) Dex2Asm.convertConstantValue(ice.handle), Dex2Asm.convertConstantValues(ice.bsmArgs));
        }
        break;
        case INVOKE_POLYMORPHIC: {
            InvokePolymorphicExpr ipe = (InvokePolymorphicExpr) value;
            Method m = ipe.method;
            String[] argTypes = ipe.getProto().getParameterTypes();
            Value[] vbs = ipe.getOps();
            accept(vbs[0], asm);
            for (int i = 1; i < vbs.length; i++) {
                Value vb = vbs[i];
                accept(vb, asm);
                insertI2x(vb.valueType, argTypes[i - 1], asm);
            }
            asm.visitMethodInsn(INVOKEVIRTUAL, toInternal(m.getOwner()), m.getName(), ipe.getProto().getDesc(), false);
        }
        default:
            break;
        }
    }

    private static byte[] collectDataAsByteArray(Value[] ops, String t) {
        switch (t) {
        case "B": {
            byte[] d = new byte[ops.length];
            for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
                Value op = ops[i];
                Constant cst = (Constant) op;
                d[i] = ((Number) cst.value).byteValue();
            }
            return d;
        }
        case "S": {
            short[] d = new short[ops.length];
            for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
                Value op = ops[i];
                Constant cst = (Constant) op;
                d[i] = ((Number) cst.value).shortValue();
            }
            return toLittleEndianArray(d);
        }
        case "I": {
            int[] d = new int[ops.length];
            for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
                Value op = ops[i];
                Constant cst = (Constant) op;
                d[i] = ((Number) cst.value).intValue();
            }
            return toLittleEndianArray(d);
        }
        case "J": {
            long[] d = new long[ops.length];
            for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
                Value op = ops[i];
                Constant cst = (Constant) op;
                d[i] = ((Number) cst.value).longValue();
            }
            return toLittleEndianArray(d);
        }
        default:
            return null;
        }
    }

    private static byte[] toLittleEndianArray(Object d) {
        if (d instanceof byte[]) {
            return (byte[]) d;
        } else if (d instanceof short[]) {
            return toLittleEndianArray((short[]) d);
        } else if (d instanceof int[]) {
            return toLittleEndianArray((int[]) d);
        } else if (d instanceof long[]) {
            return toLittleEndianArray((long[]) d);
        }
        return null;
    }

    private static byte[] toLittleEndianArray(long[] d) {
        ByteBuffer b = ByteBuffer.allocate(d.length * 8);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.asLongBuffer().put(d);
        return b.array();
    }

    private static byte[] toLittleEndianArray(int[] d) {
        ByteBuffer b = ByteBuffer.allocate(d.length * 4);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.asIntBuffer().put(d);
        return b.array();
    }

    private static byte[] toLittleEndianArray(short[] d) {
        ByteBuffer b = ByteBuffer.allocate(d.length * 2);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.asShortBuffer().put(d);
        return b.array();
    }

    private static boolean isConstant(Value[] ops) {
        for (Value op : ops) {
            if (op.vt != VT.CONSTANT) {
                return false;
            }
        }
        return true;
    }

    private static void box(String provideType, String expectedType, MethodVisitor asm) {
        if (provideType.equals(expectedType)) {
            return;
        }
        if (expectedType.equals("V")) {
            switch (provideType.charAt(0)) {
            case 'J':
            case 'D':
                asm.visitInsn(POP2);
                break;
            default:
                asm.visitInsn(POP);
                break;
            }
            return;
        }

        char p = provideType.charAt(0);
        char e = expectedType.charAt(0);

        if (expectedType.equals("Ljava/lang/Object;") && (p == '[' || p == 'L')) {
            return;
        }
        if (provideType.equals("Ljava/lang/Object;") && (e == '[' || e == 'L')) {
            asm.visitTypeInsn(CHECKCAST, toInternal(expectedType));
            return;
        }

        switch (provideType + expectedType) {
        case "ZLjava/lang/Object;":
        case "ZLjava/lang/Boolean;":
            asm.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            break;
        case "BLjava/lang/Object;":
        case "BLjava/lang/Byte;":
            asm.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
            break;
        case "SLjava/lang/Object;":
        case "SLjava/lang/Short;":
            asm.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
            break;
        case "CLjava/lang/Object;":
        case "CLjava/lang/Character;":
            asm.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
            break;
        case "ILjava/lang/Object;":
        case "ILjava/lang/Integer;":
            asm.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            break;
        case "FLjava/lang/Object;":
        case "FLjava/lang/Float;":
            asm.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            break;
        case "JLjava/lang/Object;":
        case "JLjava/lang/Long;":
            asm.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            break;
        case "DLjava/lang/Object;":
        case "DLjava/lang/Double;":
            asm.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            break;

        case "Ljava/lang/Object;Z":
            asm.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            // pass through
        case "Ljava/lang/Boolean;Z":
            asm.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            break;


        case "Ljava/lang/Object;B":
            asm.visitTypeInsn(CHECKCAST, "java/lang/Byte");
            // pass through
        case "Ljava/lang/Byte;B":
            asm.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
            break;


        case "Ljava/lang/Object;S":
            asm.visitTypeInsn(CHECKCAST, "java/lang/Short");
            // pass through
        case "Ljava/lang/Short;S":
            asm.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
            break;


        case "Ljava/lang/Object;C":
            asm.visitTypeInsn(CHECKCAST, "java/lang/Character");
            // pass through
        case "Ljava/lang/Character;C":
            asm.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
            break;


        case "Ljava/lang/Object;I":
            asm.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            // pass through
        case "Ljava/lang/Integer;I":
            asm.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            break;

        case "Ljava/lang/Object;F":
            asm.visitTypeInsn(CHECKCAST, "java/lang/Float");
            // pass through
        case "Ljava/lang/Float;F":
            asm.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
            break;


        case "Ljava/lang/Object;J":
            asm.visitTypeInsn(CHECKCAST, "java/lang/Long");
            // pass through
        case "Ljava/lang/Long;J":
            asm.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            break;

        case "Ljava/lang/Object;D":
            asm.visitTypeInsn(CHECKCAST, "java/lang/Double");
            // pass through
        case "Ljava/lang/Double;D":
            asm.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
            break;

        default:
            throw new RuntimeException("i have trouble to auto convert from " + provideType
                    + " to " + expectedType + " currently");
        }
    }

    private void reBuildE1Expression(E1Expr e1, MethodVisitor asm) {
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
            default:
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
        case NOT: // fix issue#207 missing ~ bitwise complement operator
            switch (e1.getOp().valueType) {
            case "I": {
                asm.visitLdcInsn(-1);
                asm.visitInsn(getOpcode(e1, IXOR));
            }
            break;
            case "J": {
                asm.visitLdcInsn(-1L);
                asm.visitInsn(getOpcode(e1, IXOR));
            }
            break;
            default:
                break;
            }
            break;
        default:
            break;
        }
    }

    private void reBuildE2Expression(E2Expr e2, MethodVisitor asm) {
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
            default:
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
        default:
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
            default:
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
            default:
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
            default:
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
            default:
                break;
            }
            break;
        }
        default:
            break;
        }
    }

}
