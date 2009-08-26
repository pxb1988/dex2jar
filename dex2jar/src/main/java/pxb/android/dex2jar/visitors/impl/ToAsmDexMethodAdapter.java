/**
 * 
 */
package pxb.android.dex2jar.visitors.impl;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexMethodVisitor;
import pxb.android.dex2jar.visitors.DexParameterAnnotationVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsmDexMethodAdapter implements DexMethodVisitor {
	final private ClassVisitor cv;
	private MethodVisitor mv;
	final private int access_flags;
	final private List<String> exceptions = new ArrayList<String>();

	protected void buildMv() {
		if (mv == null) {
			String es[] = exceptions.toArray(new String[exceptions.size()]);
			mv = cv.visitMethod(access_flags, name, desc, null, es);
		}
	}

	String owner;
	String name;
	String desc;

	/**
	 * @param cv
	 * @param access_flags
	 * @param name
	 * @param desc
	 */
	public ToAsmDexMethodAdapter(ClassVisitor cv, int access_flags, String owner, String name, String desc) {
		this.cv = cv;
		this.access_flags = access_flags;
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexMethodVisitor#visitAnnotation(java.lang
	 * .String, int)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitCode()
	 */
	public DexCodeVisitor visitCode() {
		buildMv();
		return new ToAsmDexCodeAdapter(mv, access_flags, this.owner, this.name, this.desc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitEnd()
	 */
	public void visitEnd() {
		buildMv();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexMethodVisitor#visitParamesterAnnotation
	 * (int)
	 */
	public DexParameterAnnotationVisitor visitParamesterAnnotation(int index) {
		// buildMv();
		return null;
	}

}
