/**
 * 
 */
package pxb.android;

import org.objectweb.asm.ClassVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface ClassVisitorFactory {
	public ClassVisitor create();
}
