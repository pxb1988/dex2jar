/**
 * 
 */
package pxb.android.dex2jar.visitors;

/**
 * @author Panxiaobo [pxb1988@126.com]
 *
 */
public interface DexOpcodeVisitor {
	public void visit(int opcode, int arg1);

	public void visit(int opcode, int arg1, int arg2);

	public void visit(int opcode, int arg1, int arg2, int arg3);
}
