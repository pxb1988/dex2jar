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

import static pxb.android.dex2jar.optimize.Util.isRead;
import static pxb.android.dex2jar.optimize.Util.isSameVar;
import static pxb.android.dex2jar.optimize.Util.isWrite;
import static pxb.android.dex2jar.optimize.Util.needBreak;
import static pxb.android.dex2jar.optimize.Util.var;

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

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class B implements MethodTransformer, Opcodes {

    private static class Block {
        public LabelNode first;
        public List<Block> froms = new ArrayList<Block>();
        public Map<Integer, AbstractInsnNode> in;
        private final int index;
        public LabelNode last;
        public Map<Integer, AbstractInsnNode> out;

        public List<Block> tos = new ArrayList<Block>();

        public Block(int index) {
            this.index = index;
        }

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
            sb.append(" from:").append('[').append(temp.toString()).append(']');

            temp.setLength(0);
            for (Block b : tos) {
                temp.append(',').append(b.index);
            }
            if (temp.length() > 0) {
                temp.deleteCharAt(0);
            }
            sb.append(" to:").append('[').append(temp.toString()).append(']');
            temp.setLength(0);
            for (Integer i : in.keySet()) {
                if (in.get(i) != null) {
                    temp.append(',').append(i);
                }
            }
            if (temp.length() > 0) {
                temp.deleteCharAt(0);
            }
            sb.append(" in:").append('[').append(temp.toString()).append(']');

            temp.setLength(0);
            for (Integer i : out.keySet()) {
                if (out.get(i) != null) {
                    temp.append(',').append(i);
                }
            }
            if (temp.length() > 0) {
                temp.deleteCharAt(0);
            }
            sb.append(" out:").append('[').append(temp.toString()).append(']');

            sb.append("\n");
            int i = 0;
            for (Object o : tr.text) {
                sb.append(String.format("%04d", i++)).append(o);
            }
            sb.append("");
            return sb.toString();
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
    private static Boolean isOutReaded(Integer i, Block block, Map<Object, Boolean> visited) {

        if (visited.containsKey(block)) {
            return visited.get(block);
        }
        visited.put(block, null);
        Boolean result = null;
        if (block.in.containsKey(i)) {
            result = true;
        } else {
            if (block.out.containsKey(i)) {
                result = false;
            } else {
                for (Block n : block.tos) {
                    Boolean xresult = isOutReaded(i, n, visited);
                    if (xresult == null) {
                    } else if (xresult.booleanValue()) {
                        result = true;
                        break;
                    }
                }
            }
        }
        visited.put(block, result);
        return result;
    }

    private static void link(Block from, Block to) {

        from.tos.add(to);
        to.froms.add(from);
    }

    private Map<Label, Block> blockMaps = new HashMap<Label, Block>();
    private List<Block> blocks = new ArrayList<Block>();
    private Map<Integer, Integer> grobalMap = new HashMap<Integer, Integer>();

    private InsnList insnList;

    private int max = 0;

    private MethodNode method;

    private boolean couldReplace(int r, Block block, Map<Block, Boolean> blocks) {
        for (Block in : block.froms) {
            Boolean x = doCouldReplace(r, in, blocks);
            if (x == null) {
                // ignore
            } else if (!x.booleanValue()) {
                return false;
            }
        }
        return true;
    }

    private void cut() {
        // long timeStart = System.currentTimeMillis();

        // log.debug("enter {}", m);
        int blockIndex = 0;

        // dump();

        Map<Integer, AbstractInsnNode> in = new HashMap<Integer, AbstractInsnNode>();
        Map<Integer, AbstractInsnNode> out = new HashMap<Integer, AbstractInsnNode>();

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
                if (in.get(r) == null) {
                    if (out.get(r) == null) {
                        in.put(r, p);
                    }
                }
            } else if (isWrite(p)) {
                int r = var(p);
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
                in = new HashMap<Integer, AbstractInsnNode>();
                out = new HashMap<Integer, AbstractInsnNode>();
            }
            p = p.getNext();
        }
    }

    private void doBlock(Block block) {
        // System.out.println("BEFORE");
        // dump(block);

        doNew(block);
        // dump(block);

        doLdc(block);
        // dump(block);

        doVar(block);
        // dump(block);
    }

    private Boolean doCouldReplace(int r, Block block, Map<Block, Boolean> blocks) {
        if (blocks.containsKey(block))
            return blocks.get(block);
        if (block.out.containsKey(r)) {
            blocks.put(block, false);
            return false;
        }
        blocks.put(block, null);
        for (Block in : block.froms) {
            Boolean x = doCouldReplace(r, in, blocks);
            if (x == null) {
                // ignore
            } else if (!x.booleanValue()) {
                blocks.put(block, false);
                return false;
            }
        }
        blocks.put(block, true);
        return true;
    }

    /**
     * <pre>
     * BEFORE:
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
     * <pre>
     * AFTER:
     *     LDC Ljavax/servlet/GenericServlet;.class
     *     LDC "/javax/servlet/LocalStrings.properties"
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
    private void doLdc(Block block) {
        Map<Integer, LdcInsnNode> map = new HashMap<Integer, LdcInsnNode>();
        AbstractInsnNode p = block.first.getNext();
        while (p != null && p != block.last) {
            if (p.getOpcode() == Opcodes.LDC) {
                AbstractInsnNode q = p.getNext();
                if (isWrite(q)) {
                    Integer var = var(q);
                    if (block.out.get(var) == null || block.out.get(var) != q) {
                        map.put(var, (LdcInsnNode) p);
                        insnList.remove(q); // remove store
                        q = p.getPrevious();
                        insnList.remove(p); // remove ldc
                        p = q;
                    }
                }
            } else if (isRead(p)) {
                Integer var = var(p);
                if (block.out.get(var) == null || block.out.get(var) != p) {
                    LdcInsnNode ldc = map.get(var);
                    if (ldc != null) {
                        AbstractInsnNode _ldc_copy = ldc.clone(null);
                        insnList.insert(p, _ldc_copy);
                        insnList.remove(p);
                        p = _ldc_copy;
                    }
                }
            } else if (isWrite(p)) {
                Integer var = var(p);
                map.remove(var);
            }
            p = p.getNext();
        }
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
    private void doNew(Block block) {
        Map<String, AbstractInsnNode> map = new HashMap<String, AbstractInsnNode>();
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

    private boolean doOptmizeFirstBlockLdc(LdcInsnNode node, int r, Block block, Map<Block, Boolean> couldReplaceBlockIds, Set<Block> replacedBlockIds) {
        if (replacedBlockIds.contains(block)) {
            return true;
        }
        replacedBlockIds.add(block);
        if (couldReplace(r, block, couldReplaceBlockIds)) {
            AbstractInsnNode p = block.first.getNext();
            while (p != null && p != block.last) {
                if (isRead(p)) {
                    int var = var(p);
                    if (r == var) {
                        LdcInsnNode nLdc = (LdcInsnNode) node.clone(null);
                        AbstractInsnNode q = p.getNext();
                        insnList.remove(p);
                        insnList.insertBefore(q, nLdc);
                        p = q;
                        continue;
                    }
                } else if (isWrite(p)) {
                    int var = var(p);
                    if (r == var) {
                        break;
                    }
                }
                p = p.getNext();
            }
        } else {
            return false;
        }
        boolean remove = true;
        if (!block.out.containsKey(r)) {
            for (Block subBlockId : block.tos) {
                boolean x = doOptmizeFirstBlockLdc(node, r, subBlockId, couldReplaceBlockIds, replacedBlockIds);
                if (!x) {
                    remove = false;
                }
            }
        }
        return remove;
    }

    /**
     * <pre>
     * BEFORE:
     *     LDC Ljavax/servlet/GenericServlet;.class
     *     LDC "/javax/servlet/LocalStrings.properties"
     *     INVOKEVIRTUAL Ljava/lang/Class;.getResourceAsStream (Ljava/lang/String;)Ljava/io/InputStream;
     *     ASTORE 1
     *     NEW Ljava/util/PropertyResourceBundle;
     *     DUP
     *     ALOAD 1
     *     INVOKESPECIAL Ljava/util/PropertyResourceBundle;.&lt;init> (Ljava/io/InputStream;)V
     *     PUTSTATIC Ljavax/servlet/GenericServlet;.lStrings : Ljava/util/ResourceBundle;
     * </pre>
     * 
     * <pre>
     * AFTER:
     *     LDC Ljavax/servlet/GenericServlet;.class
     *     LDC "/javax/servlet/LocalStrings.properties"
     *     INVOKEVIRTUAL Ljava/lang/Class;.getResourceAsStream (Ljava/lang/String;)Ljava/io/InputStream;
     *     ASTORE 4
     *     NEW Ljava/util/PropertyResourceBundle;
     *     DUP
     *     ALOAD 4
     *     INVOKESPECIAL Ljava/util/PropertyResourceBundle;.&lt;init> (Ljava/io/InputStream;)V
     *     PUTSTATIC Ljavax/servlet/GenericServlet;.lStrings : Ljava/util/ResourceBundle;
     * </pre>
     * 
     * @param block
     */
    private void doReIndex(Block block) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (AbstractInsnNode p = block.first; p != block.last; p = p.getNext()) {
            if (isWrite(p)) {
                int r = var(p);
                if (block.out.get(r) != p) {// 不输出
                    int nr = order();
                    var(p, nr);
                    map.put(r, nr);
                } else {
                    Integer v = this.grobalMap.get(r);
                    if (v == null) {
                        v = order();
                        grobalMap.put(r, v);
                    }
                    var(p, v);
                    map.put(r, v);
                }
            } else if (isRead(p)) {
                int r = var(p);
                if (block.in.get(r) != p) {
                    Integer v = map.get(r);
                    if (v == null) {
                        int nr = order();
                        var(p, nr);
                        map.put(r, nr);
                    } else {
                        var(p, v);
                    }
                } else {
                    Integer v = this.grobalMap.get(r);
                    if (v == null) {
                        v = order();
                        grobalMap.put(r, v);
                    }
                    var(p, v);
                    map.put(r, v);
                }
            }
        }

        Map<Integer, AbstractInsnNode> old = block.in;
        block.in = new HashMap<Integer, AbstractInsnNode>();
        for (Map.Entry<Integer, AbstractInsnNode> e : old.entrySet()) {
            block.in.put(this.grobalMap.get(e.getKey()), e.getValue());
        }
        old = block.out;
        block.out = new HashMap<Integer, AbstractInsnNode>();
        for (Map.Entry<Integer, AbstractInsnNode> e : old.entrySet()) {
            block.out.put(this.grobalMap.get(e.getKey()), e.getValue());
        }

    }

    /**
     * <pre>
     * BEFORE:
     *     LDC Ljavax/servlet/GenericServlet;.class
     *     LDC "/javax/servlet/LocalStrings.properties"
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
     * <pre>
     * AFTER:
     *     LDC Ljavax/servlet/GenericServlet;.class
     *     LDC "/javax/servlet/LocalStrings.properties"
     *     INVOKEVIRTUAL Ljava/lang/Class;.getResourceAsStream (Ljava/lang/String;)Ljava/io/InputStream;
     *     ASTORE 1
     *     NEW Ljava/util/PropertyResourceBundle;
     *     DUP
     *     ALOAD 1
     *     INVOKESPECIAL Ljava/util/PropertyResourceBundle;.&lt;init> (Ljava/io/InputStream;)V
     *     PUTSTATIC Ljavax/servlet/GenericServlet;.lStrings : Ljava/util/ResourceBundle;
     * </pre>
     * 
     * @param block
     */
    private void doVar(Block block) {
        AbstractInsnNode p = block.first.getNext();
        while (p != null && p != block.last) {
            if (isWrite(p)) {
                AbstractInsnNode q = p.getNext();
                if (isRead(q)) {
                    if (isSameVar(p, q)) {
                        int var = var(p);
                        boolean canDel = true;
                        for (AbstractInsnNode i = q.getNext(); i != null && i != block.last; i = i.getNext()) {
                            if (isRead(i) && var == var(i)) {
                                canDel = false;
                                break;
                            }
                            if (isWrite(i) && var == var(i)) {
                                canDel = true;
                                break;
                            }
                        }
                        if (canDel && block.out.get(var) != p) {
                            AbstractInsnNode t = q.getNext();
                            insnList.remove(p);
                            insnList.remove(q);
                            p = t.getPrevious();
                        }
                    }
                }
            }
            p = p.getNext();
        }
    }

    // private void dump() {
    // insnList.accept(tr);
    // for (Object o : tr.text) {
    // System.out.print(o);
    // }
    // tr.text.clear();
    // }
    // private TraceMethodVisitor tr = new TraceMethodVisitor();
    // private void dump(Block block) {
    // AbstractInsnNode p = block.first.getNext();
    // while (p != null && !(p instanceof LabelNode)) {
    // p.accept(tr);
    // p = p.getNext();
    // }
    // for (Object o : tr.text) {
    // System.out.print(o);
    // }
    // tr.text.clear();
    // System.out.println();
    // }

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

    /**
     * @param block
     */
    private void OptmizeFirstBlockLdc() {
        Block block = this.blocks.get(0);
        Map<Integer, LdcInsnNode> map = new HashMap<Integer, LdcInsnNode>();
        AbstractInsnNode p = block.first.getNext();
        while (p != null && p != block.last) {
            if (p.getOpcode() == Opcodes.LDC) {
                AbstractInsnNode q = p.getNext();
                if (isWrite(q)) {
                    int var = var(q);
                    if (block.out.get(var) == q) {
                        Map<Block, Boolean> couldReplace = new HashMap<Block, Boolean>();
                        Set<Block> replacedBlock = new HashSet<Block>();
                        replacedBlock.add(block);
                        couldReplace.put(block, true);
                        LdcInsnNode ldc = (LdcInsnNode) p;
                        boolean remove = true;
                        for (Block subBlock : block.tos) {
                            boolean x = doOptmizeFirstBlockLdc(ldc, var, subBlock, couldReplace, replacedBlock);
                            if (!x) {
                                remove = false;
                            }
                        }
                        if (remove) {
                            insnList.remove(p);
                            p = q.getNext();
                            insnList.remove(q);
                            map.put(var, ldc);
                            continue;
                        }

                    }
                }
            } else if (isRead(p)) {
                int var = var(p);
                LdcInsnNode ldc = map.get(var);
                if (ldc != null) {
                    AbstractInsnNode q = p.getNext();
                    insnList.remove(p);
                    insnList.insertBefore(q, ldc.clone(null));
                    p = q;
                    continue;
                }
            }
            p = p.getNext();
        }
    }

    /**
     * 优化块的输出变量
     * 
     * @param block
     */
    private void optmizeOut(Integer i) {
        Map<Object, Boolean> map = new HashMap<Object, Boolean>();
        for (Block block : blocks) {
            if (!block.out.containsKey(i)) {
                continue;
            }
            boolean read = false;
            for (Block n : block.tos) {
                Boolean xresult = isOutReaded(i, n, map);
                if (xresult == null) {
                } else if (xresult.booleanValue()) {
                    read = true;
                    break;
                }
            }
            if (!read) {
                block.out.remove(i);
            }
        }
    }

    private int order() {
        return max++;
    }

    public void transform(MethodNode method) {
        insnList = method.instructions;
        this.method = method;

        cut();

        linkTryCatch();

        linkBlocks();

        Set<Integer> set = new HashSet<Integer>();
        for (Block b : blocks) {
            set.addAll(b.out.keySet());
        }
        for (Integer i : set) {
            optmizeOut(i);
        }
        set = null;

        // for (Block block : blocks) {
        // doZeroNull(block);
        // }
        for (Block block : blocks) {
            doBlock(block);
        }
        OptmizeFirstBlockLdc();
        int i = 0;
        if ((method.access & ACC_STATIC) == 0) {
            grobalMap.put(i++, order()); // this
        }
        for (int j = 0; j < Type.getArgumentTypes(method.desc).length; j++) {
            grobalMap.put(i++, order());
        }

        for (Block block : blocks) {
            doReIndex(block);
        }
    }
}
