/**
 * 
 */
package pxb.android;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import pxb.android.dex2jar.dump.DumpDexFileAdapter;
import pxb.android.dex2jar.reader.DexFileReader;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DumpTest {
	@Test
	public void test() throws IOException {
		new DexFileReader(new File("target/classes.dex")).accept(new DumpDexFileAdapter());
	}
}
