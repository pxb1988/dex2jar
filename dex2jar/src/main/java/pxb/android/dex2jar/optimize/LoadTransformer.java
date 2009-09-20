package pxb.android.dex2jar.optimize;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LoadTransformer extends MethodTransformerAdapter implements Opcodes {
	public LoadTransformer(MethodTransformer tr) {
		super(tr);
	}

	@Override
	public void transform(MethodNode method) {
		doX(method.instructions, method.instructions.getFirst());
		super.transform(method);
	}

	private static void doX(InsnList innsnList, AbstractInsnNode p) {

		while (p != null) {
			if (isWrite(p)) {
				AbstractInsnNode q = p.getNext();
				if (isRead(q) && isSameVar(p, q)) {
					if (countAccess(innsnList, q) == 2) {
						AbstractInsnNode t = p;
						innsnList.remove(p);
						innsnList.remove(q);
						p = t;
					}
					p = q.getNext();
				} else {
					p = p.getNext();
				}
			} else {
				p = p.getNext();
			}
		}
	}

	private static int countAccess(InsnList innsnList, AbstractInsnNode var) {
		int i = 0;
		AbstractInsnNode p = innsnList.getFirst();
		while (p != null) {
			if (isRead(p) || isWrite(p)) {
				if (isSameVar(p, var))
					i++;
			}
			p = p.getNext();
		}
		return i;
	}

	public static boolean isWrite(AbstractInsnNode p) {
		if (p instanceof VarInsnNode) {
			VarInsnNode q = (VarInsnNode) p;
			switch (q.getOpcode()) {
			case ISTORE:
			case LSTORE:
			case DSTORE:
			case FSTORE:
			case ASTORE:
				return true;
			}
		}
		return false;
	}

	public static boolean isSameVar(AbstractInsnNode p, AbstractInsnNode q) {
		return ((VarInsnNode) p).var == ((VarInsnNode) q).var;
	}

	public static boolean isRead(AbstractInsnNode p) {
		if (p instanceof VarInsnNode) {
			VarInsnNode q = (VarInsnNode) p;
			switch (q.getOpcode()) {
			case ILOAD:
			case DLOAD:
			case LLOAD:
			case FLOAD:
			case ALOAD:
				return true;
			}
		}
		return false;
	}
}
