/**
 * 
 */
package pxb.android.dex2jar.visitors;

import org.objectweb.asm.AnnotationVisitor;


/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexAnnotationAsmAdapter implements DexAnnotationVisitor {
	AnnotationVisitor av;

	/**
	 * @param av
	 */
	public DexAnnotationAsmAdapter(AnnotationVisitor av) {
		super();
		this.av = av;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visit(java.lang.String,
	 * java.lang.Object)
	 */
	public void visit(String name, Object value) {
		av.visit(name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitAnnotation(java
	 * .lang.String, java.lang.String)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, String desc) {
		AnnotationVisitor _av = av.visitAnnotation(name, desc);
		if (av == null)
			return null;
		return new DexAnnotationAsmAdapter(_av);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang
	 * .String)
	 */
	public DexAnnotationVisitor visitArray(String name) {
		AnnotationVisitor _av = av.visitArray(name);
		if (av == null)
			return null;
		return new DexAnnotationAsmAdapter(_av);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnd()
	 */
	public void visitEnd() {
		av.visitEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnum(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	public void visitEnum(String name, String desc, String value) {
		av.visitEnum(name, desc, value);
	}

}
