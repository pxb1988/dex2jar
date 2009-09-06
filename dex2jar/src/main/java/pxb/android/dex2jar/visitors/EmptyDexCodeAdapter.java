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
public class EmptyDexCodeAdapter implements DexCodeVisitor {

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visit(int, int, int)
	 */
	public void visit(int total_registers_size, int in_register_size, int instruction_size) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, int, int, int)
	 */
	public void visitArrayInsn(int opcode, int regFromOrTo, int array, int index) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitArrayInsn(int, java.lang.String, int, int)
	 */
	public void visitArrayInsn(int opcode, String type, int saveToReg, int demReg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitEnd()
	 */
	public void visitEnd() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int, pxb.android.dex2jar.Field, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int reg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFieldInsn(int, pxb.android.dex2jar.Field, int, int)
	 */
	public void visitFieldInsn(int opcode, Field field, int regFromOrTo, int ownerReg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFillArrayInsn(int, int, int, int, java.lang.Object[])
	 */
	public void visitFillArrayInsn(int opcode, int reg, int elemWidth, int initLength, Object[] values) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInInsn(int, int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInInsn(int, int, int, int)
	 */
	public void visitInInsn(int opcode, int saveToReg, int opReg, int opValueOrReg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitInsn(int)
	 */
	public void visitInsn(int opcode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int)
	 */
	public void visitJumpInsn(int opcode, int offset) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int, int)
	 */
	public void visitJumpInsn(int opcode, int offset, int reg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitJumpInsn(int, int, int, int)
	 */
	public void visitJumpInsn(int opcode, int offset, int reg1, int reg2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLabel(int)
	 */
	public void visitLabel(int index) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLdcInsn(int, java.lang.Object, int)
	 */
	public void visitLdcInsn(int opcode, Object value, int reg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLineNumber(int, int)
	 */
	public void visitLineNumber(int line, int label) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLocalVariable(java.lang.String, java.lang.String, java.lang.String, int, int, int)
	 */
	public void visitLocalVariable(String name, String type, String signature, int start, int end, int reg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitLookupSwitchInsn(int, int, int, int[], int[])
	 */
	public void visitLookupSwitchInsn(int opcode, int reg, int label, int[] cases, int[] label2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitMethodInsn(int, pxb.android.dex2jar.Method, int[])
	 */
	public void visitMethodInsn(int opcode, Method method, int[] regs) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTableSwitchInsn(int, int, int, int, int, int[])
	 */
	public void visitTableSwitchInsn(int opcode, int reg, int first_case, int last_case, int label, int[] labels) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTryCatch(int, int, int, java.lang.String)
	 */
	public void visitTryCatch(int start, int offset, int handler, String type) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int, java.lang.String, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitTypeInsn(int, java.lang.String, int, int)
	 */
	public void visitTypeInsn(int opcode, String type, int toReg, int fromReg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitVarInsn(int, int)
	 */
	public void visitVarInsn(int opcode, int reg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexCodeVisitor#visitFilledNewArrayIns(int, java.lang.String, int[])
	 */
	public void visitFilledNewArrayIns(int opcode, String type, int[] regs) {
		// TODO Auto-generated method stub
		
	}

}
