/**
 * 
 */
package pxb.android.dex2jar.v3;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Ann {
	public static class Item {
		/**
		 * @param name
		 * @param value
		 */
		public Item(String name, Object value) {
			super();
			this.name = name;
			this.value = value;
		}

		public String name;
		public Object value;
	}

	/**
	 * @param type
	 * @param visible
	 */
	public Ann(String type, int visible) {
		super();
		this.type = type;
		this.visible = visible;
	}

	public List<Item> items = new ArrayList<Item>();
	public String type;
	public int visible;
}