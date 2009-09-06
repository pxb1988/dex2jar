/**
 * 
 */
package pxb.android.dex2jar.v3;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.v3.Ann.Item;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V3AnnAdapter implements DexAnnotationVisitor {

	Ann ann;

	public static void accept(List<Item> items, AnnotationVisitor av) {
		if (av == null)
			return;
		for (Item item : items) {
			Object v = item.value;
			if (v instanceof Ann) {
				Ann a = (Ann) v;
				if (a.type != null) {
					AnnotationVisitor av1 = av.visitAnnotation(item.name, a.type);
					accept(a.items, av1);
					av1.visitEnd();
				} else {// array
					AnnotationVisitor av1 = av.visitArray(item.name);
					accept(a.items, av1);
					av1.visitEnd();
				}
			} else if (v instanceof Field) {
				Field e = (Field) v;
				av.visitEnum(item.name, e.getType(), e.getName());
			} else {
				av.visit(item.name, v);
			}
		}
	}

	/**
	 * @param ann
	 */
	public V3AnnAdapter(Ann ann) {
		super();
		this.ann = ann;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visit(java.lang.String,
	 * java.lang.Object)
	 */
	public void visit(String name, Object value) {
		ann.items.add(new Item(name, value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitAnnotation(java
	 * .lang.String, java.lang.String)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, String desc) {
		Ann ann = new Ann(desc, -1);
		this.ann.items.add(new Item(name, ann));
		return new V3AnnAdapter(ann);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang
	 * .String)
	 */
	public DexAnnotationVisitor visitArray(String name) {
		Ann ann = new Ann(null, -1);
		this.ann.items.add(new Item(name, ann));
		return new V3AnnAdapter(ann);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnd()
	 */
	public void visitEnd() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexAnnotationVisitor#visitEnum(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	public void visitEnum(String name, String desc, String value) {
	}

}
