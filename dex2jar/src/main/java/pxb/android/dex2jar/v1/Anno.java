/**
 * 
 */
package pxb.android.dex2jar.v1;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.DataIn;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Anno {
	public static class Item {
		private String name;
		private Object value;

		public String toString() {
			Object value = this.getValue();
			StringBuilder sb = new StringBuilder(this.getName()).append('=');
			if (value.getClass().isArray()) {
				Object[] array = (Object[]) value;
				if (array.length > 0) {
					if (array.length > 1)
						sb.append('{');
					sb.append(array[0]);
					for (int j = 1; j < array.length; j++) {
						sb.append(',').append(array[j]);
					}
					if (array.length > 1)
						sb.append('}');
				}
			} else {
				sb.append(value);
			}
			return sb.toString();
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the value
		 */
		public Object getValue() {
			return value;
		}
	}

	public void accept(MethodVisitor mv) {
		AnnotationVisitor av = mv.visitAnnotation(this.getType(), this.getVisible());
		accept(av);
	}

	private static final Logger log = LoggerFactory.getLogger(Anno.class);

	private void accept(AnnotationVisitor av) {
		if (av == null)
			return;
		for (Item it : this.getItems()) {
			if (it.getValue() != null) {
				Object value = it.getValue();
				if (value.getClass().isArray()) {
					log.error("NoSupport Arry Value In Annotation {}", this);
					// TODO
					// AnnotationVisitor arrayVisitor =
					// av.visitArray(it.getName());
					// // arrayVisitor.visit(it.getName(), value);
					// Object[] as = (Object[]) value;
					// for (Object a : as)
					// arrayVisitor.visit(it.getName(), a);
				} else {
					av.visit(it.getName(), value);
				}
			}
		}

		av.visitEnd();
	}

	public void accept(FieldVisitor fv) {
		AnnotationVisitor av = fv.visitAnnotation(this.getType(), this.getVisible());
		accept(av);
	}

	public void accept(ClassVisitor cv) {
		AnnotationVisitor av = cv.visitAnnotation(this.getType(), this.getVisible());
		accept(av);
	}

	/**
	 * accept Method Parameter Visitor
	 * 
	 * @param index
	 * @param mv
	 */
	public void accept(int index, MethodVisitor mv) {
		AnnotationVisitor av = mv.visitParameterAnnotation(index, this.getType(), this.getVisible());
		accept(av);
	}

	public Anno(DexFile dex, DataIn in) {
		int visible_i = in.readByte();
		visible = visible_i == 1;// VISIBILITY_RUNTIME
		int type_idx = (int) in.readUnsignedLeb128();
		type = dex.getType(type_idx);
		int sizex = (int) in.readUnsignedLeb128();
		items = new Item[sizex];
		for (int k = 0; k < sizex; k++) {
			Item item = new Item();
			int name_idx = (int) in.readUnsignedLeb128();
			item.name = dex.getString(name_idx);
			Object object = Constant.ReadConstant(dex, in);
			item.value = object;
			items[k] = item;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("@").append(ClassNameAdapter.x(this.getType()));
		if (items != null && items.length > 0) {
			sb.append('(');
			Item item = items[0];
			sb.append(item);
			for (int i = 1; i < items.length; i++) {
				item = items[i];
				sb.append(',').append(item);
			}
			sb.append(')');
		}
		return sb.toString();
	}

	/**
	 * @return the visible
	 */
	public boolean getVisible() {
		return visible;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the items
	 */
	public Item[] getItems() {
		return items;
	}

	private boolean visible;
	private String type;
	private Item[] items;
}
