/**
 * 
 */
package pxb.android;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import static pxb.android.ClassNameAdapter.*;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class MethodNameAdapter extends MethodAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {

		super.visitFieldInsn(opcode, x(owner), name, desc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		super.visitMethodInsn(opcode, x(owner), name, desc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.MethodAdapter#visitTypeInsn(int, java.lang.String)
	 */
	@Override
	public void visitTypeInsn(int opcode, String type) {
		super.visitTypeInsn(opcode, x(type));
	}

	/**
	 * @param mv
	 */
	public MethodNameAdapter(MethodVisitor mv) {
		super(mv);
	}

}
