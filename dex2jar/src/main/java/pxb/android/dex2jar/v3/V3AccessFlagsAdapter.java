/**
 * 
 */
package pxb.android.dex2jar.v3;

import java.util.HashMap;
import java.util.Map;

import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexFileVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V3AccessFlagsAdapter implements DexFileVisitor {
	Map<String, Integer> map = new HashMap<String, Integer>();

	public Map<String, Integer> getAccessFlagsMap() {
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visit(int,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {
		map.put(className, access_flags);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visitEnd()
	 */
	public void visitEnd() {
	}

}
