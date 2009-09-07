/**
 * 
 */
package pxb.android.dex2jar.v3;

import java.util.Map;

import org.objectweb.asm.ClassVisitor;

import pxb.android.dex2jar.asm.TypeNameAdapter;
import pxb.android.dex2jar.v1.ClassVisitorFactory;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexFileVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V3 implements DexFileVisitor {
	protected ClassVisitorFactory cvf;
	Map<String, Integer> accessFlagsMap;

	/**
	 * @param accessFlagsMap
	 * @param classVisitorFactory
	 */
	public V3(Map<String, Integer> accessFlagsMap, ClassVisitorFactory classVisitorFactory) {
		this.accessFlagsMap = accessFlagsMap;
		this.cvf = classVisitorFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visit(int,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {
		final ClassVisitor cv = cvf.create(TypeNameAdapter.x(className));
		if (cv == null)
			return null;
		return new V3ClassAdapter(accessFlagsMap, cv, access_flags, className, superClass, interfaceNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visitEnd()
	 */
	public void visitEnd() {
	}

}
