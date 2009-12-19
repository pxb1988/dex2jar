/**
 * 
 */
package pxb.android.dex2jar.optimize;

import static pxb.android.dex2jar.optimize.Util.isRead;
import static pxb.android.dex2jar.optimize.Util.isSameVar;
import static pxb.android.dex2jar.optimize.Util.isWrite;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.TraceMethodVisitor;

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

	public static class Block {
		public AbstractInsnNode first;
		public AbstractInsnNode in[];
		public AbstractInsnNode out[];
	}

	InsnList insnList;
	TraceMethodVisitor tr = new TraceMethodVisitor();

	@Override
	public void transform(MethodNode method) {

		List<Block> blocks = new ArrayList<Block>();
		AbstractInsnNode p = method.instructions.getFirst();
		insnList = method.instructions;

		// dump();

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
		for (Block a : blocks) {
			for (int j = 0; j < a.in.length; j++) {
				if (a.out[j] != null && countR[j] == 0) {
					a.out[j] = null;
				}
			}
			doBlock(a);
		}
		// changeLdc_0(blocks);
		super.transform(method);
	}

	public void changeLdc_0(List<Block> blocks) {
		for (Block a : blocks) {
			AbstractInsnNode p = a.first.getNext();
			while (p != null && !(p instanceof LabelNode)) {
				switch (p.getOpcode()) {
				case LDC: {
					LdcInsnNode ldc = (LdcInsnNode) p;
					Object value = ldc.cst;
					if (value instanceof Integer) {
						int i = (Integer) value;
						if (i == 0) {// may be const null?
							System.out.println("sssssssssssssssssssssssssssssssssssssssssssss");
						}
					}
				}
					break;
				}
				p = p.getNext();
			}
		}
	}

	int var(AbstractInsnNode p) {
		return ((VarInsnNode) p).var;
	}

	public void dump() {
		insnList.accept(tr);
		for (Object o : tr.text) {
			System.out.print(o);
		}
		tr.text.clear();
	}

	protected void doNew(Block block) {
		AbstractInsnNode p = block.first.getNext();
		while (p != null && !(p instanceof LabelNode)) {
			if (p.getOpcode() == Opcodes.NEW) {
				p = doNew(p);
			} else {
				p = p.getNext();
			}
		}
	}

	void dump(Block block) {
		AbstractInsnNode p = block.first.getNext();
		while (p != null && !(p instanceof LabelNode)) {
			p.accept(tr);
			p = p.getNext();
		}
		for (Object o : tr.text) {
			System.out.print(o);
		}
		tr.text.clear();
	}

	protected void doLdc(Block block) {

		AbstractInsnNode p = block.first.getNext();
		while (p != null && !(p instanceof LabelNode)) {
			if (p.getOpcode() == Opcodes.LDC) {
				p = doLdc(p, block);
			} else {
				p = p.getNext();
			}
		}

	}

	protected void doVar(Block block) {
		AbstractInsnNode p = block.first.getNext();
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

	public void doBlock(Block block) {
		// System.out.println("BEFORE");
		// dump(block);

		doNew(block);
		doLdc(block);
		doVar(block);

		// System.out.println("AFTER");
		// dump(block);
	}

	private AbstractInsnNode doLdc(AbstractInsnNode _ldc, Block block) {
		AbstractInsnNode _store = _ldc.getNext();
		if (!isWrite(_store))
			return _store;

		int var = var(_store);
		if (block.out[var] == _store)
			return _store.getNext();
		AbstractInsnNode p = _store.getNext();
		AbstractInsnNode pre = _ldc.getPrevious();
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
		return pre.getNext();
	}

	private AbstractInsnNode doNew(AbstractInsnNode _new) {
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
