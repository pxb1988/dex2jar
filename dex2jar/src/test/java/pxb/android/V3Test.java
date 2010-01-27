/**
 * 
 */
package pxb.android;

import java.io.IOException;

import org.junit.Test;

import pxb.android.dex2jar.v3.Main;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V3Test {
	@Test
	public void test() throws IOException {
		Main.main("target/test-classes/pxb/android/i_jetty.dex");
	}
}
