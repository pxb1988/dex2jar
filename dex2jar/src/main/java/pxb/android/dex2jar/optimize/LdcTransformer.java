package pxb.android.dex2jar.optimize;

import static pxb.android.dex2jar.optimize.LoadTransformer.isRead;
import static pxb.android.dex2jar.optimize.LoadTransformer.isSameVar;
import static pxb.android.dex2jar.optimize.LoadTransformer.isWrite;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * 
 * @author Panxiaobo
 * 
 */
public class LdcTransformer extends MethodTransformerAdapter implements Opcodes {
	public LdcTransformer(MethodTransformer tr) {
		super(tr);
	}

	@Override
	public void transform(MethodNode method) {
		doX(method.instructions, method.instructions.getFirst());
		super.transform(method);
	}

	private static void doX(InsnList innsnList, AbstractInsnNode p) {
		while (p != null) {
			if (p instanceof LdcInsnNode) {
				AbstractInsnNode q = p.getNext();
				if (isWrite(q)) {
					AbstractInsnNode a = q.getNext();
					while (a != null) {
						// if (a instanceof LabelNode) {
						// break;
						// } else
						if (isRead(a) && isSameVar(a, q)) {
							innsnList.insertBefore(a, p.clone(null));
							AbstractInsnNode t = a.getNext();
							innsnList.remove(a);
							a = t;
						}
						a = a.getNext();
					}
					AbstractInsnNode t = q;
					innsnList.remove(p);
					innsnList.remove(q);
					p = t.getNext();
				} else {
					p = p.getNext();
				}
			} else {
				p = p.getNext();
			}
		}
	}

}
