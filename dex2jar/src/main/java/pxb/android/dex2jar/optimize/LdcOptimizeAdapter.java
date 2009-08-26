/**
 * 
 */
package pxb.android.dex2jar.optimize;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class LdcOptimizeAdapter extends MethodAdapter implements Opcodes {

	/**
	 * @param mv
	 */
	public LdcOptimizeAdapter(MethodVisitor mv) {
		super(mv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.MethodAdapter#visitLdcInsn(java.lang.Object)
	 */
	@Override
	public void visitLdcInsn(Object cst) {
		if (cst instanceof Integer) {
			int value = (Integer) cst;
			if (value >= 0 && value <= 5) {
				super.visitInsn(ICONST_0 + value);
			} else if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
				super.visitIntInsn(BIPUSH, value);
			} else if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
				super.visitIntInsn(SIPUSH, value);
			} else {
				super.visitLdcInsn(cst);
			}
		} else {
			super.visitLdcInsn(cst);
		}
	}

}
