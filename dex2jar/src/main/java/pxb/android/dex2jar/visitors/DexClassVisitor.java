/**
 * 
 */
package pxb.android.dex2jar.visitors;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexClassVisitor extends DexAnnotationAble {

	public void visitSource(String file);

	public DexFieldVisitor visitField(int access_flags, String name, String desc, Object value);

	public DexMethodVisitor visitMethod(int access_flags, String name, String desc);

	public DexAnnotationVisitor visitAnnotation(String name, int visitable);

	public void visitEnd();
}
