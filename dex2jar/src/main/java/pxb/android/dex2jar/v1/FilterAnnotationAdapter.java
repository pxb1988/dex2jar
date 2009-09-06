/**
 * 
 */
package pxb.android.dex2jar.v1;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class FilterAnnotationAdapter extends MethodAdapter {

	/**
	 * @param mv
	 */
	public FilterAnnotationAdapter(MethodVisitor mv) {
		super(mv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.MethodAdapter#visitAnnotation(java.lang.String,
	 * boolean)
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if ("Ldalvik/annotation/Throws;".equals(desc))
			return null;
		return super.visitAnnotation(desc, visible);
	}

}
