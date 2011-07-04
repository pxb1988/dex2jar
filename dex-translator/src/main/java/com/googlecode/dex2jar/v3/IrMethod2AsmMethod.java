package com.googlecode.dex2jar.v3;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.LocalType;

public class IrMethod2AsmMethod implements Opcodes {
    public void convert(IrMethod ir, MethodNode asm) {
        asm.instructions.clear();
        asm.tryCatchBlocks.clear();
        int index = 0;
        if ((ir.access & ACC_STATIC) == 0) {
            index++;
        }
        InsnList il = asm.instructions;
        int ids[] = new int[ir.args.length];
        for (int i = 0; i < ir.args.length; i++) {
            ids[i] = index;
            index += ir.args[i].getSize();
        }
        for (Stmt st : ir.stmts) {
            switch (st.st) {
            case LABEL:
                asm.visitLabel(((LabelStmt) st).label);
                break;
            case ASSIGN:
                break;
            case GOTO:
            case IDENTITY:
            case IF: {
                Label target = ((JumpStmt) st).target.label;
                Value v = ((JumpStmt) st).op.value;
                Value v1 = ((E2Expr) v).op1.value;
                Value v2 = ((E2Expr) v).op2.value;
                accept(v1, asm);
                accept(v2, asm);

                switch (v.vt) {
                case NE:
                    asm.visitJumpInsn(IF_ICMPNE, target);
                    break;
                case EQ:
                    asm.visitJumpInsn(IF_ICMPEQ, target);
                    // TODO INT,OBJECT, detect ZERO/NULL
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
                // INT, detect ZERO
                case CMP:
                    asm.visitJumpInsn(IF_ICMPEQ, target);
                    break;
                case CMPG:
                    asm.visitJumpInsn(FCMPG, target);
                    break;
                case CMPL:
                    asm.visitJumpInsn(FCMPL, target);
                    break;
                // DOUBLE,FLOAT,OBJECT
                }
            }
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

    private static void accept(Value value, MethodNode asm) {
        // TODO Auto-generated method stub

    }
}
