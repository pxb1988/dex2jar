/*
 * Copyright (c) 2009-2010 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pxb.android.dex2jar.v3;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解
 * 
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
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