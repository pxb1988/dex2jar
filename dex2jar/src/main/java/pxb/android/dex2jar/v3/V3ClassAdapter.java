/**
 * 
 */
package pxb.android.dex2jar.v3;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.asm.TypeNameAdapter;
import pxb.android.dex2jar.visitors.DexAnnotationVisitor;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexFieldVisitor;
import pxb.android.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class V3ClassAdapter implements DexClassVisitor {

	ClassVisitor cv;
	boolean build = false;
	int access_flags;
	String className;
	String superClass;
	String[] interfaceNames;

	protected void build() {
		if (!build) {
			cv.visit(Opcodes.V1_5, access_flags, className, null, superClass, interfaceNames);
			for (Ann ann : anns) {
				AnnotationVisitor av = cv.visitAnnotation(ann.type, ann.visible == 1);
				V3AnnAdapter.accept(ann.items, av);
				av.visitEnd();
			}
			if (file != null) {
				cv.visitSource(file, null);
			}
			build = true;
		}
	}

	protected List<Ann> anns = new ArrayList<Ann>();

	/**
	 * @param cv
	 * @param access_flags
	 * @param className
	 * @param superClass
	 * @param interfaceNames
	 */
	public V3ClassAdapter(ClassVisitor cv, int access_flags, String className, String superClass, String[] interfaceNames) {
		super();
		this.cv = new TypeNameAdapter(cv);
		this.access_flags = access_flags;
		this.className = className;
		this.superClass = superClass;
		this.interfaceNames = interfaceNames;
	}

	public DexAnnotationVisitor visitAnnotation(String name, int visitable) {
		Ann ann = new Ann(name, visitable);
		anns.add(ann);
		return new V3AnnAdapter(ann);
	}

	public void visitEnd() {
		build();
		cv.visitEnd();
	}

	public DexFieldVisitor visitField(Field field, Object value) {
		build();
		return new V3FieldAdapter(cv, field, value);
	}

	public DexMethodVisitor visitMethod(Method method) {
		build();
		return new V3MethodAdapter(cv, method);
	}

	String file;

	public void visitSource(String file) {
		this.file = file;
	}

}