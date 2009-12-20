/**
 * 
 */
package pxb.android;

import java.io.IOException;

import org.junit.Test;

import pxb.android.dex2jar.dump.Dump;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DumpTest {
	@Test
	public void test() throws IOException {
		Dump.main("target/test-classes/pxb/android/i_jetty.dex", "target/ijetty.dump.jar");
	}
}
