/**
 * 
 */
package pxb.android.dex2jar.v2;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.dump.DumpDexCodeAdapter;
import pxb.android.dex2jar.visitors.DexAnnotationAble;
import pxb.android.dex2jar.visitors.DexAnnotationAsmAdapter;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexMethodVisitor;
import pxb.android.dex2jar.visitors.EmptyDexAnnotationAdapter;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class ToAsmDexMethodAdapter implements DexMethodVisitor {
	final private ClassVisitor cv;
	private MethodVisitor mv;
	final private List<String> exceptions = new ArrayList<String>();

	protected void buildMv() {
		if (mv == null) {
			String es[] = exceptions.toArray(new String[exceptions.size()]);
			mv = cv.visitMethod(method.getAccessFlags(), method.getName(), method.getType().getDesc(), null, es);
		}
	}

	String owner;
	Method method;

	/**
	 * @param cv
	 * @param access_flags
	 * @param name
	 * @param desc
	 */
	public ToAsmDexMethodAdapter(ClassVisitor cv, String owner, Method method) {
		this.cv = cv;
		this.owner = owner;
		this.method = method;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pxb.android.dex2jar.visitors.DexMethodVisitor#visitAnnotation(java.lang
	 * .String, int)
	 */
	public DexAnnotationVisitor visitAnnotation(String name, int visible) {
		if ("Ldalvik/annotation/Throws;".equals(name)) {
			return new EmptyDexAnnotationAdapter() {

				@Override
				public DexAnnotationVisitor visitArray(String name) {
					return new EmptyDexAnnotationAdapter() {
						@Override
						public void visit(String name, Object value) {
							exceptions.add(value.toString());
						}

					};
				}

			};
		} else {
			buildMv();
			AnnotationVisitor av = mv.visitAnnotation(name, visible == 1);
			if (av == null) {
				return null;
			}
			return new DexAnnotationAsmAdapter(av);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitCode()
	 */
	public DexCodeVisitor visitCode() {
		buildMv();
		return new DumpDexCodeAdapter(new ToAsmDexCodeAdapter(mv, this.owner, method));
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
	public DexAnnotationAble visitParamesterAnnotation(int index) {
		// buildMv();
		return null;
	}

}
