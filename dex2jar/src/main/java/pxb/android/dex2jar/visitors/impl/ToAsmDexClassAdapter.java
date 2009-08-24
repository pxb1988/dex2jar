/**
 * 
 */
package pxb.android.dex2jar.visitors.impl;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import pxb.android.dex2jar.visitors.DexAnnotationVisitor;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexFieldVisitor;
import pxb.android.dex2jar.visitors.DexMethodVisitor;
import pxb.android.dex2jar.visitors.asm.TypeNameAdapter;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsmDexClassAdapter implements DexClassVisitor, Opcodes {
	protected ClassVisitor cv;

	/**
	 * @param access_flags
	 * @param className
	 * @param superClass
	 * @param interfaceNames
	 */
	public ToAsmDexClassAdapter(ClassVisitor cv, int access_flags, String className, String superClass, String[] interfaceNames) {
		this.cv = new TypeNameAdapter(cv);
		cv.visit(V1_5, access_flags, className, null, superClass, interfaceNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexClassVisitor#visitAnnotation(java.lang
	 * .String, int)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
		AnnotationVisitor av = cv.visitAnnotation(name, visitable == 1);
		if (av == null)
			return null;
		// TODO
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexClassVisitor#visitEnd()
	 */
	public void visitEnd() {
		cv.visitEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexClassVisitor#visitField(int,
	 * java.lang.String, java.lang.String)
	 */
	public DexFieldVisitor visitField(int access_flags, String name, String desc, Object value) {
		FieldVisitor fv = cv.visitField(access_flags, name, desc, null, value);
		if (fv == null)
			return null;
		return new ToAsmDexFieldAdapter(fv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexClassVisitor#visitMethod(int,
	 * java.lang.String, java.lang.String)
	 */
	public DexMethodVisitor visitMethod(int access_flags, String name, String desc) {
		return new ToAsmDexMethodAdapter(cv, access_flags, name, desc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexClassVisitor#visitSource(java.lang.String
	 * )
	 */
	public void visitSource(String file) {
		cv.visitSource(file, null);
	}

}
