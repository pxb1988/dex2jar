package com.googlecode.d2j.node.analysis;

import com.googlecode.d2j.node.insn.*;

import java.util.ArrayList;
import java.util.List;

public class DvmFrame<V> {
    public V[] values;
    public V tmp;

    public DvmFrame(int totalRegister) {
        values = (V[]) new Object[totalRegister];
    }


    public void set(int i, V v) {
        values[i] = v;
    }

    public void setTmp(V v) {
        this.tmp = v;
    }

    public DvmFrame<V> init(DvmFrame<? extends V> src) {
        this.tmp = src.tmp;
        System.arraycopy(src.values, 0, this.values, 0, this.values.length);
        return this;
    }

    public void execute(DexStmtNode insn, DvmInterpreter<V> interpreter) {
        if (insn.op == null) {// label or others
            return;
        }
        switch (insn.op) {
            case CONST:
            case CONST_4:
            case CONST_16:
            case CONST_HIGH16:
            case CONST_WIDE:
            case CONST_WIDE_16:
            case CONST_WIDE_32:
            case CONST_WIDE_HIGH16:
            case CONST_STRING:
            case CONST_STRING_JUMBO:
            case CONST_CLASS:
                set(((ConstStmtNode) insn).a, interpreter.newOperation(insn));
                break;
            case SGET:
            case SGET_BOOLEAN:
            case SGET_BYTE:
            case SGET_CHAR:
            case SGET_OBJECT:
            case SGET_SHORT:
            case SGET_WIDE:
                set(((FieldStmtNode) insn).a, interpreter.newOperation(insn));
                break;
            case NEW_INSTANCE:
                set(((TypeStmtNode) insn).a, interpreter.newOperation(insn));
                break;
            case MOVE:
            case MOVE_16:
            case MOVE_FROM16:
            case MOVE_OBJECT:
            case MOVE_OBJECT_16:
            case MOVE_OBJECT_FROM16:
            case MOVE_WIDE:
            case MOVE_WIDE_FROM16:
            case MOVE_WIDE_16:
                Stmt2RNode stmt2RNode = (Stmt2RNode) insn;
                set(stmt2RNode.a, interpreter.copyOperation(insn, get(stmt2RNode.b)));
                break;
            case MOVE_RESULT:
            case MOVE_RESULT_WIDE:
            case MOVE_RESULT_OBJECT:
            case MOVE_EXCEPTION:
                set(((Stmt1RNode) insn).a, interpreter.copyOperation(insn, getTmp()));
                break;
            case NOT_INT:
            case NOT_LONG:
            case NEG_DOUBLE:
            case NEG_FLOAT:
            case NEG_INT:
            case NEG_LONG:
            case INT_TO_BYTE:
            case INT_TO_CHAR:
            case INT_TO_DOUBLE:
            case INT_TO_FLOAT:
            case INT_TO_LONG:
            case INT_TO_SHORT:
            case FLOAT_TO_DOUBLE:
            case FLOAT_TO_INT:
            case FLOAT_TO_LONG:
            case DOUBLE_TO_FLOAT:
            case DOUBLE_TO_INT:
            case DOUBLE_TO_LONG:
            case LONG_TO_DOUBLE:
            case LONG_TO_FLOAT:
            case LONG_TO_INT:
            case ARRAY_LENGTH:
                Stmt2RNode stmt2RNode1 = (Stmt2RNode) insn;
                set(stmt2RNode1.a, interpreter.unaryOperation(insn, get(stmt2RNode1.b)));
                break;
            case IF_EQZ:
            case IF_GEZ:
            case IF_GTZ:
            case IF_LEZ:
            case IF_LTZ:
            case IF_NEZ:
                interpreter.unaryOperation(insn, get(((JumpStmtNode) insn).a));
                break;
            case SPARSE_SWITCH:
                interpreter.unaryOperation(insn, get(((SparseSwitchStmtNode) insn).a));
                break;
            case PACKED_SWITCH:
                interpreter.unaryOperation(insn, get(((PackedSwitchStmtNode) insn).a));
                break;
            case SPUT:
            case SPUT_BOOLEAN:
            case SPUT_BYTE:
            case SPUT_CHAR:
            case SPUT_OBJECT:
            case SPUT_SHORT:
            case SPUT_WIDE:
                interpreter.unaryOperation(insn, get(((FieldStmtNode) insn).a));
                break;
            case IGET:
            case IGET_BOOLEAN:
            case IGET_BYTE:
            case IGET_CHAR:
            case IGET_OBJECT:
            case IGET_SHORT:
            case IGET_WIDE:
                FieldStmtNode fieldStmtNode = (FieldStmtNode) insn;
                set(fieldStmtNode.a, interpreter.unaryOperation(insn, get(fieldStmtNode.b)));
                break;
            case NEW_ARRAY:
            case CHECK_CAST:
            case INSTANCE_OF:
                TypeStmtNode typeStmtNode = (TypeStmtNode) insn;
                set(typeStmtNode.a, interpreter.unaryOperation(insn, get(typeStmtNode.b)));
                break;
            case MONITOR_ENTER:
            case MONITOR_EXIT:
            case THROW:
                interpreter.unaryOperation(insn, get(((Stmt1RNode) insn).a));
                break;
            case RETURN:
            case RETURN_WIDE:
            case RETURN_OBJECT:
                interpreter.returnOperation(insn, get(((Stmt1RNode) insn).a));
                break;
            case RETURN_VOID:
                interpreter.returnOperation(insn, null);
                break;
            case AGET:
            case AGET_BOOLEAN:
            case AGET_BYTE:
            case AGET_CHAR:
            case AGET_OBJECT:
            case AGET_SHORT:
            case AGET_WIDE:
            case CMP_LONG:
            case CMPG_DOUBLE:
            case CMPG_FLOAT:
            case CMPL_DOUBLE:
            case CMPL_FLOAT:
            case ADD_DOUBLE:
            case ADD_FLOAT:
            case ADD_INT:
            case ADD_LONG:
            case SUB_DOUBLE:
            case SUB_FLOAT:
            case SUB_INT:
            case SUB_LONG:
            case MUL_DOUBLE:
            case MUL_FLOAT:
            case MUL_INT:
            case MUL_LONG:
            case DIV_DOUBLE:
            case DIV_FLOAT:
            case DIV_INT:
            case DIV_LONG:
            case REM_DOUBLE:
            case REM_FLOAT:
            case REM_INT:
            case REM_LONG:
            case AND_INT:
            case AND_LONG:
            case OR_INT:
            case OR_LONG:
            case XOR_INT:
            case XOR_LONG:
            case SHL_INT:
            case SHL_LONG:
            case SHR_INT:
            case SHR_LONG:
            case USHR_INT:
            case USHR_LONG:
                Stmt3RNode stmt3RNode = (Stmt3RNode) insn;
                set(stmt3RNode.a, interpreter.binaryOperation(insn, get(stmt3RNode.b), get(stmt3RNode.c)));
                break;
            case IF_EQ:
            case IF_GE:
            case IF_GT:
            case IF_LE:
            case IF_LT:
            case IF_NE:
                JumpStmtNode jumpStmtNode = (JumpStmtNode) insn;
                interpreter.binaryOperation(insn, get(jumpStmtNode.a), get(jumpStmtNode.b));
                break;
            case IPUT:
            case IPUT_BOOLEAN:
            case IPUT_BYTE:
            case IPUT_CHAR:
            case IPUT_OBJECT:
            case IPUT_SHORT:
            case IPUT_WIDE:
                FieldStmtNode fieldStmtNode1 = (FieldStmtNode) insn;
                interpreter.binaryOperation(insn, get(fieldStmtNode1.b), get(fieldStmtNode1.a));
                break;
            case APUT:
            case APUT_BOOLEAN:
            case APUT_BYTE:
            case APUT_CHAR:
            case APUT_OBJECT:
            case APUT_SHORT:
            case APUT_WIDE:
                Stmt3RNode stmt3RNode1 = (Stmt3RNode) insn;
                interpreter.ternaryOperation(insn, get(stmt3RNode1.b), get(stmt3RNode1.c), get(stmt3RNode1.a));
                break;
            case INVOKE_VIRTUAL_RANGE:
            case INVOKE_VIRTUAL:
            case INVOKE_SUPER_RANGE:
            case INVOKE_DIRECT_RANGE:
            case INVOKE_SUPER:
            case INVOKE_DIRECT:
            case INVOKE_STATIC_RANGE:
            case INVOKE_STATIC:
            case INVOKE_INTERFACE_RANGE:
            case INVOKE_INTERFACE: { // TODO skip for J/D
                MethodStmtNode methodStmtNode = (MethodStmtNode) insn;
                List<V> v = new ArrayList<>(methodStmtNode.args.length);
                for (int i = 0; i < methodStmtNode.args.length; i++) {
                    v.add(get(methodStmtNode.args[i]));
                }
                setTmp(interpreter.naryOperation(insn, v));
            }
            break;
            case FILLED_NEW_ARRAY:
            case FILLED_NEW_ARRAY_RANGE: {
                FilledNewArrayStmtNode filledNewArrayStmtNode = (FilledNewArrayStmtNode) insn;
                List<V> v = new ArrayList<>(filledNewArrayStmtNode.args.length);
                for (int i = 0; i < filledNewArrayStmtNode.args.length; i++) {
                    v.add(get(filledNewArrayStmtNode.args[i]));
                }
                setTmp(interpreter.naryOperation(insn, v));
            }
            break;


            case ADD_DOUBLE_2ADDR:
            case ADD_FLOAT_2ADDR:
            case ADD_INT_2ADDR:
            case ADD_LONG_2ADDR:
            case SUB_DOUBLE_2ADDR:
            case SUB_FLOAT_2ADDR:
            case SUB_INT_2ADDR:
            case SUB_LONG_2ADDR:
            case MUL_DOUBLE_2ADDR:
            case MUL_FLOAT_2ADDR:
            case MUL_INT_2ADDR:
            case MUL_LONG_2ADDR:
            case DIV_DOUBLE_2ADDR:
            case DIV_FLOAT_2ADDR:
            case DIV_INT_2ADDR:
            case DIV_LONG_2ADDR:
            case REM_DOUBLE_2ADDR:
            case REM_FLOAT_2ADDR:
            case REM_INT_2ADDR:
            case REM_LONG_2ADDR:
            case AND_INT_2ADDR:
            case AND_LONG_2ADDR:
            case OR_INT_2ADDR:
            case OR_LONG_2ADDR:
            case XOR_INT_2ADDR:
            case XOR_LONG_2ADDR:
            case SHL_INT_2ADDR:
            case SHL_LONG_2ADDR:
            case SHR_INT_2ADDR:
            case SHR_LONG_2ADDR:
            case USHR_INT_2ADDR:
            case USHR_LONG_2ADDR:
                Stmt2RNode stmt2RNode2 = (Stmt2RNode) insn;
                set(stmt2RNode2.a, interpreter.binaryOperation(insn, get(stmt2RNode2.a), get(stmt2RNode2.b)));
                break;
            case ADD_INT_LIT16:
            case ADD_INT_LIT8:
            case RSUB_INT_LIT8:
            case RSUB_INT:
            case MUL_INT_LIT8:
            case MUL_INT_LIT16:
            case DIV_INT_LIT16:
            case DIV_INT_LIT8:
            case REM_INT_LIT16:
            case REM_INT_LIT8:
            case AND_INT_LIT16:
            case AND_INT_LIT8:
            case OR_INT_LIT16:
            case OR_INT_LIT8:
            case XOR_INT_LIT16:
            case XOR_INT_LIT8:
            case SHL_INT_LIT8:
            case SHR_INT_LIT8:
            case USHR_INT_LIT8:
                Stmt2R1NNode stmt2R1NNode = (Stmt2R1NNode) insn;
                set(stmt2R1NNode.distReg, interpreter.unaryOperation(insn, get(stmt2R1NNode.srcReg)));
                break;
            case BAD_OP:
                break;
            default:
                throw new RuntimeException();
        }
    }

    private V getTmp() {
        return tmp;
    }

    private V get(int b) {
        return values[b];
    }

    public int getTotalRegisters() {
        return values.length;
    }

    public V getLocal(int i) {
        return get(i);
    }

    public void setLocal(int i, V q) {
        set(i, q);
    }
}
