/**
 * 
 */
package pxb.android.dex2jar.visitors;

/**
 * @author Panxiaobo [pxb1988@126.com]
 *
 */
public class EmptyDexAnnotationAdapter implements DexAnnotationVisitor {

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visit(java.lang.String, java.lang.Object)
	 */
	public void visit(String name, Object value) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, String desc) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang.String)
	 */
	public DexAnnotationVisitor visitArray(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnd()
	 */
	public void visitEnd() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnum(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void visitEnum(String name, String desc, String value) {
		// TODO Auto-generated method stub

	}

}
