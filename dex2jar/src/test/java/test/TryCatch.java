/**
 * 
 */
package test;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class TryCatch {
	public static void main(String... args) {
		int i = 0;
		int j = 0;
		try {
			System.out.println("a");
		} catch (Exception e) {
			System.out.println("b" + i);
		} catch (Throwable e) {
			System.out.println("c");
		}
		i += 0;
		j += i;
	}
}
