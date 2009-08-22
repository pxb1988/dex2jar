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
		try {
			System.out.println("a");
		} catch (Exception e) {
			System.out.println("b");
		} catch (Throwable e) {
			System.out.println("c");
		}
	}
}
