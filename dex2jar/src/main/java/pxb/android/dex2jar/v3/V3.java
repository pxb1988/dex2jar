/**
 * 
 */
package pxb.android.dex2jar.v3;

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

	/**
	 * @param cvf
	 */
	public V3(ClassVisitorFactory cvf) {
		this.cvf = cvf;
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
		return new V3ClassAdapter(cv, access_flags, className, superClass, interfaceNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visitEnd()
	 */
	public void visitEnd() {
	}

}
