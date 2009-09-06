/**
 * 
 */
package pxb.android.dex2jar.v1;

import static pxb.android.dex2jar.v1.ClassNameAdapter.x;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class MethodNameAdapter extends MethodAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.asm.MethodAdapter#visitTryCatchBlock(org.objectweb.asm.
	 * Label, org.objectweb.asm.Label, org.objectweb.asm.Label,
	 * java.lang.String)
	 */
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		super.visitTryCatchBlock(start, end, handler, x(type));
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.MethodAdapter#visitAnnotation(java.lang.String,
	 * boolean)
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return super.visitAnnotation(x(desc), visible);
	}

	/**
	 * @param mv
	 */
	public MethodNameAdapter(MethodVisitor mv) {
		super(mv);
	}

}
