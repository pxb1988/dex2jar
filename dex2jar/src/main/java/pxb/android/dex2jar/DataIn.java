/**
 * 
 */
package pxb.android.dex2jar;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DataIn {

	public int readIntx();

	public long readLongx();

	public short readShortx();

	public void move(int offset);

	public void pushMove(int offset);

	public void push();

	public void pop();

	byte[] readBytes(int size);

	long readUnsignedLeb128();

	long readSignedLeb128();

	/**
	 * @return
	 */
	public int readUnsignedByte();

	public boolean needPadding();

	/**
	 * 
	 */
	public int readByte();

	/**
	 * @param i
	 */
	public void skip(int bytes);
}
