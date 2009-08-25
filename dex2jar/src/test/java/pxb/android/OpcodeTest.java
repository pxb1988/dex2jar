/**
 * 
 */
package pxb.android;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import pxb.android.dex2jar.DexOpcodes;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class OpcodeTest implements Opcodes {
	@Test
	public void test() throws IllegalArgumentException, IllegalAccessException, IOException {
		OutputStream os = new FileOutputStream("target/abc.txt");
		PrintWriter out = new PrintWriter(os);
		for (java.lang.reflect.Field f : DexOpcodes.class.getDeclaredFields()) {
			int opcode = f.getInt(null);
			out.print("map[");
			out.print(opcode);
			out.print("]=\"");
			out.print(f.getName().substring(3));
			out.println("\";");
		}
		out.flush();
		out.close();
		os.flush();
		os.close();
	}
}
