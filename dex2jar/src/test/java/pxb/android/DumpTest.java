/**
 * 
 */
package pxb.android;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import pxb.android.dex2jar.dump.Dump;
import pxb.android.dex2jar.reader.DexFileReader;
import pxb.android.dex2jar.visitors.EmptyVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DumpTest {
	@Test
	public void test() throws IOException {
		new DexFileReader(new File("target/test-classes/pxb/android/i_jetty.dex")).accept(new Dump(new EmptyVisitor()));
	}
}
