package pxb.android.dex2jar;

import org.objectweb.asm.ClassVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface ClassVisitorFactory {
	public ClassVisitor create(String className);
}
