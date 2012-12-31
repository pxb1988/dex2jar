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

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.OdexOpcodes;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class EmptyVisitor implements OdexFileVisitor, DexClassVisitor, DexMethodVisitor, DexFieldVisitor,
        OdexCodeVisitor, DexAnnotationVisitor, OdexOpcodes {

    @Override
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {

        return null;
    }

    @Override
    public void visitEnd() {

    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, boolean visible) {

        return null;
    }

    @Override
    public void visitArrayStmt(int opcode, int formOrToReg, int arrayReg, int indexReg, int xt) {

    }

    @Override
    public void visitBinopLitXStmt(int opcode, int distReg, int srcReg, int content) {

    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2, int xt) {

    }

    @Override
    public void visitClassStmt(int opcode, int a, int b, String type) {

    }

    @Override
    public void visitClassStmt(int opcode, int saveTo, String type) {

    }

    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC, int xt) {

    }

    @Override
    public void visitConstStmt(int opcode, int toReg, Object value, int xt) {

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field, int xt) {

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field, int xt) {

    }

    @Override
    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {

    }

    @Override
    public void visitFilledNewArrayStmt(int opcode, int[] args, String type) {

    }

    @Override
    public void visitJumpStmt(int opcode, int a, int b, DexLabel label) {

    }

    @Override
    public void visitJumpStmt(int opcode, int reg, DexLabel label) {

    }

    @Override
    public void visitJumpStmt(int opcode, DexLabel label) {

    }

    @Override
    public void visitLookupSwitchStmt(int opcode, int aA, DexLabel label, int[] cases, DexLabel[] labels) {

    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, Method method) {

    }

    @Override
    public void visitMonitorStmt(int opcode, int reg) {

    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int xt) {

    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg, int xt) {

    }

    @Override
    public void visitReturnStmt(int opcode) {

    }

    @Override
    public void visitReturnStmt(int opcode, int reg, int xt) {

    }

    @Override
    public void visitTableSwitchStmt(int opcode, int aA, DexLabel label, int first_case, int last_case,
            DexLabel[] labels) {

    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xt) {

    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg, int xta, int xtb) {

    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handlers, String[] types) {

    }

    @Override
    public void visitArguments(int total, int[] args) {

    }

    @Override
    public void visitLabel(DexLabel label) {

    }

    @Override
    public void visitLineNumber(int line, DexLabel label) {

    }

    @Override
    public void visitLocalVariable(String name, String type, String signature, DexLabel start, DexLabel end, int reg) {

    }

    @Override
    public void visit(String name, Object value) {

    }

    @Override
    public void visitEnum(String name, String desc, String value) {

    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {

        return null;
    }

    @Override
    public DexAnnotationVisitor visitArray(String name) {

        return null;
    }

    @Override
    public void visitReturnStmt(int opcode, int cause, Object ref) {

    }

    @Override
    public void visitMethodStmt(int opcode, int[] args, int a) {

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, int fieldoff, int xt) {

    }

    @Override
    public DexCodeVisitor visitCode() {

        return null;
    }

    @Override
    public DexAnnotationAble visitParameterAnnotation(int index) {

        return null;
    }

    @Override
    public void visitSource(String file) {

    }

    @Override
    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {

        return null;
    }

    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {

        return null;
    }

    @Override
    public void visitDepedence(String name, byte[] checksum) {

    }
}
