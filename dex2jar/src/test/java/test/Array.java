/**
 * 
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
