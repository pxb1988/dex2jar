/**
 * 
 */
package pxb.android.dex2jar.optimize;

import static pxb.android.dex2jar.optimize.LoadTransformer.*;
import static pxb.android.dex2jar.optimize.LoadTransformer.isWrite;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class Z extends MethodTransformerAdapter implements Opcodes {

	Method m;

	public Z(Method m, MethodTransformer tr) {
		super(tr);
		this.m = m;
	}

	static class Block {
		AbstractInsnNode first;
		AbstractInsnNode in[];
		AbstractInsnNode out[];
	}

	InsnList insnList;

	@Override
	public void transform(MethodNode method) {
		List<Block> blocks = new ArrayList<Block>();
		AbstractInsnNode p = method.instructions.getFirst();
		insnList = method.instructions;
		Block block = new Block();
		blocks.add(block);
		if (p instanceof LabelNode) {
			block.first = p;
			p = p.getNext();
		} else {
			block.first = new LabelNode();
			insnList.insertBefore(p, block.first);
		}

		int countR[] = new int[method.maxLocals];
		AbstractInsnNode in[] = new AbstractInsnNode[method.maxLocals];
		AbstractInsnNode out[] = new AbstractInsnNode[method.maxLocals];
		block.in = in;
		block.out = out;
		// Blocks
		while (p != null) {
			if (isRead(p)) {
				int r = var(p);
				if (in[r] == null) {
					if (out[r] == null) {
						in[r] = p;
						countR[r]++;
					}
				}
			} else if (isWrite(p)) {
				int r = var(p);
				out[r] = p;
			} else if (p instanceof LabelNode) {
				block = new Block();
				blocks.add(block);
				block.first = p;
				in = new AbstractInsnNode[method.maxLocals];
				out = new AbstractInsnNode[method.maxLocals];
				block.in = in;
				block.out = out;
			}
			p = p.getNext();
		}
		// if (blocks.size() > 1) {
		for (Block a : blocks) {
			for (int j = 0; j < a.in.length; j++) {
				if (a.out[j] != null && countR[j] == 0) {
					a.out[j] = null;
				}
			}
			doBlock(a);
		}
		// } else {
		// Block a = blocks.get(0);
		// a.out = new AbstractInsnNode[method.maxLocals];
		// doBlock(a);
		// }
		super.transform(method);
	}

	int var(AbstractInsnNode p) {
		return ((VarInsnNode) p).var;
	}

	public void doBlock(Block block) {
		AbstractInsnNode p = block.first.getNext();
		while (p != null && !(p instanceof LabelNode)) {
			if (p.getOpcode() == Opcodes.NEW) {
				p = doNew(p);
			} else {
				p = p.getNext();
			}
		}
		p = block.first.getNext();
		while (p != null && !(p instanceof LabelNode)) {
			if (p.getOpcode() == Opcodes.LDC) {
				AbstractInsnNode q = p.getNext().getNext();
				doLdc(p, block);
				p = q;
			} else {
				p = p.getNext();
			}
		}
		p = block.first.getNext();
		while (p != null && !(p instanceof LabelNode)) {
			if (isWrite(p)) {
				AbstractInsnNode q = p.getNext();
				if (isRead(q) && isSameVar(p, q)) {
					int var = var(p);
					if (block.out[var] != null && block.out[var] == p) {
						p = q.getNext();
					} else {
						AbstractInsnNode c = q.getNext();
						boolean ok = true;
						while (c != null && !(c instanceof LabelNode)) {
							if (isRead(c) && isSameVar(p, c)) {
								ok = false;
								break;
							}
							if (isWrite(c) && isSameVar(p, c)) {
								break;
							}
							c = c.getNext();
						}
						if (ok) {
							AbstractInsnNode t = q.getNext();
							insnList.remove(p);
							insnList.remove(q);
							p = t;
						} else {
							p = q.getNext();
						}
					}
				} else {
					p = p.getNext();
				}
			} else {
				p = p.getNext();
			}
		}
	}

	private void doLdc(AbstractInsnNode _ldc, Block block) {
		AbstractInsnNode _store = _ldc.getNext();
		if (!isWrite(_store))
			return;
		int var = var(_store);
		AbstractInsnNode p = _store.getNext();
		insnList.remove(_ldc);
		insnList.remove(_store);
		while (p != null && !(p instanceof LabelNode)) {
			if (isRead(p) && var(p) == var) {
				AbstractInsnNode x = _ldc.clone(null);
				insnList.insert(p, x);
				insnList.remove(p);
				p = x.getNext();
			} else if (isWrite(p) && var(p) == var) {
				break;
			} else {
				p = p.getNext();
			}
		}
	}

	public AbstractInsnNode doNew(AbstractInsnNode _new) {
		AbstractInsnNode _store = _new.getNext();
		if (_store.getOpcode() == Opcodes.DUP) {
			return _store.getNext();
		}
		int var = var(_store);
		AbstractInsnNode p = _store.getNext();
		while (p != null && !(p instanceof LabelNode)) {
			if (p.getOpcode() == Opcodes.NEW) {
				p = doNew(p);
			} else if (p.getOpcode() == Opcodes.INVOKESPECIAL) {
				MethodInsnNode m = (MethodInsnNode) p;
				if (m.name.equals("<init>")) {
					AbstractInsnNode q = m.getPrevious();
					while (q != null && q != _new) {
						if (isRead(q)) {
							if (var(q) == var) {
								insnList.remove(_new);
								insnList.remove(_store);
								insnList.insertBefore(q, _new);
								insnList.insert(_new, new InsnNode(DUP));
								insnList.remove(q);
								insnList.insert(m, _store);
								return _store.getNext();
							}
						}
						q = q.getPrevious();
					}
				} else {
					p = p.getNext();
				}
			} else {
				p = p.getNext();
			}
		}
		return null;
	}
}
