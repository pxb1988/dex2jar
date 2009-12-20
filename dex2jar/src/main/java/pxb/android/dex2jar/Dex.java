/**
 * 
 */
package pxb.android.dex2jar;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface Dex {
	/**
	 * 
	 * 获取类型
	 * 
	 * @param id
	 *            编号
	 * @return
	 */
	public String getType(int id);

	/**
	 * 获取字符串
	 * 
	 * @param id
	 *            字编号
	 * @return
	 */
	public String getString(int id);

	/**
	 * 获取方法的参数及其返回值
	 * 
	 * @param id
	 *            编号
	 * @return
	 */
	public Proto getProto(int id);

	/**
	 * 获取方法
	 * 
	 * @param id
	 *            编号
	 * @return
	 */
	public Method getMethod(int id);

	/**
	 * 获取成员
	 * 
	 * @param id
	 *            编号
	 * @return
	 */
	public Field getField(int id);
}
