/*
 * Copyright (c) 2009-2010 Panxiaobo
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
package pxb.android.dex2jar.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class EmptyVisitor implements DexFileVisitor, DexClassVisitor, DexMethodVisitor, DexFieldVisitor, DexCodeVisitor, AnnotationVisitor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public DexClassVisitor visit(int accessFlags, String className, String superClass, String... interfaceNames) {

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visitEnd()
	 */
	public void visitEnd() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexClassVisitor#visitAnnotation(java.lang .String, boolean)
	 */
	public AnnotationVisitor visitAnnotation(String name, boolean visitable) {

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexClassVisitor#visitField(pxb.android.dex2jar .Field, java.lang.Object)
	 */
	public DexFieldVisitor visitField(Field field, Object value) {

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexClassVisitor#visitMethod(pxb.android. dex2jar.Method)
	 */
	public DexMethodVisitor visitMethod(Method method) {

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexClassVisitor#visitSource(java.lang.String )
	 */
	public void visitSource(String file) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitCode()
	 */
	public DexCodeVisitor visitCode() {

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitParamesterAnnotation (int)
	 */
	public DexAnnotationAble visitParamesterAnnotation(int index) {

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, int, int, int)
	 */
	public void visitArrayInsn(int opcode, int regFromOrTo, int array, int index) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, java.lang.String, int, int)
	 */
	public void visitArrayInsn(int opcode, String type, int saveToReg, int demReg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int, pxb.android.dex2jar.Field, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int reg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int, pxb.android.dex2jar.Field, int, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int regFromOrTo, int ownerReg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFillArrayInsn(int, int, int, int, java.lang.Object[])
	 */
	public void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFilledNewArrayIns(int, java.lang.String, int[])
	 */
	public void visitFilledNewArrayIns(int opcode, String type, int[] regs) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInInsn(int, int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInInsn(int, int, int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg, int opValueOrReg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInsn(int)
	 */
	public void visitInsn(int opcode) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLdcInsn(int, java.lang.Object, int)
	 */
	public void visitLdcInsn(int opcode, Object value, int reg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitMethodInsn(int, pxb.android.dex2jar.Method, int[])
	 */
	public void visitMethodInsn(int opcode, Method method, int[] args) {

	}

	public void visitJumpInsn(int opcode, Label label, int reg1, int reg2) {
	}

	public void visitJumpInsn(int opcode, Label label, int reg) {
	}

	public void visitJumpInsn(int opcode, Label label) {
	}

	public void visitLabel(Label label) {
	}

	public void visitLineNumber(int line, Label label) {
	}

	public void visitLocalVariable(String name, String type, String signature, Label start, Label end, int reg) {
	}

	public void visitLookupSwitchInsn(int opcode, int reg, Label defaultLabel, int[] cases, Label[] labels) {
	}

	public void visitTableSwitchInsn(int opcode, int reg, int firstCase, int lastCase, Label defaultLabel, Label[] labels) {
	}

	public void visitTryCatch(Label start, Label end, Label handler, String type) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int, java.lang.String, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int, java.lang.String, int, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg, int fromReg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitVarInsn(int, int)
	 */
	public void visitVarInsn(int opcode, int reg) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visit(java.lang.String, java.lang.Object)
	 */
	public void visit(String name, Object value) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitAnnotation(java .lang.String, java.lang.String)
	 */
	public AnnotationVisitor visitAnnotation(String name, String desc) {

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang .String)
	 */
	public AnnotationVisitor visitArray(String name) {

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnum(java.lang .String, java.lang.String, java.lang.String)
	 */
	public void visitEnum(String name, String desc, String value) {

	}

	public void visitInitLocal(int... args) {

	}

}
