/**
 * 
 */
package pxb.android.dex2jar.v2;

import org.objectweb.asm.ClassVisitor;

import pxb.android.dex2jar.asm.TypeNameAdapter;
import pxb.android.dex2jar.v1.ClassVisitorFactory;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexFileVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsm implements DexFileVisitor {
	protected ClassVisitorFactory cvf;

	/**
	 * @param cvf
	 */
	public ToAsm(ClassVisitorFactory cvf) {
		this.cvf = cvf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visit(int,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {
		ClassVisitor cv = cvf.create(TypeNameAdapter.x(className));
		if (cv == null)
			return null;
		return new ToAsmDexClassAdapter(cv, access_flags, className, superClass, interfaceNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexFileVisitor#visitEnd()
	 */
	public void visitEnd() {
	}
}
