package com.googlecode.dex2jar.reader;

import java.util.Map;

import org.objectweb.asm.Label;

import com.googlecode.dex2jar.Dex;
import com.googlecode.dex2jar.DexInternalOpcode;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.visitors.NewDexCoderVisitor;

public class NewOpcodeAdapter implements DexOpcodes, DexInternalOpcode {
    private NewDexCoderVisitor dcv;
    private Dex dex;

    private Map<Integer, Label> labels;
    private int offset;

    /**
     * @param dex
     * @param labels
     */
    public NewOpcodeAdapter(Dex dex, Map<Integer, Label> labels, NewDexCoderVisitor dcv) {
        super();
        this.dex = dex;
        this.labels = labels;
        this.dcv = dcv;
    }

    private Label getLabel(int offset) {
        return labels.get(this.offset + offset * 2);
    }

    public void offset(int currentOffset) {
        this.offset = currentOffset;
    }

    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {
        dcv.visitFillArrayStmt(opcode, aA, elemWidth, initLength, values);
    }

    public void visitLookupSwitchStmt(int opcode, int aA, int defaultOffset, int[] cases, int[] iLabel) {
        Label[] labels = new Label[iLabel.length];
        for (int i = 0; i < iLabel.length; i++) {
            labels[i] = getLabel(iLabel[i]);
        }
        dcv.visitLookupSwitchStmt(opcode, aA, getLabel(defaultOffset), cases, labels);
    }

    public void visitTableSwitchStmt(int opcode, int aA, int defaultOffset, int first_case, int last_case, int[] iLabel) {
        Label[] labels = new Label[iLabel.length];
        for (int i = 0; i < iLabel.length; i++) {
            labels[i] = getLabel(iLabel[i]);
        }
        dcv.visitTableSwitchStmt(opcode, aA, getLabel(defaultOffset), first_case, last_case, labels);
    }

    /**
     * <pre>
     * OP_GOTO 
     * OP_GOTO_16 
     * OP_GOTO_32
     * </pre>
     * 
     * @param opcode
     * @param offset
     */
    public void x0t(int opcode, int offset) {
        switch (opcode) {
        case OP_GOTO:
        case OP_GOTO_16:
        case OP_GOTO_32:
            dcv.visitJumpStmt(OP_GOTO, getLabel(offset));
            break;
        default:
            throw new RuntimeException("");
        }

    }

