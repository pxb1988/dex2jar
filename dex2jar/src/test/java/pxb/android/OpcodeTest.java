/**
 * 
 */
package pxb.android;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class OpcodeTest implements Opcodes {
	@Test
	public void test() {

		// System.out.println(Integer.toHexString(ACC_PUBLIC));
		int type = 6;
		long value = 1;
		long c = value ^ (value >> 63);
		int requiredBits = 65 - Long.numberOfLeadingZeros(c);

		int requiredBytes = (requiredBits + 7) >> 3;

		int a = type | (requiredBytes - 1 << 5);

		while (requiredBytes > 0) {
			int b = (byte) value;
			value >>= 8;
			--requiredBytes;
		}
	}

}
