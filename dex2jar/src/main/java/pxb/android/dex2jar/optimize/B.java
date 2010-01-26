/*
 * Copyright (c) 2009-2010 Panxiaobo
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import static pxb.android.dex2jar.optimize.Util.isRead;
import static pxb.android.dex2jar.optimize.Util.isSameVar;
import static pxb.android.dex2jar.optimize.Util.isWrite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
@SuppressWarnings("unchecked")
public class B extends MethodTransformerAdapter implements Opcodes {

	private static final Logger log = LoggerFactory.getLogger(MethodTransformerAdapter.class);
	Method m;

	public B(Method m, MethodTransformer tr) {
		super(tr);
		this.m = m;
	}

	public static class Block {
		public LabelNode first;
		public LabelNode last;
		public XList<AbstractInsnNode> in;
		public XList<AbstractInsnNode> out;

		public List<Block> froms = new ArrayList();
		public List<Block> tos = new ArrayList();

		public Block(int index) {
			this.index = index;
		}

		private final int index;

		public String toString() {
			TraceMethodVisitor tr = new TraceMethodVisitor();
			AbstractInsnNode p = first.getNext();
			while (p != last) {
				p.accept(tr);
				p = p.getNext();
			}
			StringBuilder sb = new StringBuilder().append("this:").append(index);
			StringBuilder temp = new StringBuilder();
			for (Block b : froms) {
				temp.append(',').append(b.index);
			}
			if (temp.length() > 0) {
				temp.deleteCharAt(0);
			}
			sb.append("\nfrom:").append('[').append(temp.toString()).append(']');

			temp.setLength(0);
			for (Block b : tos) {
				temp.append(',').append(b.index);
			}
			if (temp.length() > 0) {
				temp.deleteCharAt(0);
			}
			sb.append("\nto:").append('[').append(temp.toString()).append(']');
			temp.setLength(0);
			for (int i = 0; i < in.size(); i++) {
				if (in.get(i) != null) {
					temp.append(',').append(i);
				}
			}
			if (temp.length() > 0) {
				temp.deleteCharAt(0);
			}
			sb.append("\nin:").append('[').append(temp.toString()).append(']');

			temp.setLength(0);
			for (int i = 0; i < out.size(); i++) {
				if (out.get(i) != null) {
					temp.append(',').append(i);
				}
			}
			if (temp.length() > 0) {
				temp.deleteCharAt(0);
			}
			sb.append("\nout:").append('[').append(temp.toString()).append(']');

			sb.append("\n{\n");
			int i = 0;
			for (Object o : tr.text) {
				sb.append(String.format("%04d", i++)).append(o);
			}
			sb.append("}");
			return sb.toString();
		}
	}

	InsnList insnList;
	TraceMethodVisitor tr = new TraceMethodVisitor();
	MethodNode method;

	private static boolean needBreak(AbstractInsnNode ins) {
		switch (ins.getType()) {
		case AbstractInsnNode.JUMP_INSN:
		case AbstractInsnNode.LOOKUPSWITCH_INSN:
		case AbstractInsnNode.TABLESWITCH_INSN:
		case AbstractInsnNode.LABEL:
			return true;
		}
		return false;
	}

	List<Block> blocks = new ArrayList();
	Map<Label, Block> blockMaps = new HashMap();

	int max = -1;

	private int order() {
		return ++max;
	}

	public void max(int max) {
		if (max > this.max) {
			this.max = max;
		}
	}

	@Override
	public void transform(MethodNode method) {

		long timeStart = System.currentTimeMillis();

		log.debug("enter {}", m);
		int blockIndex = 0;
		insnList = method.instructions;
		this.method = method;
		// dump();

		XList<AbstractInsnNode> in = new XList();
		XList<AbstractInsnNode> out = new XList();

		AbstractInsnNode p = method.instructions.getFirst();

		if (!(p instanceof LabelNode)) {
			insnList.insertBefore(p, new LabelNode());
		}

		AbstractInsnNode q = method.instructions.getLast();
		if (!(q instanceof LabelNode)) {
			insnList.insert(q, new LabelNode());
		}

		AbstractInsnNode first = method.instructions.getFirst();
		p = first.getNext();
		// Blocks
		while (p != null) {
			if (isRead(p)) {
				int r = var(p);
				max(r);
				if (in.get(r) == null) {
					if (out.get(r) == null) {
						in.put(r, p);
					}
				}
			} else if (isWrite(p)) {
				int r = var(p);
				max(r);
				out.put(r, p);
			} else if (needBreak(p)) {
				if (p.getType() != AbstractInsnNode.LABEL) {
					q = p.getNext();
					if (q != null && q.getType() == AbstractInsnNode.LABEL) {
						p = q;
					} else {
						method.instructions.insert(p, new LabelNode());
						p = p.getNext();
					}
				}
				Block block = new Block(blockIndex++);
				block.first = (LabelNode) first;
				block.last = (LabelNode) p;
				block.in = in;
				block.out = out;
				blocks.add(block);
				blockMaps.put(block.first.getLabel(), block);
				first = p;
				in = new XList();
				out = new XList();
			}
			p = p.getNext();
		}

		linkTryCatch();

		linkBlocks();

		long timeSpend = System.currentTimeMillis() - timeStart;
		log.debug("spend ({}ms) init", timeSpend);

		for (Block block : blocks) {
			optmizeOut(block);
			doBlock(block);
		}

		// changeLdc_0(blocks);
		super.transform(method);

		timeSpend = System.currentTimeMillis() - timeStart;
		if (timeSpend > 500) {
			log.debug("spend {}s,({}ms) {}", new Object[] { timeSpend / 1000, timeSpend, m.toString() });
		}
	}

	/**
	 * 优化块的输出变量
	 * 
	 * @param block
	 */
	private static void optmizeOut(Block block) {
		// TODO to slow

		Set<Object> mask = new HashSet();
		for (int i = 0; i < block.out.size(); i++) {
			if (block.out.get(i) != null) {
				boolean read = doOptmizeOut(i, block, mask, false);
				if (!read) {
					block.out.put(i, null);
				}
			}
		}
	}

	/**
	 * 递归检查变量是否被读取
	 * 
	 * @param i
	 *            变量的编号
	 * @param block
	 *            块
	 * @param mask
	 *            结束递归的状态
	 * @param checkThis
	 * @return 是否被读取
	 */
	private static boolean doOptmizeOut(int i, Block block, Set<Object> mask, boolean checkThis) {
		if (checkThis) {
			Object o = block.in.get(i);
			if (o != null)
				return true;
		}
		boolean result = false;
		if (!mask.contains(block)) {
			mask.add(block);
			for (Block n : block.tos) {
				result = doOptmizeOut(i, n, mask, true);
				if (result) {
					break;
				}
			}
			mask.remove(block);
		}
		return result;
	}

	private void linkBlocks() {
		for (int i = 0; i < blocks.size(); i++) {
			Block block = blocks.get(i);
			AbstractInsnNode node = block.last.getPrevious();
			switch (node.getType()) {
			case AbstractInsnNode.JUMP_INSN: {
				JumpInsnNode jump = (JumpInsnNode) node;
				link(block, blockMaps.get(jump.label.getLabel()));
				if (jump.getOpcode() != GOTO) {
					link(block, blocks.get(i + 1));
				}
				break;
			}
			case AbstractInsnNode.LOOKUPSWITCH_INSN: {
				LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) node;
				link(block, blockMaps.get(lsin.dflt.getLabel()));
				for (Object l : lsin.labels) {
					LabelNode label = (LabelNode) l;
					link(block, blockMaps.get(label.getLabel()));
				}
				break;
			}
			case AbstractInsnNode.TABLESWITCH_INSN: {
				TableSwitchInsnNode tsin = (TableSwitchInsnNode) node;
				link(block, blockMaps.get(tsin.dflt.getLabel()));
				for (Object l : tsin.labels) {
					LabelNode label = (LabelNode) l;
					link(block, blockMaps.get(label.getLabel()));
				}
				break;
			}
			default: {
				int insnOpcode = node.getOpcode();
				if (insnOpcode != ATHROW && (insnOpcode < IRETURN || insnOpcode > RETURN)) {
					link(block, blocks.get(i + 1));
				}
				break;
			}
			}
		}
	}

	private void linkTryCatch() {
		for (Object o : method.tryCatchBlocks) {
			TryCatchBlockNode tcb = (TryCatchBlockNode) o;
			Block b_handle = blockMaps.get(tcb.handler.getLabel());
			int i = 0;
			while (i < blocks.size()) {
				Block block = blocks.get(i);
				if (block.first == tcb.start) {
					break;
				}
				i++;
			}
			while (i < blocks.size()) {
				Block block = blocks.get(i);
				if (block.first == tcb.end) {
					break;
				}
				link(block, b_handle);
				i++;
			}

		}
	}

	private static void link(Block from, Block to) {

		from.tos.add(to);
		to.froms.add(from);
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

	static private int var(AbstractInsnNode p) {
		return ((VarInsnNode) p).var;
	}

	static private void var(AbstractInsnNode p, int r) {
		((VarInsnNode) p).var = r;
	}

	public void dump() {
		insnList.accept(tr);
		for (Object o : tr.text) {
			System.out.print(o);
		}
		tr.text.clear();
	}

	/**
	 * 
	 * 
	 * 调整NEW指令和对应的INVOKESPECIAL指令的位置，使其与直接编译的指令顺序一样
	 * 
	 * <pre>
	 * BEFORE:
	 *     NEW Ljava/util/PropertyResourceBundle;
	 *     ASTORE 0
	 *     LDC Ljavax/servlet/GenericServlet;.class
	 *     ASTORE 1
	 *     LDC "/javax/servlet/LocalStrings.properties"
	 *     ASTORE 2
	 *     ALOAD 1
	 *     ALOAD 2
	 *     INVOKEVIRTUAL Ljava/lang/Class;.getResourceAsStream (Ljava/lang/String;)Ljava/io/InputStream;
	 *     ASTORE 1
	 *     ALOAD 0
	 *     ALOAD 1
	 *     INVOKESPECIAL Ljava/util/PropertyResourceBundle;.&lt;init> (Ljava/io/InputStream;)V
	 *     ALOAD 0
	 *     PUTSTATIC Ljavax/servlet/GenericServlet;.lStrings : Ljava/util/ResourceBundle;
	 * </pre>
	 * 
	 * 
	 * <pre>
	 * AFTER:
	 *     LDC Ljavax/servlet/GenericServlet;.class
	 *     ASTORE 1
	 *     LDC "/javax/servlet/LocalStrings.properties"
	 *     ASTORE 2
	 *     ALOAD 1
	 *     ALOAD 2
	 *     INVOKEVIRTUAL Ljava/lang/Class;.getResourceAsStream (Ljava/lang/String;)Ljava/io/InputStream;
	 *     ASTORE 1
	 *     NEW Ljava/util/PropertyResourceBundle;
	 *     DUP
	 *     ALOAD 1
	 *     INVOKESPECIAL Ljava/util/PropertyResourceBundle;.&lt;init> (Ljava/io/InputStream;)V
	 *     ASTORE 0
	 *     ALOAD 0
	 *     PUTSTATIC Ljavax/servlet/GenericServlet;.lStrings : Ljava/util/ResourceBundle;
	 * </pre>
	 * 
	 * @param block
	 */
	protected void doNew(Block block) {
		Map<String, AbstractInsnNode> map = new HashMap();
		AbstractInsnNode p = block.first.getNext();
		while (p != null && p != block.last) {
			switch (p.getOpcode()) {
			case Opcodes.NEW: {
				AbstractInsnNode store = p.getNext();
				if (store instanceof VarInsnNode) {
					map.put(((TypeInsnNode) p).desc + var(store), p);
					p = store.getNext();
				} else {
					p = store;
				}
				break;
			}
			case Opcodes.INVOKESPECIAL: {
				MethodInsnNode m = (MethodInsnNode) p;
				p = p.getNext();
				if (m.name.equals("<init>")) {
					int length = Type.getArgumentTypes(m.desc).length;
					AbstractInsnNode q = m.getPrevious();
					while (length-- > 0) {
						q = q.getPrevious();
					}
					AbstractInsnNode _new = map.remove(m.owner + var(q));
					if (_new != null) {
						AbstractInsnNode _store = _new.getNext();
						insnList.remove(_new);// remove new
						insnList.remove(_store); // remove store
						insnList.insertBefore(q, _new);
						insnList.insert(_new, new InsnNode(DUP));
						insnList.remove(q);
						insnList.insert(m, _store);
					}
				}
				break;
			}
			default:
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
		System.out.println();
	}

	protected void doLdc(Block block) {
		AbstractInsnNode p = block.first.getNext();
		while (p != null && p != block.last) {
			if (p.getOpcode() == Opcodes.LDC) {
				p = doLdc(p, block);
			} else {
				p = p.getNext();
			}
		}
	}

	protected void doVar(Block block) {
		AbstractInsnNode p = block.first.getNext();
		while (p != null && p != block.last) {
			if (isWrite(p)) {
				AbstractInsnNode q = p.getNext();
				if (isRead(q) && isSameVar(p, q)) {
					int var = var(p);
					if (block.out.get(var) != null && block.out.get(var) == p) {
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
		System.out.println("BEFORE");
		dump(block);

		doNew(block);
		dump(block);

		doLdc(block);
		dump(block);

		doVar(block);
		dump(block);
		doReIndex(block);

		System.out.println("AFTER");
		dump(block);
	}

	private void doReIndex(Block block) {
		Map<Integer, Integer> map = new HashMap();
		for (AbstractInsnNode p = block.first; p != block.last; p = p.getNext()) {
			if (isWrite(p)) {
				int r = var(p);
				if (block.out.get(r) == null) {// 不输出
					int nr = order();
					var(p, nr);
					map.put(r, nr);
				}
			} else if (isRead(p)) {
				int r = var(p);
				if (block.in.get(r) == null && block.out.get(r) == null) {
					var(p, map.get(r));
				}
			}
		}
	}

	private AbstractInsnNode doLdc(AbstractInsnNode _ldc, Block block) {
		AbstractInsnNode _store = _ldc.getNext();
		if (!isWrite(_store))
			return _store;

		int var = var(_store);
		if (block.out.get(var) == _store)
			return _store.getNext();
		AbstractInsnNode p = _store.getNext();
		AbstractInsnNode pre = _ldc.getPrevious();
		insnList.remove(_ldc);
		insnList.remove(_store);
		while (p != null && p != block.last) {
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
}
