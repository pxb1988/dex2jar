/**
 * 
 */
package pxb.android.dex2jar.visitors.asm;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class TypeNameAdapter extends ClassAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return super.visitField(access, name, desc, signature, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (exceptions != null) {
			for (int i = 0; i < exceptions.length; i++) {
				exceptions[i] = x(exceptions[i]);
			}
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	/**
	 * @param cv
	 */
	public TypeNameAdapter(ClassVisitor cv) {
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
