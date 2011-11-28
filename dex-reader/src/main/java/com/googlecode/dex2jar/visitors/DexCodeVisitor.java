/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar.visitors;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public interface DexCodeVisitor {
    /**
     * <pre>
     * OP_AGET
     * OP_AGET_BOOLEAN
     * OP_AGET_BYTE
     * OP_AGET_CHAR
     * OP_AGET_SHORT
     * OP_AGET_WIDE
     * OP_AGET_OBJECT
     * OP_APUT
     * OP_APUT_BOOLEAN
     * OP_APUT_BYTE
     * OP_APUT_CHAR
     * OP_APUT_SHORT
     * OP_APUT_WIDE
     * OP_APUT_OBJECT
     * </pre>
     * 
     * @param opcode
     * @param formOrToReg
     * @param arrayReg
     * @param indexReg
     */
    void visitArrayStmt(int opcode, int formOrToReg, int arrayReg, int indexReg);

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
    void visitBinopLitXStmt(int opcode, int distReg, int srcReg, int content);

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
     * @param opcode
     * @param saveTo
     * @param type
     */
    void visitClassStmt(int opcode, int saveTo, String type);

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
     * @param opcode
     * @param a
     * @param b
     */
    void visitConstStmt(int opcode, int toReg, Object value);

    /**
     * <pre>
     * OP_SGET
     * OP_SGET_BOOLEAN
     * OP_SGET_BYTE
     * OP_SGET_CHAR
     * OP_SGET_SHORT
     * OP_SGET_WIDE
     * OP_SGET_OBJECT
     * OP_SPUT
     * OP_SPUT_BOOLEAN
     * OP_SPUT_BYTE
     * OP_SPUT_CHAR
     * OP_SPUT_SHORT
     * OP_SPUT_WIDE
     * OP_SPUT_OBJECT
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
     * OP_IGET_BOOLEAN
     * OP_IGET_BYTE
     * OP_IGET_CHAR
     * OP_IGET_SHORT
     * OP_IGET_WIDE
     * OP_IGET_OBJECT
     * OP_IPUT
     * OP_IPUT_BOOLEAN
     * OP_IPUT_BYTE
     * OP_IPUT_CHAR
     * OP_IPUT_SHORT
     * OP_IPUT_WIDE
     * OP_IPUT_OBJECT
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
    void visitJumpStmt(int opcode, int a, int b, DexLabel label);

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
     * @param opcode
     * @param reg
     * @param label
     */
    void visitJumpStmt(int opcode, int reg, DexLabel label);

    /**
     * OP_GOTO
     * 
     * @param opcode
     * @param label
     */
    void visitJumpStmt(int opcode, DexLabel label);

    void visitLookupSwitchStmt(int opcode, int aA, DexLabel label, int[] cases, DexLabel[] labels);

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
     * OP_MOVE_EXCEPTION_WIDE
     * OP_MOVE_EXCEPTION_OBJECT
     * </pre>
     * 
     * @param opcode
     * @param toReg
     */
    void visitMoveStmt(int opcode, int toReg);

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
     * OP_RETURN_WIDE
     * OP_RETURN_OBJECT
     * OP_THROW
     * </pre>
     * 
     * @param opcode
     * @param reg
     */
    void visitReturnStmt(int opcode, int reg);

    void visitTableSwitchStmt(int opcode, int aA, DexLabel label, int first_case, int last_case, DexLabel[] labels);

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

    void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type);

    void visitArguments(int total, int[] args);

    void visitEnd();

    void visitLabel(DexLabel label);

    void visitLineNumber(int line, DexLabel label);

    void visitLocalVariable(String name, String type, String signature, DexLabel start, DexLabel end, int reg);
}
