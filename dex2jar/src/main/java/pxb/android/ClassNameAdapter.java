/**
 * 
 */
package pxb.android;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ClassNameAdapter extends ClassAdapter {

	/**
	 * @param cv
	 */
	public ClassNameAdapter(ClassVisitor cv) {
		super(cv);
	}

	public static String x(String s) {
		if (s == null)
			return null;
		if (s.charAt(0) == 'L') {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		name = x(name);
		superName = x(superName);
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				interfaces[i] = x(interfaces[i]);
			}
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}
}
