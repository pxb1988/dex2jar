/**
 * 
 */
package pxb.android.dex2jar.v2;

import org.objectweb.asm.FieldVisitor;

import pxb.android.dex2jar.visitors.DexAnnotationVisitor;
import pxb.android.dex2jar.visitors.DexFieldVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsmDexFieldAdapter implements DexFieldVisitor {
	FieldVisitor fv;

	/**
	 * @param fv2
	 */
	public ToAsmDexFieldAdapter(FieldVisitor fv2) {
		this.fv = fv2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexFieldVisitor#visitAnnotation(java.lang
	 * .String, int)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFieldVisitor#visitEnd()
	 */
	public void visitEnd() {
		fv.visitEnd();
	}
}
