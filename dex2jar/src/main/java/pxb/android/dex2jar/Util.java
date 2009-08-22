/**
 * 
 */
package pxb.android.dex2jar;

import java.io.ByteArrayOutputStream;

import pxb.util.Hex;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Util {

	public static void main(String... args) {
		writeUnsignedLeb128(0x10008);
	}

	public static int writeUnsignedLeb128(int value) {
		int remaining = value >> 7;
		int count = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (remaining != 0) {
			baos.write(value & 0x7F | 0x80);
			value = remaining;
			remaining >>= 7;
			++count;
		}
		baos.write(value & 0x7F);
		System.out.println(Hex.from(baos.toByteArray()).encode().toString());
		return (count + 1);
	}
}
