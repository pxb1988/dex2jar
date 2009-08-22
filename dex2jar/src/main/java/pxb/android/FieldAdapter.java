/**
 * 
 */
package pxb.android;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class FieldAdapter implements FieldVisitor {
	protected FieldVisitor fv;

	/**
	 * @param fv
	 */
	public FieldAdapter(FieldVisitor fv) {
		super();
		this.fv = fv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.FieldVisitor#visitAnnotation(java.lang.String,
	 * boolean)
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return fv.visitAnnotation(desc, visible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.asm.FieldVisitor#visitAttribute(org.objectweb.asm.Attribute
	 * )
	 */
	public void visitAttribute(Attribute attr) {
		fv.visitAttribute(attr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.FieldVisitor#visitEnd()
	 */
	public void visitEnd() {
		fv.visitEnd();
	}

}
