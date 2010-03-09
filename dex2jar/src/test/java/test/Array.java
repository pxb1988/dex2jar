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
package test;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Array {
	public void test() {
		int[] a = new int[123];
		a[0] = 1;
		boolean b = false;
		boolean[] bs = new boolean[1];
		bs[0] = b;

		byte[] bytes = new byte[1];
		byte _byte = 1;
		bytes[0] = _byte;
		a[0] = _byte;
	}

}
