package pxb.android.dex2jar.optimize;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.VarInsnNode;

public class Load implements Opcodes {
	public static void transform(InsnList innsnList) {
		doX(innsnList, innsnList.getFirst());
	}

	private static void doX(InsnList innsnList, AbstractInsnNode p) {
		while (p != null) {
			switch (p.getOpcode()) {
			case ISTORE:
			case LSTORE:
			case DSTORE:
			case FSTORE:
			case ASTORE: {
				VarInsnNode l = (VarInsnNode) p;
				p = p.getNext();
				if (p != null) {
					switch (p.getOpcode()) {
					case ILOAD:
					case DLOAD:
					case LLOAD:
					case FLOAD:
					case ALOAD: {
						VarInsnNode s = (VarInsnNode) p;
						if (l.var == s.var && !isRead(s.getNext(), s.var)) {
							innsnList.remove(l);
							innsnList.remove(s);
						}
					}
					}
				}
			}
				break;
			}
			p = p.getNext();
		}
	}

	private static boolean isRead(AbstractInsnNode p, int var) {
		while (p != null) {
			if (p instanceof VarInsnNode) {
				VarInsnNode q = (VarInsnNode) p;
				if (var == q.var) {
					switch (q.getOpcode()) {
					case ILOAD:
					case DLOAD:
					case LLOAD:
					case FLOAD:
					case ALOAD:
						return true;
					}
				}
			}
			p = p.getNext();
		}
		return false;
	}
}
