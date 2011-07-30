package com.googlecode.dex2jar.v3;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.ir.Constant;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.NewMutiArrayExpr;
import com.googlecode.dex2jar.ir.expr.RefExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.Cfg;
import com.googlecode.dex2jar.ir.ts.Cfg.StmtVisitor;
import com.googlecode.dex2jar.ir.ts.LocalType;

public class IrMethod2AsmMethod implements Opcodes {

    private void reIndexLocal(IrMethod ir) {
        int index = 0;
        if ((ir.access & ACC_STATIC) == 0) {
            index++;
        }
        final int ids[] = new int[ir.args.length];
        for (int i = 0; i < ir.args.length; i++) {
            ids[i] = index;
            index += ir.args[i].getSize();
        }
        for (Local local : ir.locals) {
            local._ls_index = -1;
        }

        final int[] indexHolder = new int[] { index };
        Cfg.createCFG(ir);//
        Cfg.Forward(ir, new StmtVisitor<Object>() {
            @Override
            public Object exec(Stmt stmt) {
                switch (stmt.st) {
                case ASSIGN:
                case IDENTITY:
                    if (((AssignStmt) stmt).op1.value.vt == VT.LOCAL) {
                        Local local = (Local) ((AssignStmt) stmt).op1.value;
                        if (local._ls_index == -1) {
                            Type localType = LocalType.type(local);
                            if (!Type.VOID_TYPE.equals(localType)) {// skip void type
                                Value ref = (Value) ((AssignStmt) stmt).op2.value;
                                switch (ref.vt) {
                                case THIS_REF:
                                    local._ls_index = 0;
                                    break;
                                case PARAMETER_REF:
                                    local._ls_index = ids[((RefExpr) ref).parameterIndex];
                                    break;
                                case EXCEPTION_REF:
                                    local._ls_index = indexHolder[0]++;
                                    break;
                                default:
                                    local._ls_index = indexHolder[0];
                                    indexHolder[0] += LocalType.type(ref).getSize();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
                return null;
            }
        });
    }

    public void convert(IrMethod ir, MethodNode asm) {

        reIndexLocal(ir);

        asm.instructions.clear();
        reBuildInstructions(ir, asm);

        asm.tryCatchBlocks.clear();
        reBuildTryCatchBlocks(ir, asm);

    }

    private void reBuildTryCatchBlocks(IrMethod ir, MethodNode asm) {
        for (Trap trap : ir.traps) {
            asm.visitTryCatchBlock(trap.start.label, trap.end.label, trap.handler.label, trap.type == null ? null
                    : trap.type.getInternalName());
        }
    }

    private void reBuildInstructions(IrMethod ir, MethodNode asm) {
        for (Stmt st : ir.stmts) {
            switch (st.st) {
            case LABEL:
                asm.visitLabel(((LabelStmt) st).label);
                break;
            case ASSIGN: {
                E2Stmt e2 = (E2Stmt) st;
                Value v1 = e2.op1.value;
                Value v2 = e2.op2.value;
                switch (v1.vt) {
                case LOCAL:
                    accept(v2, asm);
                    int i = ((Local) v1)._ls_index;
                    if (i >= 0) {// skip void type locals
                        asm.visitVarInsn(LocalType.type(v1).getOpcode(ISTORE), i);
                    }
                    break;
                case FIELD:
                    FieldExpr fe = (FieldExpr) v1;
                    if (fe.op == null) {// static field
                        accept(v2, asm);
                        asm.visitFieldInsn(PUTSTATIC, fe.fieldOwnerType.getInternalName(), fe.fieldName,
                                fe.fieldType.getDescriptor());
                    } else {// virtual field
                        accept(fe.op.value, asm);
                        accept(v2, asm);
                        asm.visitFieldInsn(PUTFIELD, fe.fieldOwnerType.getInternalName(), fe.fieldName,
                                fe.fieldType.getDescriptor());
                    }
                    break;
                case ARRAY:
                    ArrayExpr ae = (ArrayExpr) v1;
                    accept(ae.op1.value, asm);
                    accept(ae.op2.value, asm);
                    accept(v2, asm);
                    asm.visitInsn(LocalType.type(ae.op2.value).getOpcode(IASTORE));
                    break;
                }
            }
                break;
            case IDENTITY: {
                E2Stmt e2 = (E2Stmt) st;
                if (e2.op2.value.vt == VT.EXCEPTION_REF) {
                    asm.visitVarInsn(ASTORE, ((Local) e2.op1.value)._ls_index);
                }
            }
                break;
            case GOTO:
                asm.visitJumpInsn(GOTO, ((JumpStmt) st).target.label);
                break;
            case IF:
                reBuildJumpInstructions((JumpStmt) st, asm);
                break;
            case LOCK:
                accept(((UnopStmt) st).op.value, asm);
                asm.visitInsn(MONITORENTER);
                break;
            case UNLOCK:
                accept(((UnopStmt) st).op.value, asm);
                asm.visitInsn(MONITOREXIT);
                break;
            case NOP:
                break;
            case RETURN: {
                Value v = ((UnopStmt) st).op.value;
                accept(v, asm);
                asm.visitInsn(LocalType.type(v).getOpcode(IRETURN));
            }
                break;
            case RETURN_VOID:
                asm.visitInsn(RETURN);
                break;
            case LOOKUP_SWITCH: {
                LookupSwitchStmt lss = (LookupSwitchStmt) st;
                accept(lss.op.value, asm);
                Label targets[] = new Label[lss.targets.length];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = lss.targets[i].label;
                }
                asm.visitLookupSwitchInsn(lss.defaultTarget.label, lss.lookupValues, targets);
            }
                break;
            case TABLE_SWITCH: {
                TableSwitchStmt tss = (TableSwitchStmt) st;
                accept(tss.op.value, asm);
                Label targets[] = new Label[tss.targets.length];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = tss.targets[i].label;
                }
                asm.visitTableSwitchInsn(tss.lowIndex, tss.highIndex, tss.defaultTarget.label, targets);
            }
                break;
            case THROW:
                accept(((UnopStmt) st).op.value, asm);
                asm.visitInsn(ATHROW);
                break;
            }
        }
    }

    private void reBuildJumpInstructions(JumpStmt st, MethodNode asm) {

        Label target = st.target.label;
        Value v = st.op.value;
        Value v1 = ((E2Expr) v).op1.value;
        Value v2 = ((E2Expr) v).op2.value;

        Type type = LocalType.type(v1);

        switch (type.getSort()) {
        case Type.INT:
        case Type.BYTE:
        case Type.CHAR:
        case Type.SHORT:
        case Type.BOOLEAN:
            // IFx
            // IF_ICMPx
            if ((v1.vt == VT.CONSTANT && ((Constant) v1).value.equals(new Integer(0)))
                    || (v2.vt == VT.CONSTANT && ((Constant) v2).value.equals(new Integer(0)))) { // IFx
                if ((v2.vt == VT.CONSTANT && ((Constant) v2).value.equals(new Integer(0)))) {// v2 is zero
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
        case Type.ARRAY:
        case Type.OBJECT:
            // IF_ACMPx
            // IF[non]null

            if ((v1.vt == VT.CONSTANT && ((Constant) v1).value.equals(Constant.Null))
                    || (v2.vt == VT.CONSTANT && ((Constant) v2).value.equals(Constant.Null))) { // IF[non]null
                if ((v2.vt == VT.CONSTANT && ((Constant) v2).value.equals(Constant.Null))) {// v2 is null
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
        }
    }

    private static void accept(Value value, MethodNode asm) {

        switch (value.et) {
        case E0:
            switch (value.vt) {
            case LOCAL:
                asm.visitVarInsn(LocalType.type(value).getOpcode(ILOAD), ((Local) value)._ls_index);
                break;
            case CONSTANT:
                Constant cst = (Constant) value;
                if (cst.value.equals(Constant.Null)) {
                    asm.visitInsn(ACONST_NULL);
                } else {
                    asm.visitLdcInsn(cst.value);
                }
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

    private static void reBuildEnExpression(EnExpr value, MethodNode asm) {
        if (value.vt == VT.INVOKE_NEW) {
            asm.visitTypeInsn(NEW, ((InvokeExpr) value).methodOwnerType.getInternalName());
            asm.visitInsn(DUP);
        }

        for (ValueBox vb : value.ops) {
            accept(vb.value, asm);
        }
        switch (value.vt) {
        case NEW_MUTI_ARRAY:
            NewMutiArrayExpr nmae = (NewMutiArrayExpr) value;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nmae.dimension; i++) {
                sb.append('[');
            }
            sb.append(nmae.baseType.getDescriptor());
            asm.visitMultiANewArrayInsn(sb.toString(), nmae.dimension);
            break;
        case INVOKE_INTERFACE:
        case INVOKE_NEW:
        case INVOKE_SPECIAL:
        case INVOKE_STATIC:
        case INVOKE_VIRTUAL:
            InvokeExpr ie = (InvokeExpr) value;
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

            asm.visitMethodInsn(opcode, ie.methodOwnerType.getInternalName(), ie.methodName, Type.getMethodDescriptor(
                    ie.vt == VT.INVOKE_NEW ? Type.VOID_TYPE : ie.methodReturnType, ie.argmentTypes));
            break;
        }
    }

    private static void reBuildE1Expression(E1Expr e1, MethodNode asm) {
        if (e1.op != null) {// the op is null if GETSTATIC
            accept(e1.op.value, asm);
        }
        switch (e1.vt) {
        case FIELD:
            FieldExpr fe = (FieldExpr) e1;
            asm.visitFieldInsn(e1.op == null ? GETSTATIC : GETFIELD, fe.fieldOwnerType.getInternalName(), fe.fieldName,
                    fe.fieldType.getDescriptor());
            break;
        case NEW_ARRAY:
        case CHECK_CAST:
        case INSTANCE_OF: {
            TypeExpr te = (TypeExpr) e1;
            asm.visitTypeInsn(e1.vt == VT.NEW_ARRAY ? NEWARRAY : e1.vt == VT.CHECK_CAST ? CHECKCAST : INSTANCEOF,
                    te.type.getInternalName());
        }
            break;
        case CAST: {
            TypeExpr te = (TypeExpr) e1;
            cast2(LocalType.type(e1.op.value), te.type, asm);
        }
            break;
        case LENGTH:
            asm.visitInsn(ARRAYLENGTH);
            break;
        case NEG:
            asm.visitInsn(LocalType.type(e1).getOpcode(INEG));
            break;
        }
    }

    private static void reBuildE2Expression(E2Expr e2, MethodNode asm) {
        accept(e2.op1.value, asm);
        accept(e2.op2.value, asm);
        Type type = LocalType.type(e2.op2.value);
        switch (e2.vt) {
        case ARRAY:
            asm.visitInsn(type.getOpcode(IALOAD));
            break;
        case ADD:
            asm.visitInsn(type.getOpcode(IADD));
            break;
        case SUB:
            asm.visitInsn(type.getOpcode(ISUB));
            break;
        case DIV:
            asm.visitInsn(type.getOpcode(IDIV));
            break;
        case MUL:
            asm.visitInsn(type.getOpcode(IMUL));
            break;
        case REM:
            asm.visitInsn(type.getOpcode(IREM));
            break;
        case AND:
            asm.visitInsn(type.getOpcode(IAND));
            break;
        case OR:
            asm.visitInsn(type.getOpcode(IOR));
            break;
        case XOR:
            asm.visitInsn(type.getOpcode(IXOR));
            break;

        case SHL:
            asm.visitInsn(type.getOpcode(ISHL));
            break;
        case SHR:
            asm.visitInsn(type.getOpcode(ISHR));
            break;
        case USHR:
            asm.visitInsn(type.getOpcode(IUSHR));
            break;
        case CMP:
            asm.visitInsn(LCMP);
            break;
        case CMPG:
            asm.visitInsn(type.getSort() == Type.FLOAT ? FCMPG : DCMPG);
        case CMPL:
            asm.visitInsn(type.getSort() == Type.FLOAT ? FCMPL : DCMPL);
            break;
        }
    }

    private static void cast2(Type t1, Type t2, MethodNode asm) {
        if (t1.equals(t2)) {
            return;
        }

        // char 2
        // byte 3
        // short 4
        // int 5
        // float 6
        // long 7
        // double 8

        // int I2L = 133; // visitInsn
        // int I2F = 134; // -
        // int I2D = 135; // -
        // int L2I = 136; // -
        // int L2F = 137; // -
        // int L2D = 138; // -
        // int F2I = 139; // -
        // int F2L = 140; // -
        // int F2D = 141; // -
        // int D2I = 142; // -
        // int D2L = 143; // -
        // int D2F = 144; // -
        // int I2B = 145; // -
        // int I2C = 146; // -
        // int I2S = 147; // -

        int opcode;
        switch (t1.getSort() * 10 + t2.getSort()) {
        case 56:
            opcode = I2F;
            break;
        case 57:
            opcode = I2L;
            break;
        case 58:
            opcode = I2D;
            break;
        case 52:
            opcode = I2C;
            break;
        case 53:
            opcode = I2B;
            break;
        case 54:
            opcode = I2S;
            break;
        case 75:
            opcode = L2I;
            break;
        case 76:
            opcode = L2F;
            break;
        case 78:
            opcode = L2D;
            break;

        case 65:
            opcode = F2I;
            break;
        case 67:
            opcode = F2L;
            break;
        case 68:
            opcode = F2D;
            break;
        case 85:
            opcode = D2I;
            break;
        case 86:
            opcode = D2F;
            break;
        case 87:
            opcode = D2L;
            break;
        default:
            opcode = -1;
            break;
        }
        if (opcode == -1) {
            throw new DexException("can't cast %s to %s", t1, t2);
        }
        asm.visitInsn(opcode);
    }
}
