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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Panxiaobo [pxb1988 at gmail.com]
 * 
 */
public class TypeDetectTransformer implements MethodTransformer, Opcodes {

    String owner;

    /**
     * @param owner
     */
    public TypeDetectTransformer(String owner) {
        super();
        this.owner = owner;
    }

    static class ValueBox {
        public Value value;

        /**
         * @param local
         */
        public ValueBox(Value local) {
            super();
            this.value = local;
        }
    }

    static class Value {
        public Type type;
        boolean noTouch = false;

        /**
         * @param index
         * @param noTouch
         */
        public Value(Type type, boolean noTouch) {
            super();
            this.type = type;
            this.noTouch = noTouch;
        }
    }

    Stack<ValueBox>[] stacks;
    ValueBox[] vbs;

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.optimize.MethodTransformer#transform(org.objectweb.asm.tree.MethodNode)
     */
    @Override
    public void transform(MethodNode method) {
        il = method.instructions;
        frames = new HashMap[il.size()];
        stacks = new Stack[il.size()];
        int index = 0;
        Map<Integer, ValueBox> init = new HashMap();
        if ((method.access & ACC_STATIC) == 0) {
            ValueBox vb = new ValueBox(new Value(Type.getType(this.owner), true));
            init.put(index, vb);
            reIndexLocals.put(index, vb);
            index++;
        }
        for (Type arg : Type.getArgumentTypes(method.desc)) {
            ValueBox vb = new ValueBox(new Value(arg, true));
            init.put(index, vb);
            reIndexLocals.put(index, vb);
            index++;
        }
        frames[0] = init;
        AbstractInsnNode[] nodes = il.toArray();
        vbs = new ValueBox[nodes.length];
        Set<LabelNode>[] exs = new Set[nodes.length];

        for (Iterator it = method.tryCatchBlocks.iterator(); it.hasNext();) {
            TryCatchBlockNode tcbn = (TryCatchBlockNode) it.next();
            for (AbstractInsnNode p = tcbn.start.getNext(); p != null && p != tcbn.end; p = p.getNext()) {
                int i = il.indexOf(p);
                Set<LabelNode> set = exs[i];
                if (set == null) {
                    set = new HashSet();
                    exs[i] = set;
                }
                set.add(tcbn.handler);
            }
        }

        index = Short.MAX_VALUE;

        Type ret = Type.getReturnType(method.desc);
        Map<Integer, ValueBox> tmp = new HashMap();
        Stack<ValueBox> afterStack = new Stack();
        for (int i = 0; i < nodes.length; i++) {
            if (frames[i] == null) {
                frames[i] = new HashMap();
            }
            if (stacks[i] == null) {
                stacks[i] = new Stack();
            }
            AbstractInsnNode node = nodes[i];
            Map<Integer, ValueBox> frame = frames[i];
            Stack<ValueBox> stack = stacks[i];
            tmp.clear();
            afterStack.clear();
            tmp.putAll(frame);
            afterStack.addAll(stack);

            if (node.getOpcode() >= 172 && node.getOpcode() <= 176) {
                int nop = ret.getOpcode(IRETURN);
                if (nop != node.getOpcode()) {
                    InsnNode nNode = new InsnNode(nop);
                    il.set(node, nNode);
                    node = nNode;
                    nodes[i] = nNode;
                }
            }

            exec(node, tmp, afterStack, ret);

            merge(node, tmp, afterStack, exs[i]);

        }

        Map<Integer, Integer> reMap = new HashMap();
        int reIndex = 0;
        for (int i = 0; !reIndexLocals.isEmpty(); i++) {
            ValueBox vb = reIndexLocals.get(i);
            if (vb != null) {
                reIndexLocals.remove(i);
                Type type = vb.value.type;
                reMap.put(i, reIndex);
                if (type != null && type.getSize() == 2) {
                    reIndex += 2;
                } else {
                    reIndex += 1;
                }
            }
        }

        for (int i = 0; i < vbs.length; i++) {
            ValueBox vb = vbs[i];
            if (vb != null) {
                Type type = vb.value.type;
                AbstractInsnNode node = il.get(i);
                if (Util.isRead(node) || Util.isWrite(node)) {
                    int nop = type.getOpcode(Util.isWrite(node) ? ISTORE : ILOAD);
                    if (nop != node.getOpcode()) {
                        VarInsnNode node2 = new VarInsnNode(nop, reMap.get(Util.var(node)));
                        il.set(node, node2);
                    } else {
                        Util.var(node, reMap.get(Util.var(node)));
                    }
                } else if (node.getType() == AbstractInsnNode.JUMP_INSN) {
                    int op = node.getOpcode();
                    if (type.equals(Type.INT_TYPE)) {
                        if (op == IFNULL || op == IFNONNULL) {
                            JumpInsnNode node2 = new JumpInsnNode(op == IFNULL ? IFEQ : IFNE, ((JumpInsnNode) node).label);
                            il.set(node, node2);
                        }
                    } else {
                        if (op == IFEQ || op == IFNE) {
                            JumpInsnNode node2 = new JumpInsnNode(op == IFEQ ? IFNULL : IFNONNULL, ((JumpInsnNode) node).label);
                            il.set(node, node2);
                        }
                    }
                } else if (type != null) {// ldc
                    LdcInsnNode ldc = (LdcInsnNode) node;
                    switch (type.getSort()) {
                    case Type.INT:
                        break;
                    case Type.OBJECT:
                        ldc.cst = null;
                        break;
                    case Type.FLOAT:
                        ldc.cst = new Float(Float.intBitsToFloat((Integer) ldc.cst));
                        break;
                    case Type.LONG:
                        break;
                    case Type.DOUBLE:
                        ldc.cst = new Double(Double.longBitsToDouble((Long) ldc.cst));
                        break;
                    }
                }
            }
        }

    }

