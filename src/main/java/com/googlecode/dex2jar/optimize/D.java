/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar.optimize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

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
        public AbstractInsnNode extraInsn;
        public List<AbstractInsnNode> insns = new ArrayList<AbstractInsnNode>();
        final public LabelNode label;
        public LabelNode next;
        public List<Block> fromBlocks = new ArrayList<Block>();
        public Block nextBlock;

        public Block(int id, LabelNode label) {
            this.id = id;
            this.label = label;
        }

        public String toString() {
            return String.format("B%03d", id);
        }
    }

    private static final class BranchBlock extends Block {
        public BranchBlock(int id, LabelNode label, AbstractInsnNode insn) {
            super(id, label);
            this.extraInsn = insn;
        }

        public List<Block> toBlocks = new ArrayList<Block>();
    }

    private static class EndBlock extends Block {
        public EndBlock(int id, LabelNode label, AbstractInsnNode endInsn) {
            super(id, label);
            this.extraInsn = endInsn;
        }

        public EndBlock clone() {
            EndBlock n = new EndBlock(id, new LabelNode(), extraInsn.clone(null));
            List<AbstractInsnNode> insnList = new ArrayList<AbstractInsnNode>();
            for (AbstractInsnNode p : insns) {
                insnList.add(p.clone(null));
            }
            n.insns = insnList;
            return n;
        }
    }

    private static final Comparator<Block> BlockComparator = new Comparator<Block>() {
        @Override
        public int compare(Block o1, Block o2) {
            return o1.id - o2.id;
        }
    };

    private static final Comparator<TcbK> TcbKComparator = new Comparator<TcbK>() {
        @Override
        public int compare(TcbK o1, TcbK o2) {
            int i = o1.start.id - o2.start.id;
            int j = o1.end.id - o2.end.id;
            return i == 0 ? j : i;
        }
    };

    private static class TcbK {
        final public Block start, end;
        final public Map<Block, String> handlers = new TreeMap<Block, String>(BlockComparator);

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

    private Map<Block, Set<TcbK>> tcbs = new HashMap<Block, Set<TcbK>>();

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
            case AbstractInsnNode.JUMP_INSN: {
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
            case AbstractInsnNode.LOOKUPSWITCH_INSN:
            case AbstractInsnNode.TABLESWITCH_INSN: {
                block = new BranchBlock(i++, label, cp);
                block.insns = currentInsnList;
                block.next = cp.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN ? ((LookupSwitchInsnNode) cp).dflt : ((TableSwitchInsnNode) cp).dflt;
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

        for (Block b : map.values()) {
            if (b.next != null) {
                b.nextBlock = map.get(b.next);
                b.nextBlock.fromBlocks.add(b);
            }
            if (b instanceof BranchBlock) {
                BranchBlock bb = (BranchBlock) b;
                switch (bb.extraInsn.getType()) {
                case AbstractInsnNode.LOOKUPSWITCH_INSN:
                case AbstractInsnNode.TABLESWITCH_INSN:
                    List<LabelNode> labels = bb.extraInsn.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN ? ((LookupSwitchInsnNode) bb.extraInsn).labels
                            : ((TableSwitchInsnNode) bb.extraInsn).labels;
                    for (LabelNode ln : labels) {
                        Block t = map.get(ln);
                        bb.toBlocks.add(b);
                        t.fromBlocks.add(bb);
                    }
                    break;
                default:
                    Block t = map.get(((JumpInsnNode) bb.extraInsn).label);
                    bb.toBlocks.add(t);
                    t.fromBlocks.add(bb);
                }
            }

        }

        for (Iterator<?> it = method.tryCatchBlocks.iterator(); it.hasNext();) {
            TryCatchBlockNode tcn = (TryCatchBlockNode) it.next();

            Block s = map.get((LabelNode) tcn.start.clone(cloneMap));
            Block e = map.get((LabelNode) tcn.end.clone(cloneMap));
            Block handler = map.get(tcn.handler.clone(cloneMap));

            Set<TcbK> handlers = tcbs.get(s);
            if (handlers == null) {
                handlers = new TreeSet<TcbK>(TcbKComparator);
                tcbs.put(s, handlers);
            }
            TcbK key = new TcbK(s, e);
            if (!handlers.add(key)) {
                for (TcbK x : handlers) {
                    if (TcbKComparator.compare(key, x) == 0) {
                        key = x;
                        break;
                    }
                }
            }
            key.handlers.put(handler, tcn.type);

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

    private void doRebuild(Stack<Block> toWriteBlock, List<LabelNode> visited) {
        while (!toWriteBlock.empty()) {
            Block b = toWriteBlock.pop();
            if (visited.contains(b.label)) {
                continue;
            }
            visited.add(b.label);
            if (b instanceof BranchBlock) {
                BranchBlock bb = (BranchBlock) b;
                switch (bb.extraInsn.getType()) {
                case AbstractInsnNode.JUMP_INSN:
                    JumpInsnNode jump = (JumpInsnNode) bb.extraInsn;
                    toWriteBlock.push(map.get(jump.label));
                    continue;
                case AbstractInsnNode.LOOKUPSWITCH_INSN:
                case AbstractInsnNode.TABLESWITCH_INSN: {
                    AbstractInsnNode ts = bb.extraInsn;
                    LabelNode dfltLabel = ts.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN ? ((LookupSwitchInsnNode) ts).dflt
                            : ((TableSwitchInsnNode) ts).dflt;
                    @SuppressWarnings("unchecked")
                    List<LabelNode> labels = ts.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN ? ((LookupSwitchInsnNode) ts).labels
                            : ((TableSwitchInsnNode) ts).labels;
                    toWriteBlock.push(map.get(dfltLabel));
                    List<LabelNode> cLables = new ArrayList<LabelNode>(labels);
                    Collections.reverse(cLables);
                    for (LabelNode labelNode : cLables) {
                        toWriteBlock.push(map.get(labelNode));
                    }

                    continue;
                }
                }
            }
        }
    }

    /**
     * 
     * @param method
     * @param blocks
     */
    private static void rebuild(MethodNode method, List<Block> blocks) {
        InsnList insnList = new InsnList();
        method.instructions = insnList;

        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);
            insnList.add(b.label);
            for (AbstractInsnNode p : b.insns) {
                insnList.add(p);
            }
            if (b.extraInsn != null) {
                insnList.add(b.extraInsn);
            }
            if (b.next != null) {
                LabelNode labelNode = i + 1 < blocks.size() ? blocks.get(i + 1).label : null;
                if (!b.next.equals(labelNode)) {// 与下一块的开始地址不一样
                    insnList.add(new JumpInsnNode(GOTO, b.next));
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.optimize.MethodTransformer#transform(org.objectweb.asm.tree.MethodNode)
     */
    @Override
    public void transform(MethodNode method) {
        cut(method);
        List<Block> blocks = reOrder();
        rebuild(method, blocks);
    }

    /**
     * @return
     */
    private List<Block> reOrder() {
        List<Block> blocks = new ArrayList(map.size());
        blocks.addAll(map.values());
        Block current = first;

        return blocks;
    }
}
