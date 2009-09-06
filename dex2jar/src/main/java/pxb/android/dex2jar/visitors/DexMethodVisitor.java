/**
 * 
 */
package pxb.android.dex2jar.visitors;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexMethodVisitor extends DexAnnotationAble {

	public DexAnnotationAble visitParamesterAnnotation(int index);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationAble#visitAnnotation(java.lang
	 * .String, boolean)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visiable);

	/**
	 * 
	 */
	public void visitEnd();

	/**
	 * 
	 */
	public DexCodeVisitor visitCode();
}
