package pxb.android.dex2jar.optimize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import pxb.android.dex2jar.Method;

/**
 * @deprecated
 * @author Panxiaobo
 * 
 */
@SuppressWarnings("unchecked")
public class X implements Opcodes {
	public static void transform(Method m, MethodNode method) {

		if (method.instructions.getFirst() != null) {
			List<LabelNode> labels = new ArrayList<LabelNode>();
			Set<LabelNode> frames = new HashSet<LabelNode>();

			for (AbstractInsnNode p = method.instructions.getFirst(); p != null; p = p.getNext()) {
				if (p instanceof LabelNode) {
					labels.add((LabelNode) p);
				}
			}

			for (Iterator<TryCatchBlockNode> it = method.tryCatchBlocks.iterator(); it.hasNext();) {
				TryCatchBlockNode tcbn = it.next();
				frames.add(tcbn.handler);
			}

			for (AbstractInsnNode p = method.instructions.getFirst(); p != null; p = p.getNext()) {
				if (p instanceof JumpInsnNode) {
					if (p.getOpcode() == GOTO) {
						frames.add(((JumpInsnNode) p).label);
					}
				} else if (p instanceof LookupSwitchInsnNode) {
					LookupSwitchInsnNode lsi = (LookupSwitchInsnNode) p;
					frames.add(lsi.dflt);
					for (Iterator<LabelNode> it = lsi.labels.iterator(); it.hasNext();) {
						frames.add(it.next());
					}
				} else if (p instanceof TableSwitchInsnNode) {
					TableSwitchInsnNode tsi = (TableSwitchInsnNode) p;
					frames.add(tsi.dflt);
					for (Iterator<LabelNode> it = tsi.labels.iterator(); it.hasNext();) {
						frames.add(it.next());
					}
				}
			}
			AnalyzerAdapter a = new AnalyzerAdapter(m.getOwner(), method.access, method.name, method.desc,
					new EmptyVisitor());
			for (AbstractInsnNode p = method.instructions.getFirst(); p != null; p = p.getNext()) {
				p.accept(a);
				if (p instanceof LabelNode) {
					if (frames.contains(p)) {
						String type = null;
						for (Iterator<TryCatchBlockNode> it = method.tryCatchBlocks.iterator(); it.hasNext();) {
							TryCatchBlockNode tcbn = it.next();
							if (p.equals(tcbn.handler)) {
								type = tcbn.type;
								if (type == null) {
									type = "Ljava/lang/Throwable;";
								}
								break;
							}
						}
						if (type != null) {
							a.visitFrame(Opcodes.F_NEW, a.locals.size(), a.locals.toArray(), 1, new Object[] { type });
						} else {
							a.visitFrame(Opcodes.F_NEW, a.locals.size(), a.locals.toArray(new Object[0]), 0, null);
						}
					}

				}
			}
		}
	}
}
