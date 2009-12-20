/**
 * 
 */
package pxb.android.dex2jar.v3;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Ann {
	public static class Item {
		public String name;

		public Object value;

		/**
		 * @param name
		 * @param value
		 */
		public Item(String name, Object value) {
			super();
			this.name = name;
			this.value = value;
		}
	}

	public List<Item> items = new ArrayList<Item>();

	public String type;
	public int visible;

	/**
	 * @param type
	 * @param visible
	 */
	public Ann(String type, int visible) {
		super();
		this.type = type;
		this.visible = visible;
	}
}