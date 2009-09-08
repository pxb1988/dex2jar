/**
 * 
 */
package pxb.android.dex2jar.visitors;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexClassAdapter implements DexClassVisitor {
	protected DexClassVisitor dcv;

	/**
	 * @param dcv
	 */
	public DexClassAdapter(DexClassVisitor dcv) {
		super();
		this.dcv = dcv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexClassVisitor#visitAnnotation(java.lang
	 * .String, int)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
		return dcv.visitAnnotation(name, visitable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexClassVisitor#visitEnd()
	 */
	public void visitEnd() {
		dcv.visitEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexClassVisitor#visitField(pxb.android.dex2jar
	 * .Field, java.lang.Object)
	 */
	public DexFieldVisitor visitField(Field field, Object value) {
		return dcv.visitField(field, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexClassVisitor#visitMethod(pxb.android.
	 * dex2jar.Method)
	 */
	public DexMethodVisitor visitMethod(Method method) {
		return dcv.visitMethod(method);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexClassVisitor#visitSource(java.lang.String
	 * )
	 */
	public void visitSource(String file) {
		dcv.visitSource(file);
	}

}
