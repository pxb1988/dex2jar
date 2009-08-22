/**
 * 
 */
package pxb.android.dex2jar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Stack;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DataInImpl implements DataIn {

	private static class XByteArrayInputStream extends ByteArrayInputStream {
		/**
		 * @param buf
		 */
		public XByteArrayInputStream(byte[] buf) {
			super(buf);
		}

		public void setPos(int pos) {
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}
	}

	Stack<Integer> stack = new Stack<Integer>();
	XByteArrayInputStream in;

	/**
	 * @param in
	 */
	public DataInImpl(byte[] data) {
		in = new XByteArrayInputStream(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#move(int)
	 */
	public void move(int offset) {
		((XByteArrayInputStream) in).setPos(offset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#pupBack()
	 */
	public void pop() {
		this.move(stack.pop());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#pushMove(int)
	 */
	public void pushMove(int offset) {
		this.push();
		this.move(offset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#readIntx()
	 */
	public int readIntx() {
		return in.read() | (in.read() << 8) | (in.read() << 16) | (in.read() << 24);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#readBytes(int)
	 */
	public byte[] readBytes(int size) {
		byte[] data = new byte[size];
		try {
			in.read(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#readUnsignedLeb128()
	 */
	public int readUnsignedLeb128() {
		int value = 0;
		int count = 0;
		int b = in.read();
		while ((b & 0x80) != 0) {
			value |= (b & 0x7f) << count;
			count += 7;
			b = in.read();
		}
		value |= (b & 0x7f) << count;
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#push()
	 */
	public void push() {
		stack.push(((XByteArrayInputStream) in).getPos());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#readShortx()
	 */
	public short readShortx() {
		return (short) (in.read() | (in.read() << 8));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#readByte()
	 */
	public int readByte() {
		return in.read();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.DataIn#readUnsignedByte()
	 */
	public int readUnsignedByte() {
		return in.read() & 0xff;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see pxb.android.DataIn#readStringx(int)
	// */
	// public String readStringx(int offset) throws IOException {
	// this.pushMove(offset);
	// int count = 0;
	// while (this.read() != 0) {
	// count++;
	// }
	// byte[] data = new byte[count];
	// this.move(offset);
	// this.read(data);
	// this.pupBack();
	// return new String(data);
	// }

}
