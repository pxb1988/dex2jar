/**
 * 
 */
package pxb.android.dex2jar.visitors;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public interface DexAnnotationAble {
	DexAnnotationVisitor visitAnnotation(String name, int visitable);
}
