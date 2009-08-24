/**
 * 
 */
package pxb.android.dex2jar.visitors;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexFileVisitor {
	public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames);

	/**
	 * 
	 */
	public void visitEnd();
}
