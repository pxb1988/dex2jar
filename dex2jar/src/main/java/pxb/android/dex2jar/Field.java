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
		return this.getOwner() + "." + this.getName() + " " + this.getType();
	}

	private int access_flags;

	/**
	 * @return the access_flags
	 */
	public int getAccessFlags() {
		return access_flags;
	}

	/**
	 * @param access_flags
	 *            the access_flags to set
	 */
	public void setAccessFlags(int access_flags) {
		this.access_flags = access_flags;
	}

	public Field(Dex dex, DataIn in) {
		int owner_idx = in.readShortx();
		int type_idx = in.readShortx();
		int name_idx = in.readIntx();
		owner = dex.getType(owner_idx);
		type = dex.getType(type_idx);
		name = dex.getString(name_idx);
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
