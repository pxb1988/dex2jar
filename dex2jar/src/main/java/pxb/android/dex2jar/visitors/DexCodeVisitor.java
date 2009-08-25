/**
 * 
 */
package pxb.android.dex2jar.visitors;

import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexCodeVisitor {

	/**
	 * @param registers_size
	 */
	void visitRegister(int registers_size);

	void visitTryCatch(int start, int offset, int handler, String type);

	public void visitLabel(int index);

	/**
	 * @param opcode
	 * @param method
	 * @param buildMethodRegister
	 */
	void visitMethodInsn(int opcode, Method method, int[] regs);

}
