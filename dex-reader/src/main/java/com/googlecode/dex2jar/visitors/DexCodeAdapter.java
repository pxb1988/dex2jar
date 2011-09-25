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

import org.objectweb.asm.Label;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DexCodeAdapter implements DexCodeVisitor {
    protected DexCodeVisitor dcv;

    /**
     * @param dcv
     */
    public DexCodeAdapter(DexCodeVisitor dcv) {
        super();
        this.dcv = dcv;
    }

    @Override
    public void visitArrayStmt(int opAget, int formOrToReg, int arrayReg, int indexReg) {
        dcv.visitArrayStmt(opAget, formOrToReg, arrayReg, indexReg);
    }

    @Override
    public void visitBinopLitXStmt(int opcode, int aA, int bB, int cC) {
        dcv.visitBinopLitXStmt(opcode, aA, bB, cC);
    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2) {
        dcv.visitBinopStmt(opcode, toReg, r1, r2);
    }

    @Override
    public void visitClassStmt(int opcode, int a, int b, String type) {
        dcv.visitClassStmt(opcode, a, b, type);
    }

    @Override
    public void visitClassStmt(int opCheckCast, int saveTo, String type) {
        dcv.visitClassStmt(opCheckCast, saveTo, type);
    }

    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC) {
        dcv.visitCmpStmt(opcode, distReg, bB, cC);
    }

    @Override
    public void visitConstStmt(int opConst, int toReg, Object value) {
        dcv.visitConstStmt(opConst, toReg, value);
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field) {
        dcv.visitFieldStmt(opcode, fromOrToReg, field);
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field) {
        dcv.visitFieldStmt(opcode, fromOrToReg, objReg, field);
    }

    @Override
    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {
        dcv.visitFillArrayStmt(opcode, aA, elemWidth, initLength, values);
    }

    @Override
    public void visitFilledNewArrayStmt(int opcode, int[] args, String type) {
        dcv.visitFilledNewArrayStmt(opcode, args, type);
    }

    @Override
    public void visitJumpStmt(int opcode, int a, int b, Label label) {
        dcv.visitJumpStmt(opcode, a, b, label);
    }

    @Override
    public void visitJumpStmt(int opConst, int reg, Label label) {
        dcv.visitJumpStmt(opConst, reg, label);
    }

    @Override
    public void visitJumpStmt(int opGoto, Label label) {
        dcv.visitJumpStmt(opGoto, label);
    }

    @Override
    public void visitLookupSwitchStmt(int opcode, int aA, Label label, int[] cases, Label[] labels) {
        dcv.visitLookupSwitchStmt(opcode, aA, label, cases, labels);
    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, Method method) {
        dcv.visitMethodStmt(opcode, args, method);
    }

    @Override
    public void visitMonitorStmt(int opcode, int reg) {
        dcv.visitMonitorStmt(opcode, reg);
    }

    @Override
    public void visitMoveStmt(int opConst, int toReg) {
        dcv.visitMoveStmt(opConst, toReg);
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg) {
        dcv.visitMoveStmt(opcode, toReg, fromReg);
    }

    @Override
    public void visitReturnStmt(int opcode) {
        dcv.visitReturnStmt(opcode);
    }

    @Override
    public void visitReturnStmt(int opConst, int reg) {
        dcv.visitReturnStmt(opConst, reg);
    }

    @Override
    public void visitTableSwitchStmt(int opcode, int aA, Label label, int first_case, int last_case, Label[] labels) {
        dcv.visitTableSwitchStmt(opcode, aA, label, first_case, last_case, labels);
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg) {
        dcv.visitUnopStmt(opcode, toReg, fromReg);
    }

    @Override
    public void visitTryCatch(Label start, Label end, Label handler, String type) {
        dcv.visitTryCatch(start, end, handler, type);
    }

    @Override
    public void visitArguments(int total, int[] args) {
        dcv.visitArguments(total, args);
    }

    @Override
    public void visitEnd() {
        dcv.visitEnd();
    }

    @Override
    public void visitLabel(Label label) {
        dcv.visitLabel(label);
    }

    @Override
    public void visitLineNumber(int line, Label label) {
        dcv.visitLineNumber(line, label);
    }

    @Override
    public void visitLocalVariable(String name, String type, String signature, Label start, Label end, int reg) {
        dcv.visitLocalVariable(name, type, signature, start, end, reg);
    }

}
