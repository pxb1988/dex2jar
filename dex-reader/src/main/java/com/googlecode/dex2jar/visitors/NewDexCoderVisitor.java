package com.googlecode.dex2jar.visitors;

import org.objectweb.asm.Label;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;

public interface NewDexCoderVisitor {

    /**
     * <pre>
     * OP_AGET
     * OP_APUT
     * </pre>
     * 
     * @param opAget
     * @param formOrToReg
     * @param arrayReg
     * @param indexReg
     */
    void visitArrayStmt(int opAget, int formOrToReg, int arrayReg, int indexReg);

    /**
     * <pre>
     * OP_ADD_INT_LIT_X
     * OP_RSUB_INT_LIT_X
     * OP_MUL_INT_LIT_X
     * OP_DIV_INT_LIT_X
     * OP_REM_INT_LIT_X
     * OP_AND_INT_LIT_X
     * OP_OR_INT_LIT_X
     * OP_XOR_INT_LIT_X
     * OP_SHL_INT_LIT_X
     * OP_SHR_INT_LIT_X
     * OP_USHR_INT_LIT_X
     * </pre>
     * 
     * @param opcode
     * @param aA
     * @param bB
     * @param cC
     */
    void visitBinopLitXStmt(int opcode, int aA, int bB, int cC);

    /**
     * <pre>
     * 
     * OP_ADD_INT
     * OP_SUB_INT
     * OP_MUL_INT
     * OP_DIV_INT
     * OP_REM_INT
     * OP_AND_INT
     * OP_OR_INT
     * OP_XOR_INT
     * OP_SHL_INT
     * OP_SHR_INT
     * OP_USHR_INT
     * OP_ADD_LONG
     * OP_SUB_LONG
     * OP_MUL_LONG
     * OP_DIV_LONG
     * OP_REM_LONG
     * OP_AND_LONG
     * OP_OR_LONG
     * OP_XOR_LONG
     * OP_SHL_LONG
     * OP_SHR_LONG
     * OP_USHR_LONG
     * OP_ADD_FLOAT
     * OP_SUB_FLOAT
     * OP_MUL_FLOAT
     * OP_DIV_FLOAT
     * OP_REM_FLOAT
     * OP_ADD_DOUBLE
     * OP_SUB_DOUBLE
     * OP_MUL_DOUBLE
     * OP_DIV_DOUBLE
     * OP_REM_DOUBLE
     * 
     * </pre>
     * 
     * @param opcode
     * @param toReg
     * @param r1
     * @param r2
     */
    void visitBinopStmt(int opcode, int toReg, int r1, int r2);

    /**
     * <pre>
     * OP_INSTANCE_OF
     * OP_NEW_ARRAY
     * </pre>
     * 
     * @param opcode
     * @param a
     * @param b
     * @param type
     */
    void visitClassStmt(int opcode, int a, int b, String type);

    /**
     * <pre>
     * OP_CHECK_CAST
     * OP_NEW_INSTANCE
     * </pre>
     * 
     * @param opCheckCast
     * @param saveTo
     * @param type
     */
    void visitClassStmt(int opCheckCast, int saveTo, String type);

    /**
     * <pre>
     * OP_CMPL_FLOAT
     * OP_CMPG_FLOAT
     * OP_CMPL_DOUBLE
     * OP_CMPG_DOUBLE
     * OP_CMP_LONG
     * </pre>
     * 
     * @param opcode
     * @param distReg
     * @param bB
     * @param cC
     */
    void visitCmpStmt(int opcode, int distReg, int bB, int cC);

    /**
     * <pre>
     * OP_CONST
     * OP_CONST_WIDE
     * OP_CONST_STRING
     * OP_CONST_CLASS
     * </pre>
     * 
     * @param opConst
     * @param a
     * @param b
     */
    void visitConstStmt(int opConst, int toReg, Object value);

    /**
     * <pre>
     * OP_SGET
     * OP_SPUT
     * </pre>
     * 
     * @param opcode
     * @param fromOrToReg
     * @param field
     */
    void visitFieldStmt(int opcode, int fromOrToReg, Field field);

