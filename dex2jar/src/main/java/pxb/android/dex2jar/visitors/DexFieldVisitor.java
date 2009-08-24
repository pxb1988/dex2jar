/**
 * 
 */
package pxb.android.dex2jar.visitors;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexFieldVisitor extends DexAnnotationAble {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationAble#visitAnnotation(java.lang
	 * .String, boolean)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visitable);

	/**
	 * 
	 */
	public void visitEnd();
}
