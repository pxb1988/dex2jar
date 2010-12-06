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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * 
 */
public class D implements MethodTransformer, Opcodes {

	private static class Block {
		public Block(int id, Label label) {
			this.id = id;
			this.label = label;
		}

		final public int id;
		final public Label label;
		public Label next;
		public InsnList insns = new InsnList();
	}

	private static final class BranchBlock extends Block {
		public BranchBlock(int id, Label label, AbstractInsnNode insn) {
			super(id, label);
			this.branchInsn = insn;
		}

		public final AbstractInsnNode branchInsn;
	}

	private static class EndBlock extends Block {
		public EndBlock(int id, Label label, AbstractInsnNode endInsn) {
			super(id, label);
			this.endInsn = endInsn;
		}

		public final AbstractInsnNode endInsn;

		public EndBlock clone() {
			EndBlock n = new EndBlock(id, new Label(), endInsn.clone(null));
			InsnList insnList = new InsnList();
			for (AbstractInsnNode p = insns.getFirst(); p != null; p = p.getNext()) {
				insnList.add(p.clone(null));
			}
			n.insns = insnList;
			return n;
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

	private static class Tcb {
		public TryCatchBlockNode tcn;
		public Block start;
		public Block end;
		public Block handler;
	}

	private Map<Label, Block> map = new HashMap<Label, Block>();

	private List<Tcb> tcbs = new ArrayList<Tcb>();

	private Block first;

	private void addToMap(Block block) {
		map.put(block.label, block);
		if (block.id == 0) {
			first = block;
		}
	}

	private void cut(MethodNode method) {
		InsnList insns = method.instructions;
		{
			AbstractInsnNode p = method.instructions.getFirst();
			if (!(p instanceof LabelNode)) {
				insns.insertBefore(p, new LabelNode());
			}
			AbstractInsnNode q = method.instructions.getLast();
			if (!(q instanceof LabelNode)) {
				insns.insert(q, new LabelNode());
			}
		}
		Map<Label, Block> preBlockMap = new HashMap<Label, Block>();
		int i = 0;
		Label label = null;
		Block block = null;
		InsnList currentInsnList = null;
		for (AbstractInsnNode p = insns.getFirst(); p != null; p = p.getNext()) {
			switch (p.getType()) {
			case AbstractInsnNode.LABEL: {
				if (label != null) {
					block = new Block(i++, label);
					block.insns = currentInsnList;
					block.next = ((LabelNode) p).getLabel();
					addToMap(block);
				}
				currentInsnList = new InsnList();
				label = ((LabelNode) p).getLabel();
				preBlockMap.put(label, block);
				break;
			}
			case AbstractInsnNode.JUMP_INSN:
			case AbstractInsnNode.LOOKUPSWITCH_INSN:
			case AbstractInsnNode.TABLESWITCH_INSN: {
				if (p.getOpcode() == GOTO) {
					block = new Block(i++, label);
					block.next = ((JumpInsnNode) p).label.getLabel();
				} else {//
					block = new BranchBlock(i++, label, p);
					block.next = getNextLabelNode(p, insns).getLabel();
				}
				block.insns = currentInsnList;
				addToMap(block);
				currentInsnList = null;
				label = null;
				break;
			}
			default:
				switch (p.getOpcode()) {
				case IRETURN:
				case LRETURN:
				case FRETURN:
				case DRETURN:
				case ARETURN:
				case RETURN:
				case ATHROW:
					block = new EndBlock(i++, label, p);
					getNextLabelNode(p, insns).getLabel();
					block.insns = currentInsnList;
					addToMap(block);
					currentInsnList = null;
					label = null;
					break;
				default:
					currentInsnList.add(p.clone(null));
				}

			}
		}

		for (Iterator<?> it = method.tryCatchBlocks.iterator(); it.hasNext();) {
			TryCatchBlockNode tcn = (TryCatchBlockNode) it.next();
			Tcb tcb = new Tcb();
			tcbs.add(tcb);
			tcb.tcn = tcn;
			tcb.start = map.get(tcn.start.getLabel());
			tcb.end = preBlockMap.get(tcn.end);
			tcb.handler = map.get(tcn.handler.getLabel());
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

	/**
	 * @param method
	 */
	@SuppressWarnings("rawtypes")
	private void rebuild(MethodNode method) {
		InsnList insnList = new InsnList();
		method.instructions = insnList;
		List<Label> visited = new ArrayList<Label>();
		doRebuild(insnList, first, visited);
		method.tryCatchBlocks = new ArrayList();
		for (Tcb tcb : tcbs) {
			TryCatchBlockNode tcn = tcb.tcn;
			tcn.start = new LabelNode(tcb.start.label);
			tcn.handler = new LabelNode(tcb.handler.label);
			int index = visited.indexOf(tcb.end.label);
			tcn.end = new LabelNode(visited.get(index + 1));
			doRebuild(insnList, tcb.handler, visited);
		}
	}

	private void doRebuild(InsnList insnList, Block b, List<Label> visited) {
		if (b == null) {
			return;
		}
		if (visited.contains(b.label)) {
			return;
		}
		visited.add(b.label);
		insnList.add(new LabelNode(b.label));
		for (AbstractInsnNode p = b.insns.getFirst(); p != null; p = p.getNext()) {
			insnList.add(p.clone(null));
		}
		if (b instanceof BranchBlock) {
			BranchBlock bb = (BranchBlock) b;
			switch (bb.branchInsn.getType()) {
			case AbstractInsnNode.JUMP_INSN:
				JumpInsnNode jump = (JumpInsnNode) bb.branchInsn;
				insnList.add(jump);
				if (visited.contains(bb.next)) {
					insnList.add(new JumpInsnNode(GOTO, new LabelNode(bb.next)));
				} else {
					doRebuild(insnList, map.get(bb.next), visited);
				}
				doRebuild(insnList, map.get(jump.label.getLabel()), visited);
				return;
			case AbstractInsnNode.LOOKUPSWITCH_INSN: {
				LookupSwitchInsnNode ls = (LookupSwitchInsnNode) bb.branchInsn;
				insnList.add(ls);
				doRebuild(insnList, map.get(ls.dflt.getLabel()), visited);
				for (Iterator<?> it = ls.labels.iterator(); it.hasNext();) {
					doRebuild(insnList, map.get(((LabelNode) it.next()).getLabel()), visited);
				}
				doRebuild(insnList, map.get(bb.next), visited);
				return;
			}
			case AbstractInsnNode.TABLESWITCH_INSN: {
				LookupSwitchInsnNode ts = (LookupSwitchInsnNode) bb.branchInsn;
				insnList.add(ts);
				doRebuild(insnList, map.get(ts.dflt.getLabel()), visited);
				for (Iterator<?> it = ts.labels.iterator(); it.hasNext();) {
					doRebuild(insnList, map.get(((LabelNode) it.next()).getLabel()), visited);
				}
				doRebuild(insnList, map.get(bb.next), visited);
				return;
			}
			}
		} else {
			if (b instanceof EndBlock) {
				EndBlock eb = (EndBlock) b;
				insnList.add(eb.endInsn);
			}
			if (b.next != null) {
				if (visited.contains(b.next)) {
					insnList.add(new JumpInsnNode(GOTO, new LabelNode(b.next)));
				} else {
					doRebuild(insnList, map.get(b.next), visited);
				}
			}
		}
	}
}
