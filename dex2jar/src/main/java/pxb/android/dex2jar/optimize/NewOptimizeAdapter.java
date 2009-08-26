/**
 * 
 */
package pxb.android.dex2jar.optimize;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.xwork.Opcodes;

/**
 * @author Panxiaobo [pxb1988@126.com]
 *
 */
public class NewOptimizeAdapter extends MethodAdapter {

	/**
	 * @param mv
	 */
	public NewOptimizeAdapter(MethodVisitor mv) {
		super(mv);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitTypeInsn(int, java.lang.String)
	 */
	@Override
	public void visitTypeInsn(int opcode, String type) {
		if(opcode==Opcodes.NEW){
			
		}
		super.visitTypeInsn(opcode, type);
	}

}
