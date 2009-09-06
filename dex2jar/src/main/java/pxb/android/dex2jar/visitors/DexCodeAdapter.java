/**
 * 
 */
package pxb.android.dex2jar.visitors;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
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
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visit(int, int, int)
	 */
	public void visit(int total_registers_size, int in_register_size, int instruction_size) {
		dcv.visit(total_registers_size, in_register_size, instruction_size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, int,
	 * int, int)
	 */
	public void visitArrayInsn(int opcode, int value, int array, int index) {
		dcv.visitArrayInsn(opcode, value, array, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int,
	 * java.lang.String, int, int)
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
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int,
	 * pxb.android.dex2jar.Field, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int reg) {
		dcv.visitFieldInsn(opcode, field, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int,
	 * pxb.android.dex2jar.Field, int, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int value_reg, int owner_reg) {
		dcv.visitFieldInsn(opcode, field, value_reg, owner_reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFillArrayInsn(int,
	 * int, int, int, java.lang.Object[])
	 */
	public void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values) {
		dcv.visitFillArrayInsn(opcode, reg, elemWidth, initLength, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInsn(int)
	 */
	public void visitInsn(int opcode) {
		dcv.visitInsn(opcode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitIntInsn(int, int,
	 * int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg, int opValueOrReg) {
		dcv.visitInInsn(opcode, saveToReg, opReg, opValueOrReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int,
	 * int)
	 */
	public void visitJumpInsn(int opcode, int offset, int reg) {
		dcv.visitJumpInsn(opcode, offset, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int,
	 * int, int)
	 */
	public void visitJumpInsn(int opcode, int offset, int reg1, int reg2) {
		dcv.visitJumpInsn(opcode, offset, reg1, reg2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLabel(int)
	 */
	public void visitLabel(int index) {
		dcv.visitLabel(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLdcInsn(int,
	 * java.lang.Object, int)
	 */
	public void visitLdcInsn(int opcode, Object value, int reg) {
		dcv.visitLdcInsn(opcode, value, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLineNumber(int,
	 * int)
	 */
	public void visitLineNumber(int line, int label) {
		dcv.visitLineNumber(line, label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitLocalVariable(java.lang
	 * .String, java.lang.String, java.lang.String, int, int, int)
	 */
	public void visitLocalVariable(String name, String type, String signature, int start, int end, int reg) {
		dcv.visitLocalVariable(name, type, signature, start, end, reg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitLookupSwitchInsn(int,
	 * int, int, int[], int[])
	 */
	public void visitLookupSwitchInsn(int opcode, int reg, int label, int[] cases, int[] label2) {
		dcv.visitLookupSwitchInsn(opcode, reg, label, cases, label2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitMethodInsn(int,
	 * pxb.android.dex2jar.Method, int[])
	 */
	public void visitMethodInsn(int opcode, Method method, int[] regs) {
		dcv.visitMethodInsn(opcode, method, regs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitTableSwitchInsn(int,
	 * int, int, int, int, int[])
	 */
	public void visitTableSwitchInsn(int opcode, int reg, int first_case, int last_case, int label, int[] labels) {
		dcv.visitTableSwitchInsn(opcode, reg, first_case, last_case, label, labels);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTryCatch(int, int,
	 * int, java.lang.String)
	 */
	public void visitTryCatch(int start, int offset, int handler, String type) {
		dcv.visitTryCatch(start, offset, handler, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int,
	 * java.lang.String, int, int)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitIntInsn(int, int,
	 * int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg) {
		dcv.visitInInsn(opcode, saveToReg, opReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int)
	 */
	public void visitJumpInsn(int opcode, int offset) {
		dcv.visitJumpInsn(opcode, offset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int,
	 * java.lang.String, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg) {
		dcv.visitTypeInsn(opcode, type, toReg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexCodeVisitor#visitFilledNewArrayIns(int,
	 * java.lang.String, int[])
	 */
	public void visitFilledNewArrayIns(int opcode, String type, int[] regs) {
		dcv.visitFilledNewArrayIns(opcode, type, regs);
	}

}
