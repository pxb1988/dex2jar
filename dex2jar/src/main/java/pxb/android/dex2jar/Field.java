/**
 * 
 */
package pxb.android.dex2jar;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Field {
	public String toString() {
		return this.getOwner() + "." + this.getName() + this.getType();
	}

	public Field(DexFile dex, DataIn in) {
		int owner_idx = in.readShortx();
		int type_idx = in.readShortx();
		int name_idx = in.readIntx();
		owner = dex.getTypeItem(owner_idx);
		type = dex.getTypeItem(type_idx);
		name = dex.getStringItem(name_idx);
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
	public String getType() {
		return type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	private String owner;
	private String type;
	private String name;
}
