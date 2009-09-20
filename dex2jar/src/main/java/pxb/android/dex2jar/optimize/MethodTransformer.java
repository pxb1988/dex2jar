package pxb.android.dex2jar.optimize;

import org.objectweb.asm.tree.MethodNode;

/**
 * 
 * @author Panxiaobo
 * 
 */
public interface MethodTransformer {
	public void transform(MethodNode method);
}