    /**
     * <pre>
     * OP_IGET
     * OP_IPUT
     * </pre>
     * 
     * @param opcode
     * @param fromOrToReg
     * @param objReg
     * @param field
     */
    void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field);

    void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values);

    /**
     * <pre>
     * OP_FILLED_NEW_ARRAY
     * </pre>
     * 
     * @param opcode
     * @param args
     * @param type
     */
    void visitFilledNewArrayStmt(int opcode, int[] args, String type);

    /**
     * <pre>
     * OP_IF_EQ
     * OP_IF_NE
     * OP_IF_LT
     * OP_IF_GE
     * OP_IF_GT
     * OP_IF_LE
     * </pre>
     * 
     * @param opcode
     * @param a
     * @param b
     * @param label
     */
    void visitJumpStmt(int opcode, int a, int b, Label label);

    /**
     * <pre>
     * OP_IF_EQZ
     * OP_IF_NEZ
     * OP_IF_LTZ
     * OP_IF_GEZ
     * OP_IF_GTZ
     * OP_IF_LEZ
     * </pre>
     * 
     * @param opConst
     * @param reg
     * @param label
     */
    void visitJumpStmt(int opConst, int reg, Label label);

    /**
     * OP_GOTO
     * 
     * @param opGoto
     * @param label
     */
    void visitJumpStmt(int opGoto, Label label);

    void visitLookupSwitchStmt(int opcode, int aA, Label label, int[] cases, Label[] labels);

    /**
     * <pre>
     * OP_INVOKE_VIRTUAL
     * OP_INVOKE_SUPER
     * OP_INVOKE_DIRECT
     * OP_INVOKE_STATIC
     * OP_INVOKE_INTERFACE
     * </pre>
     * 
     * @param opcode
     * @param args
     * @param method
     */
    void visitMethodStmt(int opcode, int[] args, Method method);

    /**
     * <pre>
     * OP_MONITOR_ENTER
     * OP_MONITOR_EXIT
     * </pre>
     * 
     * @param opcode
     * @param reg
     */
    void visitMonitorStmt(int opcode, int reg);

    /**
     * <pre>
     * OP_MOVE_RESULT
     * OP_MOVE_RESULT_WIDE
     * OP_MOVE_RESULT_OBJECT
     * OP_MOVE_EXCEPTION
     * </pre>
     * 
     * @param opConst
     * @param toReg
     */
    void visitMoveStmt(int opConst, int toReg);

    /**
     * <pre>
     * OP_MOVE
     * OP_MOVE_WIDE
     * OP_MOVE_OBJECT
     * </pre>
     * 
     * @param opcode
     * @param toReg
     * @param fromReg
     */
    void visitMoveStmt(int opcode, int toReg, int fromReg);

    /**
     * {@link #OP_RETURN_VOID}
     * 
     * @param opcode
     */
    void visitReturnStmt(int opcode);

    /**
     * <pre>
     * OP_RETURN
     * OP_THROW
     * </pre>
     * 
     * @param opConst
     * @param reg
     */
    void visitReturnStmt(int opConst, int reg);

    void visitTableSwitchStmt(int opcode, int aA, Label label, int first_case, int last_case, Label[] labels);

    /**
     * <pre>
     * OP_ARRAY_LENGTH
     * OP_NEG_INT
     * OP_NOT_INT
     * OP_NEG_LONG
     * OP_NOT_LONG
     * OP_NEG_FLOAT
     * OP_NEG_DOUBLE
     * OP_INT_TO_LONG
     * OP_INT_TO_FLOAT
     * OP_INT_TO_DOUBLE
     * OP_LONG_TO_INT
     * OP_LONG_TO_FLOAT
     * OP_LONG_TO_DOUBLE
     * OP_FLOAT_TO_INT
     * OP_FLOAT_TO_LONG
     * OP_FLOAT_TO_DOUBLE
     * OP_DOUBLE_TO_INT
     * OP_DOUBLE_TO_LONG
     * OP_DOUBLE_TO_FLOAT
     * OP_INT_TO_BYTE
     * OP_INT_TO_CHAR
     * OP_INT_TO_SHORT
     * </pre>
     * 
     * @param opcode
     * @param toReg
     * @param fromReg
     */
    void visitUnopStmt(int opcode, int toReg, int fromReg);

}
