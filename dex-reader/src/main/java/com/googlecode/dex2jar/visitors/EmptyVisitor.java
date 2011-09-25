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
public class EmptyVisitor implements DexFileVisitor, DexClassVisitor, DexMethodVisitor, DexFieldVisitor,
        DexCodeVisitor, DexAnnotationVisitor {

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    public DexClassVisitor visit(int accessFlags, String className, String superClass, String... interfaceNames) {

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visitEnd()
     */
    public void visitEnd() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitAnnotation(java.lang .String, boolean)
     */
    public DexAnnotationVisitor visitAnnotation(String name, boolean visible) {

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitField(com.googlecode.dex2jar .Field, java.lang.Object)
     */
    public DexFieldVisitor visitField(Field field, Object value) {

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitMethod(com.googlecode. dex2jar.Method)
     */
    public DexMethodVisitor visitMethod(Method method) {

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitSource(java.lang.String )
     */
    public void visitSource(String file) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitCode()
     */
    public DexCodeVisitor visitCode() {

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitParameterAnnotation (int)
     */
    public DexAnnotationAble visitParameterAnnotation(int index) {

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visit(java.lang.String, java.lang.Object)
     */
    public void visit(String name, Object value) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitAnnotation(java .lang.String, java.lang.String)
     */
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang .String)
     */
    public DexAnnotationVisitor visitArray(String name) {

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitEnum(java.lang .String, java.lang.String,
     * java.lang.String)
     */
    public void visitEnum(String name, String desc, String value) {

    }

    @Override
    public void visitArrayStmt(int opAget, int formOrToReg, int arrayReg, int indexReg) {

    }

    @Override
    public void visitBinopLitXStmt(int opcode, int aA, int bB, int cC) {

    }

    @Override
    public void visitBinopStmt(int opcode, int toReg, int r1, int r2) {

    }

    @Override
    public void visitClassStmt(int opcode, int a, int b, String type) {

    }

    @Override
    public void visitClassStmt(int opCheckCast, int saveTo, String type) {

    }

    @Override
    public void visitCmpStmt(int opcode, int distReg, int bB, int cC) {

    }

    @Override
    public void visitConstStmt(int opConst, int toReg, Object value) {

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, Field field) {

    }

    @Override
    public void visitFieldStmt(int opcode, int fromOrToReg, int objReg, Field field) {

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
    public void visitJumpStmt(int opConst, int reg, DexLabel label) {

    }

    @Override
    public void visitJumpStmt(int opGoto, DexLabel label) {

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
    public void visitMoveStmt(int opConst, int toReg) {

    }

    @Override
    public void visitMoveStmt(int opcode, int toReg, int fromReg) {

    }

    @Override
    public void visitReturnStmt(int opcode) {

    }

    @Override
    public void visitReturnStmt(int opConst, int reg) {

    }

    @Override
    public void visitTableSwitchStmt(int opcode, int aA, DexLabel label, int first_case, int last_case,
            DexLabel[] labels) {

    }

    @Override
    public void visitUnopStmt(int opcode, int toReg, int fromReg) {

    }

    @Override
    public void visitTryCatch(DexLabel start, DexLabel end, DexLabel handler, String type) {

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

}