    void req(ValueBox v, Type type) {
        Type vt = v.value.type;
        if (vt == null) {
            v.value.type = type;
        } else if (!type.equals(vt)) {
            // if (type.getSort() != vt.getSort()) {
            // System.out.println("123");
            // }
        }
    }

    Map<Integer, ValueBox> reIndexLocals = new HashMap();

    /**
     * @param node
     * @param tmp
     * @param afterStack
     */
    private void exec(AbstractInsnNode node, Map<Integer, ValueBox> locals, Stack<ValueBox> stack, Type returnType) {
        switch (node.getType()) {
        case AbstractInsnNode.FIELD_INSN:
            FieldInsnNode fin = (FieldInsnNode) node;
            switch (node.getOpcode()) {
            case GETFIELD:
                req(stack.pop(), Type.getType(fin.owner));
                stack.push(new ValueBox(new Value(Type.getType(fin.desc), true)));
                break;
            case GETSTATIC:
                stack.push(new ValueBox(new Value(Type.getType(fin.desc), true)));
                break;
            case PUTFIELD:
                req(stack.pop(), Type.getType(fin.desc));
                req(stack.pop(), Type.getType(fin.owner));
                break;
            case PUTSTATIC:
                req(stack.pop(), Type.getType(fin.desc));
                break;
            }
            break;
        case AbstractInsnNode.LINE:
        case AbstractInsnNode.LABEL:
            break;
        case AbstractInsnNode.IINC_INSN:
            IincInsnNode iin = (IincInsnNode) node;
            req(locals.get(iin.var), Type.INT_TYPE);
            break;
        case AbstractInsnNode.LOOKUPSWITCH_INSN:
        case AbstractInsnNode.TABLESWITCH_INSN:
            req(stack.pop(), Type.INT_TYPE);
            break;
        case AbstractInsnNode.TYPE_INSN:
            TypeInsnNode tin = (TypeInsnNode) node;
            switch (node.getOpcode()) {
            case NEW:
                stack.push(new ValueBox(new Value(Type.getType(tin.desc), true)));
                break;
            case ANEWARRAY:
                req(stack.pop(), Type.INT_TYPE);
                stack.push(new ValueBox(new Value(Type.getType(tin.desc), true)));
                break;
            case CHECKCAST:
                req(stack.pop(), Type.getType(Object.class));
                stack.push(new ValueBox(new Value(Type.getType(tin.desc), true)));
                break;
            case INSTANCEOF:
                req(stack.pop(), Type.getType(Object.class));
                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            }
            break;
        case AbstractInsnNode.METHOD_INSN:
            MethodInsnNode min = (MethodInsnNode) node;
            Type[] args = Type.getArgumentTypes(min.desc);
            for (int i = args.length - 1; i >= 0; i--) {
                req(stack.pop(), args[i]);
            }
            if (node.getOpcode() != INVOKESTATIC) {
                req(stack.pop(), Type.getType(min.owner));
            }
            Type ret = Type.getReturnType(min.desc);
            if (!ret.equals(Type.VOID_TYPE)) {
                stack.push(new ValueBox(new Value(ret, true)));
            }
            break;
        case AbstractInsnNode.VAR_INSN:
            VarInsnNode vin = (VarInsnNode) node;

            switch (node.getOpcode()) {
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD: {
                ValueBox vb = locals.get(vin.var);
                stack.push(vb);
                vbs[il.indexOf(node)] = vb;
                reIndexLocals.put(vin.var, vb);
            }
                break;
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE: {
                ValueBox vb = stack.pop();
                locals.put(vin.var, vb);
                vbs[il.indexOf(node)] = vb;
                reIndexLocals.put(vin.var, vb);
            }
                break;
            case RET:
            }
            break;
        case AbstractInsnNode.LDC_INSN:
            LdcInsnNode lin = (LdcInsnNode) node;
            if (lin.cst instanceof String) {
                stack.push(new ValueBox(new Value(Type.getType(String.class), true)));
            } else if (lin.cst instanceof Type) {
                stack.push(new ValueBox(new Value(Type.getType(Class.class), true)));
            } else {
                ValueBox vb = new ValueBox(new Value(null, false));
                vbs[il.indexOf(node)] = vb;
                stack.push(vb);
            }
            break;
        case AbstractInsnNode.MULTIANEWARRAY_INSN:
            MultiANewArrayInsnNode maain = (MultiANewArrayInsnNode) node;
            for (int i = 0; i < maain.dims; i++) {
                req(stack.pop(), Type.INT_TYPE);
            }
            stack.push(new ValueBox(new Value(Type.getType(maain.desc), true)));
            break;
        case AbstractInsnNode.INT_INSN:
            if (node.getOpcode() == NEWARRAY) {
                req(stack.pop(), Type.INT_TYPE);
                final Type[] types = new Type[] { Type.BOOLEAN_TYPE, Type.CHAR_TYPE, Type.FLOAT_TYPE, Type.DOUBLE_TYPE, Type.BYTE_TYPE, Type.SHORT_TYPE,
                        Type.INT_TYPE, Type.LONG_TYPE };
                stack.push(new ValueBox(new Value(Type.getType('[' + types[((IntInsnNode) node).operand - 4].getDescriptor()), true)));
            }
            break;
        case AbstractInsnNode.INSN:
            switch (node.getOpcode()) {
            case NOP:
                break;
            case ACONST_NULL:
                stack.push(new ValueBox(new Value(null, false)));
                break;
            case IALOAD:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[I"));
                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            case LALOAD:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[J"));
                stack.push(new ValueBox(new Value(Type.LONG_TYPE, true)));
                break;
            case FALOAD:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[F"));
                stack.push(new ValueBox(new Value(Type.FLOAT_TYPE, true)));
                break;
            case DALOAD:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[D"));
                stack.push(new ValueBox(new Value(Type.DOUBLE_TYPE, true)));
                break;
            case AALOAD:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[Ljava/lang/Object;"));
                stack.push(new ValueBox(new Value(Type.getType("Ljava/lang/Object;"), true)));
                break;
            case BALOAD:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[B"));
                stack.push(new ValueBox(new Value(Type.BYTE_TYPE, true)));
                break;
            case CALOAD:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[C"));
                stack.push(new ValueBox(new Value(Type.CHAR_TYPE, true)));
                break;
            case SALOAD:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[S"));
                stack.push(new ValueBox(new Value(Type.SHORT_TYPE, true)));
                break;
            case IASTORE:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[I"));
                break;
            case LASTORE:
                req(stack.pop(), Type.LONG_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[J"));
                break;
            case FASTORE:
                req(stack.pop(), Type.FLOAT_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[F"));
                break;
            case DASTORE:
                req(stack.pop(), Type.DOUBLE_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[D"));
                break;
            case AASTORE:
                req(stack.pop(), Type.getType("Ljava/lang/Object;"));
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[Ljava/lang/Object;"));
                break;
            case BASTORE:
                req(stack.pop(), Type.BYTE_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[B"));
                break;
            case CASTORE:
                req(stack.pop(), Type.CHAR_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[C"));
                break;
            case SASTORE:
                req(stack.pop(), Type.SHORT_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.getType("[S"));
                break;
            case POP:
            case POP2:
                stack.pop();
                break;
            case DUP:
            case DUP2:
                stack.push(stack.peek());
                break;
            case DUP_X1:
            case DUP_X2:
            case DUP2_X1:
            case DUP2_X2:
                throw new RuntimeException();
            case SWAP: {
                ValueBox vb1 = stack.pop();
                ValueBox vb2 = stack.pop();
                stack.push(vb1);
                stack.push(vb2);
            }
                break;
            case IADD:
            case IDIV:
            case ISUB:

            case IOR:
            case IMUL:
            case IREM:
            case IAND:
            case IXOR:

                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            case LADD:
            case LMUL:
            case LDIV:
            case LREM:
            case LSUB:
            case LAND:
            case LXOR:
            case LOR:
                req(stack.pop(), Type.LONG_TYPE);
                req(stack.pop(), Type.LONG_TYPE);
                stack.push(new ValueBox(new Value(Type.LONG_TYPE, true)));
                break;
            case FADD:
            case FREM:
            case FSUB:
            case FMUL:
            case FDIV:
                req(stack.pop(), Type.FLOAT_TYPE);
                req(stack.pop(), Type.FLOAT_TYPE);
                stack.push(new ValueBox(new Value(Type.FLOAT_TYPE, true)));
                break;
            case DADD:
            case DSUB:
            case DMUL:
            case DDIV:
            case DREM:
                req(stack.pop(), Type.DOUBLE_TYPE);
                req(stack.pop(), Type.DOUBLE_TYPE);
                stack.push(new ValueBox(new Value(Type.DOUBLE_TYPE, true)));
                break;
            case INEG:
                req(stack.peek(), Type.INT_TYPE);
                break;
            case LNEG:
                req(stack.peek(), Type.LONG_TYPE);
                break;
            case FNEG:
                req(stack.peek(), Type.FLOAT_TYPE);
                break;
            case DNEG:
                req(stack.peek(), Type.DOUBLE_TYPE);
                break;
            case IUSHR:
            case ISHL:
            case ISHR:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.peek(), Type.INT_TYPE);
                break;
            case LSHL:
            case LSHR:
            case LUSHR:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.peek(), Type.LONG_TYPE);
                break;
            case I2L:
            case I2F:
            case I2D:
            case L2I:
            case L2F:
            case L2D:
            case F2I:
            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
            case I2B:
            case I2C:
            case I2S:
                switch (node.getOpcode()) {
                case I2L:
                case I2F:
                case I2D:
                case I2B:
                case I2C:
                case I2S:
                    req(stack.pop(), Type.INT_TYPE);
                    break;
                case L2I:
                case L2F:
                case L2D:
                    req(stack.pop(), Type.LONG_TYPE);
                    break;
                case F2I:
                case F2L:
                case F2D:
                    req(stack.pop(), Type.FLOAT_TYPE);
                    break;
                case D2I:
                case D2L:
                case D2F:
                    req(stack.pop(), Type.DOUBLE_TYPE);
                    break;
                }
                switch (node.getOpcode()) {
                case I2F:
                case D2F:
                case L2F:
                    stack.push(new ValueBox(new Value(Type.FLOAT_TYPE, true)));
                    break;
                case I2D:
                case L2D:
                case F2D:
                    stack.push(new ValueBox(new Value(Type.DOUBLE_TYPE, true)));
                    break;
                case L2I:
                case D2I:
                case F2I:
                    stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                    break;
                case F2L:
                case I2L:
                case D2L:
                    stack.push(new ValueBox(new Value(Type.LONG_TYPE, true)));
                    break;
                case I2B:
                    stack.push(new ValueBox(new Value(Type.BYTE_TYPE, true)));
                    break;
                case I2C:
                    stack.push(new ValueBox(new Value(Type.CHAR_TYPE, true)));
                    break;
                case I2S:
                    stack.push(new ValueBox(new Value(Type.SHORT_TYPE, true)));
                    break;
                }
                break;
            case LCMP:
                req(stack.pop(), Type.LONG_TYPE);
                req(stack.pop(), Type.LONG_TYPE);

                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            case FCMPL:
                req(stack.pop(), Type.FLOAT_TYPE);
                req(stack.pop(), Type.FLOAT_TYPE);

                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            case FCMPG:
                req(stack.pop(), Type.FLOAT_TYPE);
                req(stack.pop(), Type.FLOAT_TYPE);

                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            case DCMPL:
                req(stack.pop(), Type.DOUBLE_TYPE);
                req(stack.pop(), Type.DOUBLE_TYPE);

                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            case DCMPG:
                req(stack.pop(), Type.DOUBLE_TYPE);
                req(stack.pop(), Type.DOUBLE_TYPE);

                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
                if (!returnType.equals(Type.VOID_TYPE)) {
                    req(stack.pop(), returnType);
                }
                break;
            case ARRAYLENGTH:
                req(stack.pop(), Type.getType("[Ljava/lang/Object;"));
                stack.push(new ValueBox(new Value(Type.INT_TYPE, true)));
                break;
            case ATHROW:
                break;
            case MONITORENTER:
            case MONITOREXIT:
                req(stack.pop(), Type.getType(Object.class));
                break;
            }
            break;
        case AbstractInsnNode.JUMP_INSN:
            switch (node.getOpcode()) {
            case IFEQ:
            case IFNE:
            case IFNULL:
            case IFNONNULL: {
                ValueBox vb = stack.pop();
                vbs[il.indexOf(node)] = vb;
                break;
            }
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
                req(stack.pop(), Type.INT_TYPE);
                break;
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
                same(stack.pop(), stack.pop());
                break;
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
                req(stack.pop(), Type.INT_TYPE);
                req(stack.pop(), Type.INT_TYPE);
                break;
            case GOTO:
            case JSR:

            }
        }

    }

    /**
     * @param pop
     * @param pop2
     */
    private void same(ValueBox pop, ValueBox pop2) {
        // TODO Auto-generated method stub

    }

    InsnList il;
    Map<Integer, ValueBox>[] frames;

    /**
     * @param il
     * @param node
     * @param tmp
     * @param afterStack
     * @param frames
     * @param exs
     */
    private void merge(AbstractInsnNode node, Map<Integer, ValueBox> tmp, Stack<ValueBox> afterStack, Set<LabelNode> exs) {
        int index = il.indexOf(node);
        int opcode = node.getOpcode();
        if (node.getType() == AbstractInsnNode.JUMP_INSN) {
            if (opcode == GOTO) {
                merge(tmp, afterStack, index, ((JumpInsnNode) node).label);
            } else {
                merge(tmp, afterStack, index, node.getNext());
                merge(tmp, afterStack, index, ((JumpInsnNode) node).label);
            }
        } else if (opcode == LOOKUPSWITCH) {
            LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) node;
            merge(tmp, afterStack, index, lsin.dflt);
            for (Iterator it = lsin.labels.iterator(); it.hasNext();) {
                LabelNode ln = (LabelNode) it.next();
                merge(tmp, afterStack, index, ln);
            }
        } else if (opcode == TABLESWITCH) {
            TableSwitchInsnNode tsin = (TableSwitchInsnNode) node;
            merge(tmp, afterStack, index, tsin.dflt);
            for (Iterator it = tsin.labels.iterator(); it.hasNext();) {
                LabelNode ln = (LabelNode) it.next();
                merge(tmp, afterStack, index, ln);
            }
        } else if (Util.isEnd(node)) {
            //
        } else {
            merge(tmp, afterStack, index, node.getNext());
            if (exs != null) {
                for (LabelNode ln : exs) {
                    merge(tmp, afterStack, index, ln);
                }
            }
        }
    }

    /**
     * @param il
     * @param frames
     * @param tmp
     * @param index
     * @param indexOf
     */
    private void merge(Map<Integer, ValueBox> tmp, Stack<ValueBox> afterStack, int index, AbstractInsnNode dist) {
        if (dist == null) {
            return;
        }
        if (tmp == null) {
            tmp = frames[index];
        }
        int distIndex = il.indexOf(dist);

        Map<Integer, ValueBox> distFrame = frames[distIndex];
        if (distFrame == null) {
            distFrame = new HashMap();
            distFrame.putAll(tmp);
            frames[distIndex] = distFrame;
        } else {
            for (Integer i : tmp.keySet()) {
                ValueBox a = tmp.get(i);
                ValueBox b = distFrame.get(i);
                if (b == null) {
                    distFrame.put(i, a);
                } else if (a.value != b.value) {
                    if (a.value.noTouch) {
                        b.value = a.value;
                    } else if (b.value.noTouch) {
                        a.value = b.value;
                    } else {
                        a.value = b.value;
                    }
                }
            }
        }

        Stack<ValueBox> distStack = stacks[distIndex];
        if (distStack == null) {
            distStack = new Stack();
            distStack.addAll(afterStack);
            stacks[distIndex] = distStack;
        } else {
            for (int i = 0; i < distStack.size(); i++) {
                ValueBox a = afterStack.get(i);
                ValueBox b = distStack.get(i);
                if (b == null) {
                    distFrame.put(i, a);
                } else if (a.value != b.value) {
                    if (a.value.noTouch) {
                        b.value = a.value;
                    } else if (b.value.noTouch) {
                        a.value = b.value;
                    } else {
                        a.value = b.value;
                    }
                }
            }
        }

    }
}
