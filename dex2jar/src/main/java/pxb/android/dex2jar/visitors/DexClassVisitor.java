/**
 * 
 */
package pxb.android.dex2jar.visitors;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexClassVisitor extends DexAnnotationAble {

	public void visitSource(String file);

	public DexFieldVisitor visitField(Field field, Object value);

	public DexMethodVisitor visitMethod(Method method);

	public DexAnnotationVisitor visitAnnotation(String name, int visitable);

	public void visitEnd();
}
