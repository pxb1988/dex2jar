/**
 * 
 */
package pxb.android.dex2jar;

/**
 * 成员
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Field {
	/**
	 * 修饰符
	 */
	private int access_flags;
	/**
	 * 成员名
	 */
	private String name;
	/**
	 * 所有者
	 */
	private String owner;
	/**
	 * 成员类型
	 */
	private String type;

	public Field(Dex dex, DataIn in) {
		int owner_idx = in.readShortx();
		int type_idx = in.readShortx();
		int name_idx = in.readIntx();
		owner = dex.getType(owner_idx);
		type = dex.getType(type_idx);
		name = dex.getString(name_idx);
	}

	/**
	 * @see #access_flags
	 * @return the access_flags
	 */
	public int getAccessFlags() {
		return access_flags;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
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
	 * @see #access_flags
	 * @param access_flags
	 */
	public void setAccessFlags(int access_flags) {
		this.access_flags = access_flags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getOwner() + "." + this.getName() + " " + this.getType();
	}
}
