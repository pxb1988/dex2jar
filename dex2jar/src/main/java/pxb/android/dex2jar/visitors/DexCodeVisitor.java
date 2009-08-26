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
public interface DexCodeVisitor {

	/**
	 * @param opcode
	 * @param array
	 * @param index
	 * @param value
	 */
	void visitArrayInsn(int opcode, int array, int index, int value);

	void visitEnd();

	/**
	 * Static Field
	 * 
	 * @param opcode
	 * @param field
	 * @param reg
	 */
	void visitFieldInsn(int opcode, Field field, int reg);

	void visitFieldInsn(int opcode, Field field, int owner_reg, int value_reg);

	/**
	 * @param opcode
	 */
	void visitInsn(int opcode);

	void visitIntInsn(int opcode, int reg1, int reg2, int value);

	void visitJumpInsn(int opcode, int offset, int reg);

	void visitJumpInsn(int opcode, int offset, int reg1, int reg2);

	public void visitLabel(int index);

	void visitLdcInsn(int opcode, Object value, int reg);

	void visitMethodInsn(int opcode, Method method, int[] regs);

	/**
	 * @param opcode
	 * @param from
	 * @param to
	 */
	void visitMoveInsn(int opcode, int from, int to);

	void visitTryCatch(int start, int offset, int handler, String type);

	void visitTypeInsn(int opcode, String type, int reg);

	/**
	 * @param opcode
	 * @param arg1
	 */
	void visitVarInsn(int opcode, int reg);

	/**
	 * @param total_registers_size
	 * @param in_register_size
	 * @param instruction_size
	 */
	void visit(int total_registers_size, int in_register_size, int instruction_size);

	/**
	 * 
	 * @param opcode
	 * @param type
	 * @param saveToReg
	 * @param demReg
	 */
	void visitArrayInsn(int opcode, String type, int saveToReg, int demReg);

}
