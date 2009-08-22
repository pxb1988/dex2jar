/**
 * 
 */
package pxb.android.dex2jar;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Proto {
	public String toString() {
		return desc;
	}

	public Proto(DexFile dex, DataIn in) {
		// int shorty_idx =
		in.readIntx();
		int return_type_idx = in.readIntx();
		int parameters_off = in.readIntx();

		returnType = dex.getTypeItem(return_type_idx);
		List<String> parameterTypeList = new ArrayList<String>();
		StringBuilder ps = new StringBuilder("(");
		if (parameters_off != 0) {
			in.pushMove(parameters_off);
			int size = in.readIntx();
			for (int i = 0; i < size; i++) {
				String p = dex.getTypeItem(in.readShortx());
				parameterTypeList.add(p);
				ps.append(p);
			}
			in.pop();
		}
		ps.append(")").append(returnType);
		desc = ps.toString();
		parameterTypes = parameterTypeList.toArray(new String[parameterTypeList.size()]);
	}

	private String[] parameterTypes;
	private String desc;
	private String returnType;

	/**
	 * @return the parameterTypes
	 */
	public String[] getParameterTypes() {
		return parameterTypes;
	}

	public String getReturnType() {
		return returnType;
	}

	public String getDesc() {
		return desc;
	}
}
