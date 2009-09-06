/**
 * 
 */
package pxb.android.dex2jar.v3;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;
import pxb.android.dex2jar.visitors.DexFieldVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V3FieldAdapter implements DexFieldVisitor {
	protected List<Ann> anns = new ArrayList<Ann>();
	protected boolean build = false;
	protected ClassVisitor cv;
	protected Field field;
	protected FieldVisitor fv;
	Object value;

	protected void build() {
		if (!build) {
			FieldVisitor fv = cv.visitField(field.getAccessFlags(), field.getName(), field.getOwner(), null, value);
			if (fv != null) {
				for (Ann ann : anns) {
					AnnotationVisitor av = fv.visitAnnotation(ann.type, ann.visible == 1);
					V3AnnAdapter.accept(ann.items, av);
					av.visitEnd();
				}
			}
			this.fv = fv;
			build = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexFieldVisitor#visitAnnotation(java.lang
	 * .String, int)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
		Ann ann = new Ann(name, visitable);
		anns.add(ann);
		return new V3AnnAdapter(ann);
	}

	/**
	 * @param cv
	 * @param field
	 */
	public V3FieldAdapter(ClassVisitor cv, Field field, Object value) {
		super();
		this.cv = cv;
		this.field = field;
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFieldVisitor#visitEnd()
	 */
	public void visitEnd() {
		build();
		if (fv != null)
			fv.visitEnd();
	}

}
