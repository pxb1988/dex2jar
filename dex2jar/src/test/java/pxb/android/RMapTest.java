/**
 * 
 */
package pxb.android;

import org.junit.Assert;
import org.junit.Test;

import pxb.android.dex2jar.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class RMapTest {
	@Test
	public void test() {
		Assert.assertArrayEquals(new int[] { 5, 6, 7, 0, 1, 2, 3, 4 }, DexMethodVisitor.RegisterMapGenerator(8, 5));
		Assert.assertArrayEquals(new int[] { 4, 5, 6, 7, 0, 1, 2, 3 }, DexMethodVisitor.RegisterMapGenerator(8, 4));
	}
}
