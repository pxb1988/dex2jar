/**
 * 
 */
package pxb.android.dex2jar;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface Dex {
	public String getType(int id);

	public String getString(int id);

	public Proto getProto(int id);

	public Method getMethod(int id);

	public Field getField(int id);
}
