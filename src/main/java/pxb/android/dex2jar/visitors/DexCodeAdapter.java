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

import org.objectweb.asm.Label;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, int, int, int)
	 */
	public void visitArrayInsn(int opcode, int value, int array, int index) {
		dcv.visitArrayInsn(opcode, value, array, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, java.lang.String, int, int)
	 */
	public void visitArrayInsn(int opcode, String type, int saveToReg, int demReg) {
		dcv.visitArrayInsn(opcode, type, saveToReg, demReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitEnd()
	 */
	public void visitEnd() {
		dcv.visitEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int, pxb.android.dex2jar.Field, int, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int value_reg, int owner_reg) {
		dcv.visitFieldInsn(opcode, field, value_reg, owner_reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFillArrayInsn(int, int, int, int, java.lang.Object[])
	 */
	public void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values) {
		dcv.visitFillArrayInsn(opcode, reg, elemWidth, initLength, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFilledNewArrayIns(int, java.lang.String, int[])
	 */
	public void visitFilledNewArrayIns(int opcode, String type, int[] regs) {
		dcv.visitFilledNewArrayIns(opcode, type, regs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitIntInsn(int, int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg) {
		dcv.visitInInsn(opcode, saveToReg, opReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitIntInsn(int, int, int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg, int opValueOrReg) {
		dcv.visitInInsn(opcode, saveToReg, opReg, opValueOrReg);
	}

	public void visitInitLocal(int... args) {
		dcv.visitInitLocal(args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInsn(int)
	 */
	public void visitInsn(int opcode) {
		dcv.visitInsn(opcode);
	}

	public void visitJumpInsn(int opcode, Label label) {
		dcv.visitJumpInsn(opcode, label);
	}

	public void visitJumpInsn(int opcode, Label label, int reg) {
		dcv.visitJumpInsn(opcode, label, reg);
	}

	public void visitJumpInsn(int opcode, Label label, int reg1, int reg2) {
		dcv.visitJumpInsn(opcode, label, reg1, reg2);
	}

	public void visitLabel(Label label) {
		dcv.visitLabel(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLdcInsn(int, java.lang.Object, int)
	 */
	public void visitLdcInsn(int opcode, Object value, int reg) {
		dcv.visitLdcInsn(opcode, value, reg);
	}

	public void visitLineNumber(int line, Label label) {
		dcv.visitLineNumber(line, label);
	}

	public void visitLocalVariable(String name, String type, String signature, Label start, Label end, int reg) {
		dcv.visitLocalVariable(name, type, signature, start, end, reg);
	}

	public void visitLookupSwitchInsn(int opcode, int reg, Label defaultLabel, int[] cases, Label[] labels) {
		dcv.visitLookupSwitchInsn(opcode, reg, defaultLabel, cases, labels);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitMethodInsn(int, pxb.android.dex2jar.Method, int[])
	 */
	public void visitMethodInsn(int opcode, Method method, int[] regs) {
		dcv.visitMethodInsn(opcode, method, regs);
	}

	public void visitTableSwitchInsn(int opcode, int reg, int firstCase, int lastCase, Label defaultLabel, Label[] labels) {
		dcv.visitTableSwitchInsn(opcode, reg, firstCase, lastCase, defaultLabel, labels);
	}

	public void visitTryCatch(Label start, Label end, Label handler, String type) {
		dcv.visitTryCatch(start, end, handler, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int, java.lang.String, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg) {
		dcv.visitTypeInsn(opcode, type, toReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int, java.lang.String, int, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg, int fromReg) {
		dcv.visitTypeInsn(opcode, type, toReg, fromReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitVarInsn(int, int)
	 */
	public void visitVarInsn(int opcode, int reg) {
		dcv.visitVarInsn(opcode, reg);
	}

}
