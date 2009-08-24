/**
 * 
 */
package pxb.android.dex2jar;

import pxb.android.dex2jar.visitors.DexAnnotationAble;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexAnnotationReader {
	Dex dex;

	/**
	 * @param dex
	 */
	public DexAnnotationReader(Dex dex) {
		super();
		this.dex = dex;
	}

	public void accept(DataIn in, DexAnnotationAble daa) {
		int size = in.readIntx();
		for (int j = 0; j < size; j++) {
			int field_annotation_offset = in.readIntx();
			in.pushMove(field_annotation_offset);
			int visible_i = in.readByte();
			int type_idx = in.readUnsignedLeb128();
			String type = dex.getType(type_idx);
			DexAnnotationVisitor dav = daa.visitAnnotation(type, visible_i);
			if (dav != null) {
				int sizex = in.readUnsignedLeb128();
				for (int k = 0; k < sizex; k++) {
					int name_idx = in.readUnsignedLeb128();
					String name = dex.getString(name_idx);
					Object object = Constant.ReadConstant(dex, in);
					dav.visit(name, object);
				}
			}
			in.pop();
		}
	}
}
