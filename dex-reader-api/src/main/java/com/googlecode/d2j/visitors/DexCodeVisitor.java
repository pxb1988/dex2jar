/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package com.googlecode.d2j.visitors;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.reader.Op;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexCodeVisitor {
    protected DexCodeVisitor visitor;

    public DexCodeVisitor() {
        super();
    }

    public DexCodeVisitor(DexCodeVisitor visitor) {
        super();
        this.visitor = visitor;
    }

    public void visitRegister(int total) {
        if (visitor != null) {
            visitor.visitRegister(total);
        }
    }

    /**
     * <pre>
     * OP_X_INT_LIT8
     * </pre>
     * 
     * @param op
     * @param distReg
     * @param srcReg
     * @param content
     */
    public void visitStmt2R1N(Op op, int distReg, int srcReg, int content) {
        if (visitor != null) {
            visitor.visitStmt2R1N(op, distReg, srcReg, content);
        }
    }

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
     * OP_CMPL
     * OP_CMPG
     * OP_CMP
     * OP_AGETX
     * OP_APUTX
     * </pre>
     * 
     */
    public void visitStmt3R(Op op, int a, int b, int c) {
        if (visitor != null) {
            visitor.visitStmt3R(op, a, b, c);
        }
    }

    /**
     * <pre>
     * OP_INSTANCE_OF
     * OP_NEW_ARRAY
     * OP_CHECK_CAST
     * OP_NEW_INSTANCE
     * </pre>
     * 
     * @param op
     * @param a
     * @param b
     * @param type
     */
    public void visitTypeStmt(Op op, int a, int b, String type) {
        if (visitor != null) {
            visitor.visitTypeStmt(op, a, b, type);
        }
    }

    /**
     * <pre>
     * CONST * CONST_WIDE * CONST_STRING * CONST_CLASS *
     * </pre>
     * 
     * @param op
     * @param ra
     * @param value
     *            int/long/type
     */
    public void visitConstStmt(Op op, int ra, Object value) {
        if (visitor != null) {
            visitor.visitConstStmt(op, ra, value);
        }
    }

    public void visitFillArrayDataStmt(Op op, int ra, Object array) {
        if (visitor != null) {
            visitor.visitFillArrayDataStmt(op, ra, array);
        }
    }

    public void visitEnd() {
        if (visitor != null) {
            visitor.visitEnd();
        }
    }

    /**
     * <pre>
     * OP_IGETX a,b field
     * OP_IPUTX a,b field
     * OP_SGETX a field
     * OP_SPUTX a field
     * </pre>
     * 
     * @param op
     * @param a
     * @param b
     * @param field
     */
    public void visitFieldStmt(Op op, int a, int b, Field field) {
        if (visitor != null) {
            visitor.visitFieldStmt(op, a, b, field);
        }
    }

    /**
     * <pre>
     * OP_FILLED_NEW_ARRAY
     * </pre>
     * 
     * @param op
     * @param args
     * @param type
     */
    public void visitFilledNewArrayStmt(Op op, int[] args, String type) {
        if (visitor != null) {
            visitor.visitFilledNewArrayStmt(op, args, type);
        }
    }

    /**
     * <pre>
     * OP_IF_EQ
     * OP_IF_NE
     * OP_IF_LT
     * OP_IF_GE
     * OP_IF_GT
     * OP_IF_LE
     * OP_GOTO
     * OP_IF_EQZ
     * OP_IF_NEZ
     * OP_IF_LTZ
     * OP_IF_GEZ
     * OP_IF_GTZ
     * OP_IF_LEZ
     * </pre>
     * 
     * @param op
     * @param a
     * @param b
     * @param label
     */
    public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
        if (visitor != null) {
            visitor.visitJumpStmt(op, a, b, label);
        }
    }

    public void visitLabel(DexLabel label) {
        if (visitor != null) {
            visitor.visitLabel(label);
        }
    }

    public void visitSparseSwitchStmt(Op op, int ra, int[] cases, DexLabel[] labels) {
        if (visitor != null) {
            visitor.visitSparseSwitchStmt(op, ra, cases, labels);
        }
    }

    /**
     * <pre>
     * OP_INVOKE_VIRTUAL
     * OP_INVOKE_SUPER
     * OP_INVOKE_DIRECT
     * OP_INVOKE_STATIC
     * OP_INVOKE_INTERFACE
     * </pre>
     * 
     * @param op
     * @param args
     * @param method
     */
    public void visitMethodStmt(Op op, int[] args, Method method) {
        if (visitor != null) {
            visitor.visitMethodStmt(op, args, method);
        }
    }

    /**
     * <pre>
     * OP_MOVE*
     * a = a X b
     * OP_ARRAY_LENGTH
     * a=Xb
     * X_TO_Y
     * </pre>
     * 
     * @param op
     * @param a
     * @param b
     */
    public void visitStmt2R(Op op, int a, int b) {
        if (visitor != null) {
            visitor.visitStmt2R(op, a, b);
        }
    }

    /**
     * 
     * {@link Op#RETURN_VOID} {@link Op#NOP} {@link Op#BAD_OP}
     * 
     * @param op
     */
    public void visitStmt0R(Op op) {
        if (visitor != null) {
            visitor.visitStmt0R(op);
        }
    }

    /**
     * <pre>
     * OP_RETURN_X
     * OP_THROW_X
     * OP_MONITOR_ENTER
     * OP_MONITOR_EXIT
     * OP_MOVE_RESULT_X
     * OP_MOVE_EXCEPTION_X
     * </pre>
     * 
     * @param op
     * @param reg
     */
    public void visitStmt1R(Op op, int reg) {
        if (visitor != null) {
            visitor.visitStmt1R(op, reg);
        }
    }

    public void visitPackedSwitchStmt(Op op, int aA, int first_case, DexLabel[] labels) {
        if (visitor != null) {
            visitor.visitPackedSwitchStmt(op, aA, first_case, labels);
        }
    }

    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler[], String type[]) {
        if (visitor != null) {
            visitor.visitTryCatch(start, end, handler, type);
        }
    }

    public DexDebugVisitor visitDebug() {
        if (visitor != null) {
            return visitor.visitDebug();
        }
        return null;
    }

}
