/**
 * 
 */
package pxb.android.dex2jar;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ClassNameAdapter extends ClassAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {

		FieldVisitor fv = super.visitField(access, name, desc, signature, value);
		if (fv != null)
			fv = new FieldNameAdapter(fv);
		return fv;
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
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (mv != null)
			mv = new MethodNameAdapter(mv);
		return mv;
	}

	private static class FieldNameAdapter extends FieldAdapter {

		/**
		 * @param fv
		 */
		public FieldNameAdapter(FieldVisitor fv) {
			super(fv);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see pxb.android.FieldAdapter#visitAnnotation(java.lang.String,
		 * boolean)
		 */
		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return super.visitAnnotation(x(desc), visible);
		}

	}

	private static class MethodNameAdapter extends MethodAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int,
		 * java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			super.visitFieldInsn(opcode, x(owner), name, desc);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int,
		 * java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			super.visitMethodInsn(opcode, x(owner), name, desc);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.objectweb.asm.MethodAdapter#visitTypeInsn(int,
		 * java.lang.String)
		 */
		@Override
		public void visitTypeInsn(int opcode, String type) {
			super.visitTypeInsn(opcode, x(type));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.objectweb.asm.MethodAdapter#visitAnnotation(java.lang.String,
		 * boolean)
		 */
		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return super.visitAnnotation(x(desc), visible);
		}

		/**
		 * @param mv
		 */
		public MethodNameAdapter(MethodVisitor mv) {
			super(mv);
		}

	}

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
