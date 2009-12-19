package pxb.android.dex2jar.optimize;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class A extends MethodTransformerAdapter implements Opcodes {

	public A(MethodTransformer tr) {
		super(tr);
	}

	@Override
	public void transform(MethodNode method) {
		for (Object o : method.tryCatchBlocks) {
			TryCatchBlockNode tcb = (TryCatchBlockNode) o;
			AbstractInsnNode end = tcb.end;
			AbstractInsnNode p = end.getNext();
			if (p != null && Util.isWrite(p)) {
				AbstractInsnNode q = p.getNext();
				if (q != null && q.getOpcode() == GOTO) {
					method.instructions.remove(p);
					method.instructions.insertBefore(end, p);
					AbstractInsnNode r = p.getPrevious();
					if (r.getOpcode() == POP) {
						method.instructions.remove(r);
					}
				}
			}
		}
		super.transform(method);
	}
}
