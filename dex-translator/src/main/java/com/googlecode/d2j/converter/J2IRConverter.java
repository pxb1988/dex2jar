package com.googlecode.d2j.converter;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.*;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class J2IRConverter {
    Map<Label, LabelStmt> map = new HashMap<>();
    InsnList insnList;
    int[] parentCount;
    JvmFrame[] frames;
    MethodNode methodNode;
    IrMethod target;
    List<Stmt> emitStmts[];
    List<Stmt> preEmit = new ArrayList<>();
    List<Stmt> currentEmit;

    private J2IRConverter() {
    }

    public static IrMethod convert(String owner, MethodNode methodNode) throws AnalyzerException {
        return new J2IRConverter().convert0(owner, methodNode);
    }

    LabelStmt getLabel(LabelNode labelNode) {
        Label label = labelNode.getLabel();
        LabelStmt ls = map.get(label);
        if (ls == null) {
            ls = Stmts.nLabel();
            map.put(label, ls);
        }
        return ls;
    }

    void emit(Stmt stmt) {
        currentEmit.add(stmt);
    }

    IrMethod populate(String owner, MethodNode source) {
        IrMethod target = new IrMethod();
        target.name = source.name;
        target.owner = "L" + owner + ";";
        target.ret = Type.getReturnType(source.desc).getDescriptor();
        Type[] args = Type.getArgumentTypes(source.desc);
        String sArgs[] = new String[args.length];
        target.args = sArgs;
        for (int i = 0; i < args.length; i++) {
            sArgs[i] = args[i].getDescriptor();
        }
        target.isStatic = 0 != (source.access & Opcodes.ACC_STATIC);
        return target;
    }

    IrMethod convert0(String owner, MethodNode methodNode) throws AnalyzerException {
        this.methodNode = methodNode;
        target = populate(owner, methodNode);
        if (methodNode.instructions.size() == 0) {
            return target;
        }

        insnList = methodNode.instructions;
        BitSet[] exBranch = new BitSet[insnList.size()];
        parentCount = new int[insnList.size()];

        initParentCount(parentCount);


        BitSet handlers = new BitSet(insnList.size());
        if (methodNode.tryCatchBlocks != null) {
            for (TryCatchBlockNode tcb : methodNode.tryCatchBlocks) {
                target.traps.add(new Trap(getLabel(tcb.start), getLabel(tcb.end), new LabelStmt[]{getLabel(tcb.handler)},
                        new String[]{tcb.type}));
                int handlerIdx = insnList.indexOf(tcb.handler);
                handlers.set(handlerIdx);

                for (AbstractInsnNode p = tcb.start.getNext(); p != tcb.end; p = p.getNext()) {

                    BitSet x = exBranch[insnList.indexOf(p)];
                    if (x == null) {
                        x = exBranch[insnList.indexOf(p)] = new BitSet(insnList.size());
                    }
                    x.set(handlerIdx);
                    parentCount[handlerIdx]++;
                }
            }
        }

        Interpreter<JvmValue> interpreter = buildInterpreter();
        frames = new JvmFrame[insnList.size()];
        emitStmts = new ArrayList[insnList.size()];
        BitSet access = new BitSet(insnList.size());

        dfs(exBranch, handlers, access, interpreter);

        StmtList stmts = target.stmts;
        stmts.addAll(preEmit);
        for (int i = 0; i < insnList.size(); i++) {
            AbstractInsnNode p = insnList.get(i);
            if (access.get(i)) {
                List<Stmt> es = emitStmts[i];
                if (es != null) {
                    stmts.addAll(es);
                }
            } else {
                if (p.getType() == AbstractInsnNode.LABEL) {
                    stmts.add(getLabel((LabelNode) p));
                }
            }
        }
        emitStmts = null;


        Queue<JvmValue> queue = new LinkedList<>();

        for (int i1 = 0; i1 < frames.length; i1++) {
            JvmFrame frame = frames[i1];
            if (parentCount[i1] > 1 && frame != null && access.get(i1)) {
                for (int j = 0; j < frame.getLocals(); j++) {
                    JvmValue v = frame.getLocal(j);
                    addToQueue(queue, v);
                }
                for (int j = 0; j < frame.getStackSize(); j++) {
                    addToQueue(queue, frame.getStack(j));
                }
            }

        }

        while (!queue.isEmpty()) {
            JvmValue v = queue.poll();
            getLocal(v);
            if (v.parent != null) {
                if (v.parent.local == null) {
                    queue.add(v.parent);
                }
            }
            if (v.otherParent != null) {
                for (JvmValue v2 : v.otherParent) {
                    if (v2.local == null) {
                        queue.add(v2);
                    }
                }
            }
        }

        Set<com.googlecode.dex2jar.ir.expr.Value> phiValues = new HashSet<>();
        List<LabelStmt> phiLabels = new ArrayList<>();
        for (int i = 0; i < frames.length; i++) {
            JvmFrame frame = frames[i];
            if (parentCount[i] > 1 && frame != null && access.get(i)) {
                AbstractInsnNode p = insnList.get(i);
                LabelStmt labelStmt = getLabel((LabelNode) p);
                List<AssignStmt> phis = new ArrayList<>();
                for (int j = 0; j < frame.getLocals(); j++) {
                    JvmValue v = frame.getLocal(j);
                    addPhi(v, phiValues, phis);
                }
                for (int j = 0; j < frame.getStackSize(); j++) {
                    addPhi(frame.getStack(j), phiValues, phis);
                }
                labelStmt.phis = phis;
                phiLabels.add(labelStmt);
            }
        }
        if (phiLabels.size() > 0) {
            target.phiLabels = phiLabels;
        }

        return target;

    }

    private void addPhi(JvmValue v, Set<com.googlecode.dex2jar.ir.expr.Value> phiValues, List<AssignStmt> phis) {
        if (v != null) {
            if (v.local != null) {
                if (v.parent != null) {
                    phiValues.add(getLocal(v.parent));
                }
                if (v.otherParent != null) {
                    for (JvmValue v2 : v.otherParent) {
                        phiValues.add(getLocal(v2));
                    }
                }
                if (phiValues.size() > 0) {
                    phis.add(Stmts.nAssign(v.local, Exprs.nPhi(phiValues.toArray(new com.googlecode.dex2jar.ir.expr.Value[phiValues.size()]))));
                    phiValues.clear();
                }
            }
        }
    }

    private void addToQueue(Queue<JvmValue> queue, JvmValue v) {
        if (v != null) {
            if (v.local != null) {
                if (v.parent != null) {
                    if (v.parent.local == null) {
                        queue.add(v.parent);
                    }
                }
                if (v.otherParent != null) {
                    for (JvmValue v2 : v.otherParent) {
                        if (v2.local == null) {
                            queue.add(v2);
                        }
                    }
                }
            }
        }
    }

    private void dfs(BitSet[] exBranch, BitSet handlers, BitSet access, Interpreter<JvmValue> interpreter) throws AnalyzerException {
        currentEmit = preEmit;
        JvmFrame first = initFirstFrame(methodNode, target);
        if (parentCount[0] > 1) {
            merge(first, 0);
        } else {
            frames[0] = first;
        }
        Stack<AbstractInsnNode> stack = new Stack<>();
        stack.push(insnList.getFirst());

        JvmFrame tmp = new JvmFrame(methodNode.maxLocals, methodNode.maxStack);

        while (!stack.isEmpty()) {
            AbstractInsnNode p = stack.pop();
            int index = insnList.indexOf(p);
            if (!access.get(index)) {
                access.set(index);
            } else {
                continue;
            }
            JvmFrame frame = frames[index];
            setCurrentEmit(index);

            if (p.getType() == AbstractInsnNode.LABEL) {
                emit(getLabel((LabelNode) p));
                if (handlers.get(index)) {
                    Local ex = newLocal();
                    emit(Stmts.nIdentity(ex, Exprs.nExceptionRef("Ljava/lang/Throwable;")));
                    frame.clearStack();
                    frame.push(new JvmValue(1, ex));
                }
            }
            BitSet ex = exBranch[index];
            if (ex != null) {
                for (int i = ex.nextSetBit(0); i >= 0; i = ex.nextSetBit(i + 1)) {
                    mergeEx(frame, i);
                    stack.push(insnList.get(i));
                }
            }

            tmp.init(frame);
            tmp.execute(p, interpreter);

            int op = p.getOpcode();
            if (p.getType() == AbstractInsnNode.JUMP_INSN) {
                JumpInsnNode jump = (JumpInsnNode) p;
                stack.push(jump.label);
                merge(tmp, insnList.indexOf(jump.label));
            }

            if (op == Opcodes.TABLESWITCH || op == Opcodes.LOOKUPSWITCH) {
                if (op == Opcodes.TABLESWITCH) {
                    TableSwitchInsnNode tsin = (TableSwitchInsnNode) p;
                    for (LabelNode label : tsin.labels) {
                        stack.push(label);
                        merge(tmp, insnList.indexOf(label));
                    }
                    stack.push(tsin.dflt);
                    merge(tmp, insnList.indexOf(tsin.dflt));

                } else {
                    LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) p;
                    for (LabelNode label : lsin.labels) {
                        stack.push(label);
                        merge(tmp, insnList.indexOf(label));
                    }
                    stack.push(lsin.dflt);
                    merge(tmp, insnList.indexOf(lsin.dflt));
                }
            }
            if ((op >= Opcodes.GOTO && op <= Opcodes.RETURN) || op == Opcodes.ATHROW) {
                // can't continue
            } else {
                stack.push(p.getNext());
                merge(tmp, index + 1);
            }

            // cleanup frame it is useless
            if (parentCount[index] <= 1) {
                frames[index] = null;
            }

        }
    }

    private void setCurrentEmit(int index) {
        currentEmit = emitStmts[index];
        if (currentEmit == null) {
            currentEmit = emitStmts[index] = new ArrayList<>(1);
        }
    }

    private Interpreter<JvmValue> buildInterpreter() {
        return new Interpreter<JvmValue>(Opcodes.ASM4) {
            @Override
            public JvmValue newValue(Type type) {
                return null;
            }

            @Override
            public JvmValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
                switch (insn.getOpcode()) {
                    case ACONST_NULL:
                        return b(1, Exprs.nNull());
                    case ICONST_M1:
                    case ICONST_0:
                    case ICONST_1:
                    case ICONST_2:
                    case ICONST_3:
                    case ICONST_4:
                    case ICONST_5:
                        return b(1, Exprs.nInt(insn.getOpcode() - ICONST_0));
                    case LCONST_0:
                    case LCONST_1:
                        return b(2, Exprs.nLong(insn.getOpcode() - LCONST_0));
                    case FCONST_0:
                    case FCONST_1:
                    case FCONST_2:
                        return b(1, Exprs.nFloat(insn.getOpcode() - FCONST_0));
                    case DCONST_0:
                    case DCONST_1:
                        return b(2, Exprs.nDouble(insn.getOpcode() - DCONST_0));
                    case BIPUSH:
                    case SIPUSH:
                        return b(1, Exprs.nInt(((IntInsnNode) insn).operand));
                    case LDC:
                        Object cst = ((LdcInsnNode) insn).cst;
                        if (cst instanceof Integer) {
                            return b(1, Exprs.nInt((Integer) cst));
                        } else if (cst instanceof Float) {
                            return b(1, Exprs.nFloat((Float) cst));
                        } else if (cst instanceof Long) {
                            return b(2, Exprs.nLong((Long) cst));
                        } else if (cst instanceof Double) {
                            return b(2, Exprs.nDouble((Double) cst));
                        } else if (cst instanceof String) {
                            return b(1, Exprs.nString((String) cst));
                        } else if (cst instanceof Type) {
                            Type type = (Type) cst;
                            int sort = type.getSort();
                            if (sort == Type.OBJECT || sort == Type.ARRAY) {
                                return b(1, Exprs.nType(type.getDescriptor()));
                            } else if (sort == Type.METHOD) {
                                throw new UnsupportedOperationException("Not supported yet.");
                            } else {
                                throw new IllegalArgumentException("Illegal LDC constant " + cst);
                            }
                        } else if (cst instanceof Handle) {
                            throw new UnsupportedOperationException("Not supported yet.");
                        } else {
                            throw new IllegalArgumentException("Illegal LDC constant " + cst);
                        }
                    case JSR:
                        throw new UnsupportedOperationException("Not supported yet.");
                    case GETSTATIC:
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        return b(Type.getType(fin.desc).getSize(), Exprs.nStaticField("L" + fin.owner + ";", fin.name,
                                fin.desc));
                    case NEW:
                        return b(1, Exprs.nNew("L" + ((TypeInsnNode) insn).desc + ";"));
                    default:
                        throw new Error("Internal error.");
                }
            }

            @Override
            public JvmValue copyOperation(AbstractInsnNode insn, JvmValue value) throws AnalyzerException {
                return b(value.getSize(), getLocal(value));
            }

            @Override
            public JvmValue unaryOperation(AbstractInsnNode insn, JvmValue value0) throws AnalyzerException {
                Local local = value0 == null ? null : getLocal(value0);
                switch (insn.getOpcode()) {
                    case INEG:
                        return b(1, Exprs.nNeg(local, "I"));
                    case IINC:
                        return b(1, Exprs.nAdd(local, Exprs.nInt(((IincInsnNode) insn).incr), "I"));
                    case L2I:
                        return b(1, Exprs.nCast(local, "J", "I"));
                    case F2I:
                        return b(1, Exprs.nCast(local, "F", "I"));
                    case D2I:
                        return b(1, Exprs.nCast(local, "D", "I"));
                    case I2B:
                        return b(1, Exprs.nCast(local, "I", "B"));
                    case I2C:
                        return b(1, Exprs.nCast(local, "I", "C"));
                    case I2S:
                        return b(1, Exprs.nCast(local, "I", "S"));
                    case FNEG:
                        return b(1, Exprs.nNeg(local, "F"));
                    case I2F:
                        return b(1, Exprs.nCast(local, "I", "F"));
                    case L2F:
                        return b(1, Exprs.nCast(local, "J", "F"));
                    case D2F:
                        return b(1, Exprs.nCast(local, "D", "F"));
                    case LNEG:
                        return b(2, Exprs.nNeg(local, "J"));
                    case I2L:
                        return b(2, Exprs.nCast(local, "I", "J"));
                    case F2L:
                        return b(2, Exprs.nCast(local, "F", "J"));
                    case D2L:
                        return b(2, Exprs.nCast(local, "D", "J"));
                    case DNEG:
                        return b(2, Exprs.nNeg(local, "D"));
                    case I2D:
                        return b(2, Exprs.nCast(local, "I", "D"));
                    case L2D:
                        return b(2, Exprs.nCast(local, "J", "D"));
                    case F2D:
                        return b(2, Exprs.nCast(local, "F", "D"));
                    case IFEQ:
                        emit(Stmts.nIf(Exprs.nEq(local, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFNE:
                        emit(Stmts.nIf(Exprs.nNe(local, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFLT:
                        emit(Stmts.nIf(Exprs.nLt(local, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFGE:
                        emit(Stmts.nIf(Exprs.nGe(local, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFGT:
                        emit(Stmts.nIf(Exprs.nGt(local, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFLE:
                        emit(Stmts.nIf(Exprs.nLe(local, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case TABLESWITCH: {
                        TableSwitchInsnNode ts = (TableSwitchInsnNode) insn;
                        LabelStmt targets[] = new LabelStmt[ts.labels.size()];
                        for (int i = 0; i < ts.labels.size(); i++) {
                            targets[i] = getLabel((LabelNode) ts.labels.get(i));
                        }
                        emit(Stmts.nTableSwitch(local, ts.min, targets, getLabel(ts.dflt)));
                        return null;
                    }
                    case LOOKUPSWITCH: {
                        LookupSwitchInsnNode ls = (LookupSwitchInsnNode) insn;
                        LabelStmt targets[] = new LabelStmt[ls.labels.size()];
                        int[] lookupValues = new int[ls.labels.size()];
                        for (int i = 0; i < ls.labels.size(); i++) {
                            targets[i] = getLabel((LabelNode) ls.labels.get(i));
                            lookupValues[i] = (Integer) ls.keys.get(i);
                        }
                        emit(Stmts.nLookupSwitch(local, lookupValues, targets, getLabel(ls.dflt)));
                        return null;
                    }
                    case IRETURN:
                    case LRETURN:
                    case FRETURN:
                    case DRETURN:
                    case ARETURN:
                        // skip, move to returnOperation
                        return null;
                    case PUTSTATIC: {
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        emit(Stmts.nAssign(Exprs.nStaticField("L" + fin.owner + ";", fin.name, fin.desc), local));
                        return null;
                    }
                    case GETFIELD: {
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        Type fieldType = Type.getType(fin.desc);
                        return b(fieldType.getSize(), Exprs.nField(local, "L" + fin.owner + ";", fin.name, fin.desc));
                    }
                    case NEWARRAY:
                        switch (((IntInsnNode) insn).operand) {
                            case T_BOOLEAN:
                                return b(1, Exprs.nNewArray("Z", local));
                            case T_CHAR:
                                return b(1, Exprs.nNewArray("C", local));
                            case T_BYTE:
                                return b(1, Exprs.nNewArray("B", local));
                            case T_SHORT:
                                return b(1, Exprs.nNewArray("S", local));
                            case T_INT:
                                return b(1, Exprs.nNewArray("I", local));
                            case T_FLOAT:
                                return b(1, Exprs.nNewArray("F", local));
                            case T_DOUBLE:
                                return b(1, Exprs.nNewArray("D", local));
                            case T_LONG:
                                return b(1, Exprs.nNewArray("D", local));
                            default:
                                throw new AnalyzerException(insn, "Invalid array type");
                        }
                    case ANEWARRAY:
                        String desc = "L" + ((TypeInsnNode) insn).desc + ";";
                        return b(1, Exprs.nNewArray(desc, local));
                    case ARRAYLENGTH:
                        return b(1, Exprs.nLength(local));
                    case ATHROW:
                        emit(Stmts.nThrow(local));
                        return null;
                    case CHECKCAST:
                        String orgDesc = ((TypeInsnNode) insn).desc;
                        desc = orgDesc.startsWith("[") ? orgDesc : ("L" + orgDesc + ";");
                        return b(1, Exprs.nCheckCast(local, desc));
                    case INSTANCEOF:
                        return b(1, Exprs.nInstanceOf(local, "L" + ((TypeInsnNode) insn).desc + ";"));
                    case MONITORENTER:
                        emit(Stmts.nLock(local));
                        return null;
                    case MONITOREXIT:
                        emit(Stmts.nUnLock(local));
                        return null;
                    case IFNULL:
                        emit(Stmts.nIf(Exprs.nEq(local, Exprs.nNull(), "L"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFNONNULL:
                        emit(Stmts.nIf(Exprs.nNe(local, Exprs.nNull(), "L"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case GOTO: // special case
                        emit(Stmts.nGoto(getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    default:
                        throw new Error("Internal error.");
                }
            }

            JvmValue b(int size, com.googlecode.dex2jar.ir.expr.Value value) {
                Local local = newLocal();
                emit(Stmts.nAssign(local, value));
                return new JvmValue(size, local);
            }

            @Override
            public JvmValue binaryOperation(AbstractInsnNode insn, JvmValue value10, JvmValue value20)
                    throws AnalyzerException {
                Local local1 = getLocal(value10);
                Local local2 = getLocal(value20);
                switch (insn.getOpcode()) {

                    case IALOAD:
                        return b(1, Exprs.nArray(local1, local2, "I"));
                    case BALOAD:
                        return b(1, Exprs.nArray(local1, local2, "B"));
                    case CALOAD:
                        return b(1, Exprs.nArray(local1, local2, "C"));
                    case SALOAD:
                        return b(1, Exprs.nArray(local1, local2, "S"));
                    case FALOAD:
                        return b(1, Exprs.nArray(local1, local2, "F"));
                    case AALOAD:
                        return b(1, Exprs.nArray(local1, local2, "L"));
                    case DALOAD:
                        return b(1, Exprs.nArray(local1, local2, "D"));
                    case LALOAD:
                        return b(1, Exprs.nArray(local1, local2, "J"));
                    case IADD:
                        return b(1, Exprs.nAdd(local1, local2, "I"));
                    case ISUB:
                        return b(1, Exprs.nSub(local1, local2, "I"));
                    case IMUL:
                        return b(1, Exprs.nMul(local1, local2, "I"));
                    case IDIV:
                        return b(1, Exprs.nDiv(local1, local2, "I"));
                    case IREM:
                        return b(1, Exprs.nRem(local1, local2, "I"));
                    case ISHL:
                        return b(1, Exprs.nShl(local1, local2, "I"));
                    case ISHR:
                        return b(1, Exprs.nShr(local1, local2, "I"));
                    case IUSHR:
                        return b(1, Exprs.nUshr(local1, local2, "I"));
                    case IAND:
                        return b(1, Exprs.nAnd(local1, local2, "I"));
                    case IOR:
                        return b(1, Exprs.nOr(local1, local2, "I"));
                    case IXOR:
                        return b(1, Exprs.nXor(local1, local2, "I"));
                    case FADD:
                        return b(1, Exprs.nAdd(local1, local2, "F"));
                    case FSUB:
                        return b(1, Exprs.nSub(local1, local2, "F"));
                    case FMUL:
                        return b(1, Exprs.nMul(local1, local2, "F"));
                    case FDIV:
                        return b(1, Exprs.nDiv(local1, local2, "F"));
                    case FREM:
                        return b(1, Exprs.nRem(local1, local2, "F"));
                    case LADD:
                        return b(2, Exprs.nAdd(local1, local2, "J"));
                    case LSUB:
                        return b(2, Exprs.nSub(local1, local2, "J"));
                    case LMUL:
                        return b(2, Exprs.nMul(local1, local2, "J"));
                    case LDIV:
                        return b(2, Exprs.nDiv(local1, local2, "J"));
                    case LREM:
                        return b(2, Exprs.nRem(local1, local2, "J"));
                    case LSHL:
                        return b(2, Exprs.nShl(local1, local2, "J"));
                    case LSHR:
                        return b(2, Exprs.nShr(local1, local2, "J"));
                    case LUSHR:
                        return b(2, Exprs.nUshr(local1, local2, "J"));
                    case LAND:
                        return b(2, Exprs.nAnd(local1, local2, "J"));
                    case LOR:
                        return b(2, Exprs.nOr(local1, local2, "J"));
                    case LXOR:
                        return b(2, Exprs.nXor(local1, local2, "J"));

                    case DADD:
                        return b(2, Exprs.nAdd(local1, local2, "D"));
                    case DSUB:
                        return b(2, Exprs.nSub(local1, local2, "D"));
                    case DMUL:
                        return b(2, Exprs.nMul(local1, local2, "D"));
                    case DDIV:
                        return b(2, Exprs.nDiv(local1, local2, "D"));
                    case DREM:
                        return b(2, Exprs.nRem(local1, local2, "D"));

                    case LCMP:
                        return b(2, Exprs.nLCmp(local1, local2));
                    case FCMPL:
                        return b(1, Exprs.nFCmpl(local1, local2));
                    case FCMPG:
                        return b(1, Exprs.nFCmpg(local1, local2));
                    case DCMPL:
                        return b(2, Exprs.nDCmpl(local1, local2));
                    case DCMPG:
                        return b(2, Exprs.nDCmpg(local1, local2));

                    case IF_ICMPEQ:
                        emit(Stmts.nIf(Exprs.nEq(local1, local2, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPNE:
                        emit(Stmts.nIf(Exprs.nNe(local1, local2, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPLT:
                        emit(Stmts.nIf(Exprs.nLt(local1, local2, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPGE:
                        emit(Stmts.nIf(Exprs.nGe(local1, local2, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPGT:
                        emit(Stmts.nIf(Exprs.nGt(local1, local2, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPLE:
                        emit(Stmts.nIf(Exprs.nLe(local1, local2, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ACMPEQ:
                        emit(Stmts.nIf(Exprs.nEq(local1, local2, "L"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ACMPNE:
                        emit(Stmts.nIf(Exprs.nNe(local1, local2, "L"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case PUTFIELD:
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        emit(Stmts.nAssign(Exprs.nField(local1, "L" + fin.owner + ";", fin.name, fin.desc), local2));
                        return null;
                    default:
                        throw new Error("Internal error.");
                }
            }

            @Override
            public JvmValue ternaryOperation(AbstractInsnNode insn, JvmValue value1, JvmValue value2, JvmValue value3)
                    throws AnalyzerException {
                Local local1 = getLocal(value1);
                Local local2 = getLocal(value2);
                Local local3 = getLocal(value3);
                switch (insn.getOpcode()) {
                    case IASTORE:
                        emit(Stmts.nAssign(Exprs.nArray(local1, local2, "I"),
                                local3));
                        break;
                    case LASTORE:
                        emit(Stmts.nAssign(Exprs.nArray(local1, local2, "J"),
                                local3));
                        break;
                    case FASTORE:
                        emit(Stmts.nAssign(Exprs.nArray(local1, local2, "F"),
                                local3));
                        break;
                    case DASTORE:
                        emit(Stmts.nAssign(Exprs.nArray(local1, local2, "D"),
                                local3));
                        break;
                    case AASTORE:
                        emit(Stmts.nAssign(Exprs.nArray(local1, local2, "L"),
                                local3));
                        break;
                    case BASTORE:
                        emit(Stmts.nAssign(Exprs.nArray(local1, local2, "B"),
                                local3));
                        break;
                    case CASTORE:
                        emit(Stmts.nAssign(Exprs.nArray(local1, local2, "C"),
                                local3));
                        break;
                    case SASTORE:
                        emit(Stmts.nAssign(Exprs.nArray(local1, local2, "S"),
                                local3));
                        break;
                }

                return null;
            }

            public String[] toDescArray(Type[] ts) {
                String[] ds = new String[ts.length];
                for (int i = 0; i < ts.length; i++) {
                    ds[i] = ts[i].getDescriptor();
                }
                return ds;
            }

            @Override
            public JvmValue naryOperation(AbstractInsnNode insn, List<? extends JvmValue> xvalues) throws AnalyzerException {

                com.googlecode.dex2jar.ir.expr.Value values[] = new com.googlecode.dex2jar.ir.expr.Value[xvalues.size()];
                for (int i = 0; i < xvalues.size(); i++) {
                    values[i] = getLocal(xvalues.get(i));
                }
                if (insn.getOpcode() == MULTIANEWARRAY) {
                    throw new UnsupportedOperationException("Not supported yet.");
                } else {
                    MethodInsnNode mi = (MethodInsnNode) insn;
                    com.googlecode.dex2jar.ir.expr.Value v = null;
                    String ret = Type.getReturnType(mi.desc).getDescriptor();
                    String owner = "L" + mi.owner + ";";
                    String ps[] = toDescArray(Type.getArgumentTypes(mi.desc));
                    switch (insn.getOpcode()) {
                        case INVOKEVIRTUAL:
                            v = Exprs.nInvokeVirtual(values, owner, mi.name, ps, ret);
                            break;
                        case INVOKESPECIAL:
                            v = Exprs.nInvokeSpecial(values, owner, mi.name, ps, ret);
                            break;
                        case INVOKESTATIC:
                            v = Exprs.nInvokeStatic(values, owner, mi.name, ps, ret);
                            break;
                        case INVOKEINTERFACE:
                            v = Exprs.nInvokeInterface(values, owner, mi.name, ps, ret);
                            break;
                        case INVOKEDYNAMIC:
                            throw new UnsupportedOperationException("Not supported yet.");
                    }
                    if ("V".equals(ret)) {
                        emit(Stmts.nVoidInvoke(v));
                        return null;
                    } else {
                        return b(Type.getReturnType(mi.desc).getSize(), v);
                    }
                }
            }

            @Override
            public JvmValue merge(JvmValue v, JvmValue w) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void returnOperation(AbstractInsnNode insn, JvmValue value, JvmValue expected) throws AnalyzerException {
                switch (insn.getOpcode()) {
                    case IRETURN:
                    case LRETURN:
                    case FRETURN:
                    case DRETURN:
                    case ARETURN:
                        emit(Stmts.nReturn(getLocal(value)));
                        break;
                    case RETURN:
                        emit(Stmts.nReturnVoid());
                        break;
                }

            }
        };
    }

    Local getLocal(JvmValue value) {
        Local local = value.local;
        if (local == null) {
            local = value.local = newLocal();
        }
        return local;
    }

    private void initParentCount(int[] parentCount) {
        parentCount[0] = 1;
        for (AbstractInsnNode p = insnList.getFirst(); p != null; p = p.getNext()) {
            if (p.getType() == AbstractInsnNode.JUMP_INSN) {
                JumpInsnNode jump = (JumpInsnNode) p;
                parentCount[insnList.indexOf(jump.label)]++;
            }
            int op = p.getOpcode();
            if (op == Opcodes.TABLESWITCH || op == Opcodes.LOOKUPSWITCH) {
                if (op == Opcodes.TABLESWITCH) {
                    TableSwitchInsnNode tsin = (TableSwitchInsnNode) p;
                    for (LabelNode label : tsin.labels) {
                        parentCount[insnList.indexOf(label)]++;
                    }
                    parentCount[insnList.indexOf(tsin.dflt)]++;
                } else {
                    LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) p;
                    for (LabelNode label : lsin.labels) {
                        parentCount[insnList.indexOf(label)]++;
                    }
                    parentCount[insnList.indexOf(lsin.dflt)]++;
                }
            }
            if ((op >= Opcodes.GOTO && op <= Opcodes.RETURN) || op == Opcodes.ATHROW) {
                // can't continue
            } else {
                AbstractInsnNode next = p.getNext();
                if(next!=null) {
                    parentCount[insnList.indexOf(p.getNext())]++;
                }
            }
        }
    }

    private void mergeEx(JvmFrame src, int dst) {
        JvmFrame distFrame = frames[dst];
        if (distFrame == null) {
            distFrame = frames[dst] = new JvmFrame(methodNode.maxLocals, methodNode.maxStack);
        }
        for (int i = 0; i < src.getLocals(); i++) {
            JvmValue p = src.getLocal(i);
            JvmValue q = distFrame.getLocal(i);
            if (p != null) {
                if (q == null) {
                    q = new JvmValue(p.getSize());
                    distFrame.setLocal(i, q);
                }
                relate(p, q);
            }
        }
    }

    private void merge(JvmFrame src, int dst) {
        JvmFrame distFrame = frames[dst];
        if (distFrame == null) {
            distFrame = frames[dst] = new JvmFrame(methodNode.maxLocals, methodNode.maxStack);
        }
        if (parentCount[dst] > 1) {
            for (int i = 0; i < src.getLocals(); i++) {
                JvmValue p = src.getLocal(i);
                JvmValue q = distFrame.getLocal(i);
                if (p != null) {
                    if (q == null) {
                        q = new JvmValue(p.getSize());
                        distFrame.setLocal(i, q);
                    }
                    relate(p, q);
                }
            }
            if (src.getStackSize() > 0) {
                if (distFrame.getStackSize() == 0) {
                    for (int i = 0; i < src.getStackSize(); i++) {
                        distFrame.push(new JvmValue(src.getStack(i).getSize()));
                    }
                } else if (distFrame.getStackSize() != src.getStackSize()) {
                    throw new RuntimeException("stack not balanced");
                }
                for (int i = 0; i < src.getStackSize(); i++) {
                    JvmValue p = src.getStack(i);
                    JvmValue q = distFrame.getStack(i);
                    relate(p, q);
                }
            }
        } else {
            distFrame.init(src);
        }
    }

    private void relate(JvmValue parent, JvmValue child) {
        if (child.parent == null) {
            child.parent = parent;
        } else if (child.parent == parent) {
            //
        } else {
            if (child.otherParent == null) {
                child.otherParent = new HashSet<>(5);
            }
            child.otherParent.add(parent);
        }
    }

    private JvmFrame initFirstFrame(MethodNode methodNode, IrMethod target) {
        JvmFrame first = new JvmFrame(methodNode.maxLocals, methodNode.maxStack);
        int x = 0;
        if (!target.isStatic) {// not static
            Local thiz = newLocal();
            emit(Stmts.nIdentity(thiz, Exprs.nThisRef(target.owner)));
            first.setLocal(x++, new JvmValue(1, thiz));
        }
        for (int i = 0; i < target.args.length; i++) {
            Local p = newLocal();
            emit(Stmts.nIdentity(p, Exprs.nParameterRef(target.args[i], i)));
            int sizeOfType = sizeOfType(target.args[i]);
            first.setLocal(x, new JvmValue(sizeOfType, p));
            x += sizeOfType;
        }
        return first;
    }

    private int sizeOfType(String arg) {
        switch (arg.charAt(0)) {
            case 'J':
            case 'D':
                return 2;
            default:
                return 1;
        }
    }

    private Local newLocal() {
        Local thiz = Exprs.nLocal(target.locals.size());
        target.locals.add(thiz);
        return thiz;
    }

    static class JvmFrame extends Frame<JvmValue> {

        public JvmFrame(int nLocals, int nStack) {
            super(nLocals, nStack);
        }

        @Override
        public void execute(AbstractInsnNode insn, Interpreter<JvmValue> interpreter) throws AnalyzerException {
            if (insn.getType() == AbstractInsnNode.FRAME || insn.getType() == AbstractInsnNode.LINE || insn.getType() == AbstractInsnNode.LABEL) {
                return;
            }
            if (insn.getOpcode() == Opcodes.RETURN) {
                interpreter.returnOperation(insn, null, null);
            } else if (insn.getOpcode() == Opcodes.GOTO) {
                interpreter.unaryOperation(insn, null);
            } else if (insn.getOpcode() == RET) {
                throw new RuntimeException("not support yet!");
            } else {
                super.execute(insn, interpreter);
            }
        }
    }

    public static class JvmValue implements Value {
        private final int size;
        public JvmValue parent;
        public Set<JvmValue> otherParent;
        Local local;

        public JvmValue(int size, Local local) {
            this.size = size;
            this.local = local;
        }

        public JvmValue(int size) {
            this.size = size;
        }

        @Override
        public int getSize() {
            return size;
        }
    }

}
