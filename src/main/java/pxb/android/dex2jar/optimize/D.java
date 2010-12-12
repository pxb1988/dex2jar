/*
 * Copyright (c) 2009-2010 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pxb.android.dex2jar.optimize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public class D implements MethodTransformer, Opcodes {

	private static class Block {
		final public int id;

		public List<AbstractInsnNode> insns = new ArrayList<AbstractInsnNode>();
		final public LabelNode label;
		public LabelNode next;

		public Block(int id, LabelNode label) {
			this.id = id;
			this.label = label;
		}

		public String toString() {
			return String.format("B%03d", id);
		}
	}

	private static final class BranchBlock extends Block {
		public final AbstractInsnNode branchInsn;

		public BranchBlock(int id, LabelNode label, AbstractInsnNode insn) {
			super(id, label);
			this.branchInsn = insn;
		}
	}

	private static class EndBlock extends Block {
		public final AbstractInsnNode endInsn;

		public EndBlock(int id, LabelNode label, AbstractInsnNode endInsn) {
			super(id, label);
			this.endInsn = endInsn;
		}

		public EndBlock clone() {
			EndBlock n = new EndBlock(id, new LabelNode(), endInsn.clone(null));
			List<AbstractInsnNode> insnList = new ArrayList<AbstractInsnNode>();
			for (AbstractInsnNode p : insns) {
				insnList.add(p.clone(null));
			}
			n.insns = insnList;
			return n;
		}
	}

	private static class TcbK {
		final public Block start, end;

		public TcbK(Block start, Block end) {
			this.start = start;
			this.end = end;
		}

		public boolean equals(Object obj) {
			TcbK tcbk = (TcbK) obj;
			return tcbk.start.id == start.id && tcbk.end.id == end.id;
		}

		public int hashCode() {
			return start.id * 31 + end.id;
		}
	}

	private static LabelNode getNextLabelNode(AbstractInsnNode p, InsnList insns) {
		AbstractInsnNode q = p.getNext();
		if (q.getType() == AbstractInsnNode.LABEL) {
			return (LabelNode) q;
		} else {
			LabelNode r = new LabelNode();
			insns.insert(p, r);
			return r;
		}
	}

	private Block first;

	private Map<LabelNode, Block> map = new HashMap<LabelNode, Block>();

	private Map<TcbK, Map<Block, String>> tcbs = new TreeMap<TcbK, Map<Block, String>>(new Comparator<TcbK>() {

		@Override
		public int compare(TcbK o1, TcbK o2) {
			int i = o1.start.id - o2.start.id;
			int j = o1.end.id - o2.end.id;
			return i == 0 ? j : i;
		}
	});

	private void addToMap(Block block) {
		map.put(block.label, block);
		if (block.id == 0) {
			first = block;
		}
	}

	private void cut(MethodNode method) {
		InsnList insns = method.instructions;
		method.instructions = null;
		{
			AbstractInsnNode p = insns.getFirst();
			if (!(p instanceof LabelNode)) {
				insns.insertBefore(p, new LabelNode());
			}
			AbstractInsnNode q = insns.getLast();
			if (!(q instanceof LabelNode)) {
				insns.insert(q, new LabelNode());
			}
		}
		@SuppressWarnings("serial")
		Map<LabelNode, LabelNode> cloneMap = new HashMap<LabelNode, LabelNode>() {
			public LabelNode get(Object key) {

				LabelNode l = super.get(key);
				if (l == null) {
					l = new LabelNode();
					put((LabelNode) key, l);
				}
				return l;
			}
		};
		Map<LabelNode, Block> preBlockMap = new HashMap<LabelNode, Block>();
		int i = 0;
		LabelNode label = null;
		Block block = null;
		List<AbstractInsnNode> currentInsnList = null;
		for (AbstractInsnNode p = insns.getFirst(); p != null; p = p.getNext()) {
			final AbstractInsnNode cp = p.clone(cloneMap);
			switch (cp.getType()) {
			case AbstractInsnNode.LABEL: {
				if (label != null) {
					block = new Block(i++, label);
					block.insns = currentInsnList;
					block.next = (LabelNode) cp;
					addToMap(block);
				}
				currentInsnList = new ArrayList<AbstractInsnNode>();
				label = (LabelNode) cp;
				preBlockMap.put(label, block);
				break;
			}
			case AbstractInsnNode.JUMP_INSN:
			case AbstractInsnNode.LOOKUPSWITCH_INSN:
			case AbstractInsnNode.TABLESWITCH_INSN: {
				if (cp.getOpcode() == GOTO) {
					block = new Block(i++, label);
					block.next = (LabelNode) ((JumpInsnNode) cp).label;
				} else {//
					block = new BranchBlock(i++, label, cp);
					block.next = (LabelNode) getNextLabelNode(p, insns).clone(cloneMap);
				}
				block.insns = currentInsnList;
				addToMap(block);
				currentInsnList = null;
				label = null;
				break;
			}
			case AbstractInsnNode.FRAME:
			case AbstractInsnNode.LINE:
				// ignore
				break;
			default:
				switch (cp.getOpcode()) {
				case IRETURN:
				case LRETURN:
				case FRETURN:
				case DRETURN:
				case ARETURN:
				case RETURN:
				case ATHROW:
					block = new EndBlock(i++, label, cp);
					block.next = null;
					getNextLabelNode(p, insns);
					block.insns = currentInsnList;
					addToMap(block);
					currentInsnList = null;
					label = null;
					break;
				default:
					currentInsnList.add(cp);
				}

			}
		}

		for (Iterator<?> it = method.tryCatchBlocks.iterator(); it.hasNext();) {
			TryCatchBlockNode tcn = (TryCatchBlockNode) it.next();

			Block s = map.get((LabelNode) tcn.start.clone(cloneMap));
			Block e = map.get((LabelNode) tcn.end.clone(cloneMap));
			Block handler = map.get(tcn.handler.clone(cloneMap));
			TcbK key = new TcbK(s, e);

			Map<Block, String> handlers = tcbs.get(key);
			if (handlers == null) {
				handlers = new TreeMap<Block, String>(new Comparator<Block>() {
					@Override
					public int compare(Block o1, Block o2) {
						return o1.id - o2.id;
					}
				});
				tcbs.put(key, handlers);
			}
			handlers.put(handler, tcn.type);
			tcn.start = s.label;
			tcn.end = e.label;
			tcn.handler = handler.label;
		}
	}

	// private void dump(Block block) {
	// TraceMethodVisitor tv = new TraceMethodVisitor();
	// for (AbstractInsnNode insn : block.insns) {
	// insn.accept(tv);
	// }
	// int i = 0;
	// for (Object o : tv.text) {
	// System.out.print(String.format("%4d%s", i++, o));
	// }
	// }

	private void doRebuild(InsnList insnList, Stack<Block> toWriteBlock, List<LabelNode> visited) {
		while (!toWriteBlock.empty()) {
			Block b = toWriteBlock.pop();
			if (visited.contains(b.label)) {
				continue;
			}
			visited.add(b.label);
			insnList.add(b.label);
			for (AbstractInsnNode p : b.insns) {
				insnList.add(p);
			}
			if (b instanceof BranchBlock) {
				BranchBlock bb = (BranchBlock) b;
				switch (bb.branchInsn.getType()) {
				case AbstractInsnNode.JUMP_INSN:
					JumpInsnNode jump = (JumpInsnNode) bb.branchInsn;
					insnList.add(jump);
					toWriteBlock.push(map.get(jump.label));
					if (visited.contains(bb.next)) {
						insnList.add(new JumpInsnNode(GOTO, bb.next));
					} else {
						toWriteBlock.push(map.get(bb.next));
					}
					continue;
				case AbstractInsnNode.LOOKUPSWITCH_INSN:
				case AbstractInsnNode.TABLESWITCH_INSN: {
					AbstractInsnNode ts = bb.branchInsn;
					LabelNode dfltLabel = ts.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN ? ((LookupSwitchInsnNode) ts).dflt
							: ((TableSwitchInsnNode) ts).dflt;
					@SuppressWarnings("unchecked")
					List<LabelNode> labels = ts.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN ? ((LookupSwitchInsnNode) ts).labels
							: ((TableSwitchInsnNode) ts).labels;

					insnList.add(ts);
					toWriteBlock.push(map.get(dfltLabel));
					List<LabelNode> cLables = new ArrayList<LabelNode>(labels);
					Collections.reverse(cLables);
					for (LabelNode labelNode : cLables) {
						toWriteBlock.push(map.get(labelNode));
					}

					continue;
				}
				}
			} else {
				if (b instanceof EndBlock) {
					EndBlock eb = (EndBlock) b;
					insnList.add(eb.endInsn);
				}
				if (b.next != null) {
					if (visited.contains(b.next)) {
						insnList.add(new JumpInsnNode(GOTO, b.next));
					} else {
						toWriteBlock.push(map.get(b.next));
					}
				}
			}
		}
	}

	/**
	 * @param method
	 */
	@SuppressWarnings("rawtypes")
	private void rebuild(MethodNode method) {
		InsnList insnList = new InsnList();
		method.instructions = insnList;
		List<LabelNode> visited = new ArrayList<LabelNode>();
		Stack<Block> toWriteBlock = new Stack<Block>();
		toWriteBlock.push(first);
		doRebuild(insnList, toWriteBlock, visited);
		method.tryCatchBlocks = new ArrayList();
		for (Iterator<?> it = method.tryCatchBlocks.iterator(); it.hasNext();) {
			TryCatchBlockNode tcn = (TryCatchBlockNode) it.next();
			toWriteBlock.push(map.get(tcn.handler));
			doRebuild(insnList, toWriteBlock, visited);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pxb.android.dex2jar.optimize.MethodTransformer#transform(org.objectweb.asm.tree.MethodNode)
	 */
	@Override
	public void transform(MethodNode method) {
		cut(method);
		rebuild(method);
	}
}
