/**
 * 
 */
package pxb.android.dex2jar;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Constant {
	private Object[] array;

	public Constant(DataIn in) {

	}

	public static Object ReadConstant(Dex dex, DataIn in) {
		int b = in.readByte();
		int type = b & 0x1f;
		switch (type) {
		case 0:
			return new Byte((byte) x0246(in, b));
		case 2:
			return new Short((short) x0246(in, b));
		case 4:
			return new Integer((int) x0246(in, b));
		case 6:
			return new Long(x0246(in, b));
		case 3:
			return new Character((char) x3(in, b));
		case 23:
			return dex.getString((int) x3(in, b));
		case 16:
			return Float.intBitsToFloat((int) (xf(in, b) >> 32));
		case 17:
			return Double.longBitsToDouble(xf(in, b));
		case 30:
			return null;// null
		case 28: {
			int size = in.readByte();
			Object[] array = new Object[size];
			for (int i = 0; i < size; i++) {
				array[i] = ReadConstant(dex, in);
			}
			return array;
		}
		case 24: {
			int type_id = (int) x3(in, b);
			return dex.getType(type_id);
		}
		case 31: {
			return new Boolean(((b >> 5) & 0x3) != 0);
		}
		default:
			throw new RuntimeException("Not support yet.");
		}
	}

	public Object get(int id) {
		return array[id];
	}

	private static long x0246(DataIn in, int before) {
		int length = ((before >> 5) & 0x7) + 1;
		long value = 0;
		for (int j = 0; j < length; j++) {
			value |= ((long) in.readByte()) << (j * 8);
		}
		return value;
	}

	private static long x3(DataIn in, int before) {
		int length = ((before >> 5) & 0x7) + 1;
		long value = 0;
		for (int j = 0; j < length; j++) {
			value |= in.readByte() << (j * 8);
		}
		return value;
	}

	private static long xf(DataIn in, int before) {
		int bytes = ((before >> 5) & 0x7) + 1;
		long result = 0L;
		int bitpos = 0;
		for (int i = 0; i < bytes; ++i, bitpos += 8) {
			int b = in.readByte();
			result |= (long) b << bitpos;
		}
		result <<= (8 - bytes) * 8;
		return result;
	}
}