    /**
     * <pre>
     * OP_NOP
     * OP_RETURN_VOID
     * </pre>
     * 
     * @param opcode
     */
    public void x0x(int opcode) {
        switch (opcode) {
        case OP_NOP:
            break;
        case OP_RETURN_VOID:
            dcv.visitReturnStmt(opcode);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1c(int opcode, int A, int B) {
        switch (opcode) {
        case OP_CONST_STRING:
        case OP_CONST_STRING_JUMBO:
            dcv.visitConstStmt(OP_CONST_STRING, A, dex.getString(B));
            break;
        case OP_CONST_CLASS:
            dcv.visitConstStmt(OP_CONST_CLASS, A, dex.getType(B));
            break;
        case OP_CHECK_CAST:
        case OP_NEW_INSTANCE:
            dcv.visitClassStmt(OP_CHECK_CAST, A, dex.getType(B));
            break;
        case OP_SGET:
        case OP_SGET_WIDE:
        case OP_SGET_OBJECT:
        case OP_SGET_BOOLEAN:
        case OP_SGET_BYTE:
        case OP_SGET_CHAR:
        case OP_SGET_SHORT:
            dcv.visitFieldStmt(OP_SGET, A, dex.getField(B));
            break;
        case OP_SPUT:
        case OP_SPUT_WIDE:
        case OP_SPUT_OBJECT:
        case OP_SPUT_BOOLEAN:
        case OP_SPUT_BYTE:
        case OP_SPUT_CHAR:
        case OP_SPUT_SHORT:
            dcv.visitFieldStmt(OP_SPUT, A, dex.getField(B));
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1h(int opcode, int A, int B) {
        switch (opcode) {
        case OP_CONST_HIGH16:
            dcv.visitConstStmt(OP_CONST, A, B << 16);
            break;
        case OP_CONST_WIDE_HIGH16:
            dcv.visitConstStmt(OP_CONST_WIDE, A, ((long) B) << 48);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1i(int opcode, int aA, int bBBBBBBB) {
        switch (opcode) {
        case OP_CONST:
            dcv.visitConstStmt(opcode, aA, bBBBBBBB);
            break;
        case OP_CONST_WIDE_32:
            dcv.visitConstStmt(OP_CONST_WIDE, aA, (long) bBBBBBBB);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1l(int opcode, int aA, long bBBBBBBB_BBBBBBBB) {
        switch (opcode) {
        case OP_CONST_WIDE:
            dcv.visitConstStmt(opcode, aA, bBBBBBBB_BBBBBBBB);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    /**
     * OP_CONST_4
     * 
     * @param opcode
     * @param A
     * @param B
     */
    public void x1n(int opcode, int A, int B) {
        switch (opcode) {
        case OP_CONST_4:
            dcv.visitConstStmt(OP_CONST, A, B);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1s(int opcode, int A, int B) {
        switch (opcode) {
        case OP_CONST_16:
            dcv.visitConstStmt(OP_CONST, A, B);
            break;
        case OP_CONST_WIDE_16:
            dcv.visitConstStmt(OP_CONST_WIDE, A, (long) B);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1t(int opcode, int A, int offset) {
        switch (opcode) {
        case OP_IF_EQZ:
        case OP_IF_NEZ:
        case OP_IF_LTZ:
        case OP_IF_GEZ:
        case OP_IF_GTZ:
        case OP_IF_LEZ:
            dcv.visitJumpStmt(opcode, A, getLabel(offset));
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1x(int opcode, int A) {
        switch (opcode) {
        case OP_MOVE_RESULT:
        case OP_MOVE_RESULT_WIDE:
        case OP_MOVE_RESULT_OBJECT:
        case OP_MOVE_EXCEPTION:
            dcv.visitMoveStmt(opcode, A);
            break;
        case OP_RETURN:
        case OP_RETURN_WIDE:
        case OP_RETURN_OBJECT:
            dcv.visitReturnStmt(OP_RETURN, A);
            break;
        case OP_THROW:
            dcv.visitReturnStmt(opcode, A);
            break;
        case OP_MONITOR_ENTER:
        case OP_MONITOR_EXIT:
            dcv.visitMonitorStmt(opcode, A);
        default:
            throw new RuntimeException("");
        }
    }

    public void x2b(int opcode, int aA, int bB, int cC) {
        switch (opcode) {
        case OP_ADD_INT_LIT8:
        case OP_RSUB_INT_LIT8:
        case OP_MUL_INT_LIT8:
        case OP_DIV_INT_LIT8:
        case OP_REM_INT_LIT8:
        case OP_AND_INT_LIT8:
        case OP_OR_INT_LIT8:
        case OP_XOR_INT_LIT8:
        case OP_SHL_INT_LIT8:
        case OP_SHR_INT_LIT8:
        case OP_USHR_INT_LIT8:
            dcv.visitBinopLitXStmt(opcode - (OP_ADD_INT_LIT8 - OP_ADD_INT_LIT_X), aA, bB, cC);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2c(int opcode, int a, int b, int cCCC) {
        switch (opcode) {
        case OP_INSTANCE_OF:
        case OP_NEW_ARRAY:
            dcv.visitClassStmt(opcode, a, b, dex.getType(cCCC));
            break;
        case OP_IGET:
        case OP_IGET_WIDE:
        case OP_IGET_OBJECT:
        case OP_IGET_BOOLEAN:
        case OP_IGET_BYTE:
        case OP_IGET_CHAR:
        case OP_IGET_SHORT:
            dcv.visitFieldStmt(OP_IGET, a, b, dex.getField(cCCC));
            break;
        case OP_IPUT:
        case OP_IPUT_WIDE:
        case OP_IPUT_OBJECT:
        case OP_IPUT_BOOLEAN:
        case OP_IPUT_BYTE:
        case OP_IPUT_CHAR:
        case OP_IPUT_SHORT:
            dcv.visitFieldStmt(OP_IPUT, a, b, dex.getField(cCCC));
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2s(int opcode, int a, int b, int cCCC) {
        switch (opcode) {
        case OP_ADD_INT_LIT16:
        case OP_RSUB_INT:
        case OP_MUL_INT_LIT16:
        case OP_DIV_INT_LIT16:
        case OP_REM_INT_LIT16:
        case OP_AND_INT_LIT16:
        case OP_OR_INT_LIT16:
        case OP_XOR_INT_LIT16:
            dcv.visitBinopLitXStmt(opcode - (OP_ADD_INT_LIT16 - OP_ADD_INT_LIT_X), a, b, cCCC);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2t(int opcode, int a, int b, int cCCC) {
        switch (opcode) {
        case OP_IF_EQ:
        case OP_IF_NE:
        case OP_IF_LT:
        case OP_IF_GE:
        case OP_IF_GT:
        case OP_IF_LE:
            dcv.visitJumpStmt(opcode, a, b, getLabel(cCCC));
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2x(int opcode, int A, int B) {
        switch (opcode) {
        case OP_MOVE:
        case OP_MOVE_FROM16:
        case OP_MOVE_16:
            dcv.visitMoveStmt(OP_MOVE, A, B);
            break;
        case OP_MOVE_WIDE:
        case OP_MOVE_WIDE_FROM16:
        case OP_MOVE_WIDE_16:
            dcv.visitMoveStmt(OP_MOVE_WIDE, A, B);
            break;
        case OP_MOVE_OBJECT:
        case OP_MOVE_OBJECT_FROM16:
        case OP_MOVE_OBJECT_16:
            dcv.visitMoveStmt(OP_MOVE_OBJECT, A, B);
            break;
        case OP_ARRAY_LENGTH:
            dcv.visitUnopStmt(OP_ARRAY_LENGTH, A, B);
            break;
        case OP_NEG_INT:
        case OP_NOT_INT:
        case OP_NEG_LONG:
        case OP_NOT_LONG:
        case OP_NEG_FLOAT:
        case OP_NEG_DOUBLE:
        case OP_INT_TO_LONG:
        case OP_INT_TO_FLOAT:
        case OP_INT_TO_DOUBLE:
        case OP_LONG_TO_INT:
        case OP_LONG_TO_FLOAT:
        case OP_LONG_TO_DOUBLE:
        case OP_FLOAT_TO_INT:
        case OP_FLOAT_TO_LONG:
        case OP_FLOAT_TO_DOUBLE:
        case OP_DOUBLE_TO_INT:
        case OP_DOUBLE_TO_LONG:
        case OP_DOUBLE_TO_FLOAT:
        case OP_INT_TO_BYTE:
        case OP_INT_TO_CHAR:
        case OP_INT_TO_SHORT:
            dcv.visitUnopStmt(opcode, A, B);
            break;
        case OP_ADD_INT_2ADDR:
        case OP_SUB_INT_2ADDR:
        case OP_MUL_INT_2ADDR:
        case OP_DIV_INT_2ADDR:
        case OP_REM_INT_2ADDR:
        case OP_AND_INT_2ADDR:
        case OP_OR_INT_2ADDR:
        case OP_XOR_INT_2ADDR:
        case OP_SHL_INT_2ADDR:
        case OP_SHR_INT_2ADDR:
        case OP_USHR_INT_2ADDR:
        case OP_ADD_LONG_2ADDR:
        case OP_SUB_LONG_2ADDR:
        case OP_MUL_LONG_2ADDR:
        case OP_DIV_LONG_2ADDR:
        case OP_REM_LONG_2ADDR:
        case OP_AND_LONG_2ADDR:
        case OP_OR_LONG_2ADDR:
        case OP_XOR_LONG_2ADDR:
        case OP_SHL_LONG_2ADDR:
        case OP_SHR_LONG_2ADDR:
        case OP_USHR_LONG_2ADDR:
        case OP_ADD_FLOAT_2ADDR:
        case OP_SUB_FLOAT_2ADDR:
        case OP_MUL_FLOAT_2ADDR:
        case OP_DIV_FLOAT_2ADDR:
        case OP_REM_FLOAT_2ADDR:
        case OP_ADD_DOUBLE_2ADDR:
        case OP_SUB_DOUBLE_2ADDR:
        case OP_MUL_DOUBLE_2ADDR:
        case OP_DIV_DOUBLE_2ADDR:
        case OP_REM_DOUBLE_2ADDR:
            dcv.visitBinopStmt(opcode - (OP_ADD_INT_2ADDR - OP_ADD_INT), A, A, B);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x3x(int opcode, int aA, int bB, int cC) {
        switch (opcode) {
        case OP_CMPL_FLOAT:
        case OP_CMPG_FLOAT:
        case OP_CMPL_DOUBLE:
        case OP_CMPG_DOUBLE:
        case OP_CMP_LONG:
            dcv.visitCmpStmt(opcode, aA, bB, cC);
            break;

        case OP_AGET:
        case OP_AGET_WIDE:
        case OP_AGET_OBJECT:
        case OP_AGET_BOOLEAN:
        case OP_AGET_BYTE:
        case OP_AGET_CHAR:
        case OP_AGET_SHORT:
            dcv.visitArrayStmt(OP_AGET, aA, bB, cC);
            break;
        case OP_APUT:
        case OP_APUT_WIDE:
        case OP_APUT_OBJECT:
        case OP_APUT_BOOLEAN:
        case OP_APUT_BYTE:
        case OP_APUT_CHAR:
        case OP_APUT_SHORT:
            dcv.visitArrayStmt(OP_APUT, aA, bB, cC);
            break;
        case OP_ADD_INT:
        case OP_SUB_INT:
        case OP_MUL_INT:
        case OP_DIV_INT:
        case OP_REM_INT:
        case OP_AND_INT:
        case OP_OR_INT:
        case OP_XOR_INT:
        case OP_SHL_INT:
        case OP_SHR_INT:
        case OP_USHR_INT:
        case OP_ADD_LONG:
        case OP_SUB_LONG:
        case OP_MUL_LONG:
        case OP_DIV_LONG:
        case OP_REM_LONG:
        case OP_AND_LONG:
        case OP_OR_LONG:
        case OP_XOR_LONG:
        case OP_SHL_LONG:
        case OP_SHR_LONG:
        case OP_USHR_LONG:
        case OP_ADD_FLOAT:
        case OP_SUB_FLOAT:
        case OP_MUL_FLOAT:
        case OP_DIV_FLOAT:
        case OP_REM_FLOAT:
        case OP_ADD_DOUBLE:
        case OP_SUB_DOUBLE:
        case OP_MUL_DOUBLE:
        case OP_DIV_DOUBLE:
        case OP_REM_DOUBLE:
            dcv.visitBinopStmt(opcode, aA, bB, cC);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x5c(int opcode, int b, int d, int e, int f, int g, int a, int cCCC) {
        int args[];
        switch (b) {
        case 1:
            args = new int[] { d };
            break;
        case 2:
            args = new int[] { d, e };
            break;
        case 3:
            args = new int[] { d, e, f };
            break;
        case 4:
            args = new int[] { d, e, f, g };
            break;
        case 5:
            args = new int[] { d, e, f, g, a };
            break;
        default:
            throw new RuntimeException("");
        }
        switch (opcode) {
        case OP_FILLED_NEW_ARRAY:
            dcv.visitFilledNewArrayStmt(opcode, args, dex.getType(cCCC));
            break;
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_SUPER:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_STATIC:
        case OP_INVOKE_INTERFACE:
            dcv.visitMethodStmt(opcode, args, dex.getMethod(cCCC));
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void xrc(int opcode, int cCCC, int aA, int bBBB) {
        int args[] = new int[aA];
        for (int i = 0; i < aA; i++) {
            args[i] = cCCC + i;
        }
        switch (opcode) {
        case OP_FILLED_NEW_ARRAY_RANGE:
            dcv.visitFilledNewArrayStmt(OP_FILLED_NEW_ARRAY, args, dex.getType(bBBB));
            break;
        case OP_INVOKE_VIRTUAL_RANGE:
        case OP_INVOKE_SUPER_RANGE:
        case OP_INVOKE_DIRECT_RANGE:
        case OP_INVOKE_STATIC_RANGE:
        case OP_INVOKE_INTERFACE_RANGE:
            dcv.visitMethodStmt(opcode - (OP_INVOKE_VIRTUAL_RANGE - OP_INVOKE_VIRTUAL), args, dex.getMethod(bBBB));
            break;
        default:
            throw new RuntimeException("");
        }
    }

}
