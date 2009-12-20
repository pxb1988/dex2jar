/**
 * 
 */
package pxb.android.dex2jar.visitors;

/**
 * 用于访问注解
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexAnnotationAble {
	/**
	 * 访问注解
	 * 
	 * @param name
	 *            注解名
	 * @param visitable
	 *            是否运行时可见
	 * @return
	 */
	DexAnnotationVisitor visitAnnotation(String name, int visitable);
}
