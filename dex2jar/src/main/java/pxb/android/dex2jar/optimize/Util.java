package pxb.android.dex2jar.optimize;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class Util implements Opcodes {

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

	public static boolean isEnd(AbstractInsnNode p) {
		switch (p.getOpcode()) {
		case ATHROW:
		case RETURN:
		case IRETURN:
		case LRETURN:
		case FRETURN:
		case DRETURN:
			return true;
		}
		return false;
	}
}
