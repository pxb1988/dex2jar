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
package com.googlecode.dex2jar.visitors;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
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
    public void visitArrayStmt(int opAget, int formOrToReg, int arrayReg, int indexReg, int xt) {
        dcv.visitArrayStmt(opAget, formOrToReg, arrayReg, indexReg, xt);
    }

    @Override
    public void visitBinopLitXStmt(int opcode, int aA, int bB, int cC) {
        dcv.visitBinopLitXStmt(opcode, aA, bB, cC);
    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2, int xt) {
        dcv.visitBinopStmt(opcode, toReg, r1, r2, xt);
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
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC, int xt) {
        dcv.visitCmpStmt(opcode, distReg, bB, cC, xt);
    }

    @Override
    public void visitConstStmt(int opConst, int toReg, Object value, int xt) {
        dcv.visitConstStmt(opConst, toReg, value, xt);
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field, int xt) {
        dcv.visitFieldStmt(opcode, fromOrToReg, field, xt);
    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field, int xt) {
        dcv.visitFieldStmt(opcode, fromOrToReg, objReg, field, xt);
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
    public void visitJumpStmt(int opcode, int a, int b, DexLabel label) {
        dcv.visitJumpStmt(opcode, a, b, label);
    }

    @Override
    public void visitJumpStmt(int opConst, int reg, DexLabel label) {
        dcv.visitJumpStmt(opConst, reg, label);
    }

    @Override
    public void visitJumpStmt(int opGoto, DexLabel label) {
        dcv.visitJumpStmt(opGoto, label);
    }

    @Override
    public void visitLookupSwitchStmt(int opcode, int aA, DexLabel label, int[] cases, DexLabel[] labels) {
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
    public void visitMoveStmt(int opConst, int toReg, int xt) {
        dcv.visitMoveStmt(opConst, toReg, xt);
    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg, int xt) {
        dcv.visitMoveStmt(opcode, toReg, fromReg, xt);
    }

    @Override
    public void visitReturnStmt(int opcode) {
        dcv.visitReturnStmt(opcode);
    }

    @Override
    public void visitReturnStmt(int opConst, int reg, int xt) {
        dcv.visitReturnStmt(opConst, reg, xt);
    }

    @Override
    public void visitTableSwitchStmt(int opcode, int aA, DexLabel label, int first_case, int last_case,
            DexLabel[] labels) {
        dcv.visitTableSwitchStmt(opcode, aA, label, first_case, last_case, labels);
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xt) {
        dcv.visitUnopStmt(opcode, toReg, fromReg, xt);
    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {
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
    public void visitLabel(DexLabel label) {
        dcv.visitLabel(label);
    }

    @Override
    public void visitLineNumber(int line, DexLabel label) {
        dcv.visitLineNumber(line, label);
    }

    @Override
    public void visitLocalVariable(String name, String type, String signature, DexLabel start, DexLabel end, int reg) {
        dcv.visitLocalVariable(name, type, signature, start, end, reg);
    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xta, int xtb) {
        dcv.visitUnopStmt(opcode, toReg, fromReg, xta, xtb);
    }

}
