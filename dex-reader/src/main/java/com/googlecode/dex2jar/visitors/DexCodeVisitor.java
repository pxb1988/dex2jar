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
     * OP_APUT
     * </pre>
     * 
     * @param opcode
     * @param formOrToReg
     * @param arrayReg
     * @param indexReg
     */
    void visitArrayStmt(int opcode, int formOrToReg, int arrayReg, int indexReg, int xt);

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
     * OP_ADD
     * OP_SUB
     * OP_MUL
     * OP_DIV
     * OP_REM
     * OP_AND
     * OP_OR
     * OP_XOR
     * OP_SHL
     * OP_SHR
     * OP_USHR
     * 
     * </pre>
     * 
     * @param opcode
     * @param toReg
     * @param r1
     * @param r2
     */
    void visitBinopStmt(int opcode, int toReg, int r1, int r2, int xt);

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
     * OP_CMPL
     * OP_CMPG
     * OP_CMP
     * </pre>
     * 
     * @param opcode
     * @param distReg
     * @param bB
     * @param cC
     */
    void visitCmpStmt(int opcode, int distReg, int bB, int cC, int xt);

    /**
     * <pre>
     * OP_CONST
     * OP_CONST_STRING
     * OP_CONST_CLASS
     * </pre>
     * 
     * @param opcode
     * @param a
     * @param b
     */
    void visitConstStmt(int opcode, int toReg, Object value, int xt);

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
    void visitFieldStmt(int opcode, int fromOrToReg, Field field, int xt);

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
    void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field, int xt);

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
     * OP_MOVE_EXCEPTION
     * </pre>
     * 
     * @param opcode
     * @param toReg
     */
    void visitMoveStmt(int opcode, int toReg, int xt);

    /**
     * <pre>
     * OP_MOVE
     * </pre>
     * 
     * @param opcode
     * @param toReg
     * @param fromReg
     */
    void visitMoveStmt(int opcode, int toReg, int fromReg, int xt);

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
     * @param opcode
     * @param reg
     */
    void visitReturnStmt(int opcode, int reg, int xt);

    void visitTableSwitchStmt(int opcode, int aA, DexLabel label, int first_case, int last_case, DexLabel[] labels);

    /**
     * <pre>
     * OP_ARRAY_LENGTH
     * OP_NOT
     * OP_NEG
     * </pre>
     * 
     * @param opcode
     * @param toReg
     * @param fromReg
     */
    void visitUnopStmt(int opcode, int toReg, int fromReg, int xt);

    /**
     * <pre>
     * OP_X_TO_Y
     * </pre>
     * 
     * @param opcode
     * @param toReg
     * @param fromReg
     */
    void visitUnopStmt(int opcode, int toReg, int fromReg, int xta, int xtb);

    void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type);

    void visitArguments(int total, int[] args);

    void visitEnd();

    void visitLabel(DexLabel label);

    void visitLineNumber(int line, DexLabel label);

    void visitLocalVariable(String name, String type, String signature, DexLabel start, DexLabel end, int reg);
}
