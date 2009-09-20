/**
 * 
 */
package pxb.android.dex2jar.optimize;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class NewTransformer extends MethodTransformerAdapter implements Opcodes {
	public NewTransformer(MethodTransformer tr) {
		super(tr);
	}

	@Override
	public void transform(MethodNode method) {
		doX(method.instructions, method.instructions.getFirst());
		super.transform(method);
	}

	private static MethodInsnNode doX(InsnList innsnList, AbstractInsnNode p) {
		while (p != null) {
			switch (p.getOpcode()) {
			case NEW: {
				TypeInsnNode _new;
				VarInsnNode _store;
				_new = (TypeInsnNode) p;
				p = p.getNext();
				_store = (VarInsnNode) p;
				p = p.getNext();
				MethodInsnNode m = doX(innsnList, p);
				innsnList.remove(_new);
				innsnList.remove(_store);
				int length = Type.getArgumentTypes(m.desc).length;
				AbstractInsnNode q = m;
				for (int i = 0; i <= length; i++) {
					q = q.getPrevious();
				}
				innsnList.insert(q, _new);
				innsnList.insert(_new, new InsnNode(DUP));
				innsnList.remove(q);
				innsnList.insert(m, _store);
				p = _store;
			}
				break;
			case INVOKESPECIAL: {
				MethodInsnNode m = (MethodInsnNode) p;
				m.name.equals("<init>");
				return m;
			}
			}
			p = p.getNext();
		}
		return null;
	}
}
