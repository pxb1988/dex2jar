/**
 * 
 */
package pxb.android;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Method {
	public String toString() {
		return this.getOwner() + "." + this.getName() + this.getType();
	}

	DexFile dex;
	int type_idx;

	public Method(DexFile dex, DataIn in) {
		int owner_idx = in.readShortx();
		type_idx = in.readShortx();
		int name_idx = in.readIntx();

		owner = dex.getTypeItem(owner_idx);
		// type = dex.getProto(type_idx);
		name = dex.getStringItem(name_idx);
		this.dex = dex;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @return the type
	 */
	public Proto getType() {
		if (type == null) {
			type = dex.getProto(type_idx);
		}
		return type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	private String owner;
	private Proto type;
	private String name;
}
