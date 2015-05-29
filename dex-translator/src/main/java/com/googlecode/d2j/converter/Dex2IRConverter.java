package com.googlecode.d2j.converter;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexCodeNode;
import com.googlecode.d2j.node.TryCatchNode;
import com.googlecode.d2j.node.analysis.DvmFrame;
import com.googlecode.d2j.node.analysis.DvmInterpreter;
import com.googlecode.d2j.node.insn.*;
import com.googlecode.d2j.reader.Op;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.TypeClass;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.*;

import java.util.*;

import static com.googlecode.dex2jar.ir.expr.Exprs.*;
import static com.googlecode.dex2jar.ir.stmt.Stmts.*;

public class Dex2IRConverter {
    Map<DexLabel, DexLabelStmtNode> labelMap = new HashMap<>();
    List<DexStmtNode> insnList;
    int[] parentCount;
    IrMethod target;
    DexCodeNode dexCodeNode;
    List<Stmt> preEmit = new ArrayList<>();
    List<Stmt> currentEmit;
    Map<DexLabel, LabelStmt> map = new HashMap<>();
    private Dex2IrFrame[] frames;
    private ArrayList<Stmt>[] emitStmts;
    boolean initAllToZero = true;

    static int sizeofType(String s) {
        char t = s.charAt(0);
        if (t == 'J' || t == 'D') {
            return 2;
        } else {
            return 1;
        }
    }

    static class Dex2IrFrame extends DvmFrame<DvmValue> {
        public Dex2IrFrame(int totalRegister) {
            super(totalRegister);
        }
    }

    static int methodArgCount(String[] args) {
        int i = 0;
        for (String s : args) {
            i += sizeofType(s);
        }
        return i;
    }

    public IrMethod convert(boolean isStatic, Method method, DexCodeNode dexCodeNode) {
        this.dexCodeNode = dexCodeNode;
        IrMethod irMethod = new IrMethod();
        irMethod.args = method.getParameterTypes();
        irMethod.ret = method.getReturnType();
        irMethod.owner = method.getOwner();
        irMethod.name = method.getName();
        irMethod.isStatic = isStatic;
        target = irMethod;


        insnList = dexCodeNode.stmts;
        for (int i = 0; i < insnList.size(); i++) {
            DexStmtNode stmtNode = insnList.get(i);
            stmtNode.__index = i;
            if (stmtNode instanceof DexLabelStmtNode) {
                DexLabelStmtNode dexLabelStmtNode = (DexLabelStmtNode) stmtNode;
                labelMap.put(dexLabelStmtNode.label, dexLabelStmtNode);
            }
        }

        fixExceptionHandlers();

        BitSet[] exBranch = new BitSet[insnList.size()];
        parentCount = new int[insnList.size()];
        initParentCount(parentCount);

        BitSet handlers = new BitSet(insnList.size());
        initExceptionHandlers(dexCodeNode, exBranch, handlers);

        DvmInterpreter<DvmValue> interpreter = buildInterpreter();
        frames = new Dex2IrFrame[insnList.size()];
        emitStmts = new ArrayList[insnList.size()];
        BitSet access = new BitSet(insnList.size());

        dfs(exBranch, handlers, access, interpreter);


        StmtList stmts = target.stmts;
        stmts.addAll(preEmit);
        for (int i = 0; i < insnList.size(); i++) {
            DexStmtNode p = insnList.get(i);
            if (access.get(i)) {
                List<Stmt> es = emitStmts[i];
                if (es != null) {
                    stmts.addAll(es);
                }
            } else {
                if (p instanceof DexLabelStmtNode) {
                    stmts.add(getLabel(((DexLabelStmtNode) p).label));
                }
            }
        }
        emitStmts = null;


        Queue<DvmValue> queue = new LinkedList<>();

        for (int i1 = 0; i1 < frames.length; i1++) {
            Dex2IrFrame frame = frames[i1];
            if (parentCount[i1] > 1 && frame != null && access.get(i1)) {
                for (int j = 0; j < frame.getTotalRegisters(); j++) {
                    DvmValue v = frame.getReg(j);
                    addToQueue(queue, v);
                }
            }

        }

        while (!queue.isEmpty()) {
            DvmValue v = queue.poll();
            getLocal(v);
            if (v.parent != null) {
                if (v.parent.local == null) {
                    queue.add(v.parent);
                }
            }
            if (v.otherParent != null) {
                for (DvmValue v2 : v.otherParent) {
                    if (v2.local == null) {
                        queue.add(v2);
                    }
                }
            }
        }

        Set<com.googlecode.dex2jar.ir.expr.Value> phiValues = new HashSet<>();
        List<LabelStmt> phiLabels = new ArrayList<>();
        for (int i = 0; i < frames.length; i++) {
            Dex2IrFrame frame = frames[i];
            if (parentCount[i] > 1 && frame != null && access.get(i)) {
                DexStmtNode p = insnList.get(i);
                LabelStmt labelStmt = getLabel(((DexLabelStmtNode) p).label);
                List<AssignStmt> phis = new ArrayList<>();
                for (int j = 0; j < frame.getTotalRegisters(); j++) {
                    DvmValue v = frame.getReg(j);
                    addPhi(v, phiValues, phis);
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

    /**
     * issue 63
     * <pre>
     * L1:
     *    STMTs
     * L2:
     *    RETURN
     * L1~L2 > L2 Exception
     * </pre>
     * <p/>
     * fix to
     * <p/>
     * <pre>
     * L1:
     *    STMTs
     * L2:
     *    RETURN
     * L3:
     *    goto L2
     * L1~L2 > L3 Exception
     * </pre>
     */
    private void fixExceptionHandlers() {
        if (dexCodeNode.tryStmts == null) {
            return;
        }
        Queue<Integer> q = new LinkedList<>();
        Set<Integer> handlers = new TreeSet<>();
        for (TryCatchNode tcb : dexCodeNode.tryStmts) {
            for (DexLabel h : tcb.handler) {
                int index = indexOf(h);
                q.add(index + 1); // add the next insn after label
                handlers.add(index);
            }
        }

        q.add(0);

        Map<Integer, DexLabel> needChange = new HashMap<>();

        BitSet access = new BitSet(insnList.size());
        while (!q.isEmpty()) {
            Integer key = q.poll();
            int index = key;
            if (access.get(index)) {
                continue;
            } else {
                access.set(index);
            }
            if (handlers.contains(key)) { // the cfg goes to a exception handler
                needChange.put(key, null);
            }
            DexStmtNode node = insnList.get(key);
            if (node.op == null) {
                q.add(index + 1);
            } else {
                Op op = node.op;
                if (op.canContinue()) {
                    q.add(index + 1);
                }
                if (op.canBranch()) {
                    JumpStmtNode jump = (JumpStmtNode) node;
                    q.add(indexOf(jump.label));
                }
                if (op.canSwitch()) {
                    for (DexLabel dexLabel : ((BaseSwitchStmtNode) node).labels) {
                        q.add(indexOf(dexLabel));
                    }
                }
            }
        }

        if (needChange.size() > 0) {
            for (TryCatchNode tcb : dexCodeNode.tryStmts) {
                DexLabel[] handler = tcb.handler;
                for (int i = 0; i < handler.length; i++) {
                    DexLabel h = handler[i];
                    int index = indexOf(h);
                    if (needChange.containsKey(index)) {
                        DexLabel n = needChange.get(index);
                        if (n == null) {
                            n = new DexLabel();
                            needChange.put(index, n);
                            DexLabelStmtNode dexStmtNode = new DexLabelStmtNode(n);
                            dexStmtNode.__index = insnList.size();
                            insnList.add(dexStmtNode);
                            labelMap.put(n, dexStmtNode);
                            JumpStmtNode jumpStmtNode = new JumpStmtNode(Op.GOTO, 0, 0, h);
                            jumpStmtNode.__index = insnList.size();
                            insnList.add(jumpStmtNode);
                        }
                        handler[i] = n;
                    }
                }
            }
        }
    }

    private void initExceptionHandlers(DexCodeNode dexCodeNode, BitSet[] exBranch, BitSet handlers) {
        if (dexCodeNode.tryStmts != null) {
            for (TryCatchNode tcb : dexCodeNode.tryStmts) {
                target.traps.add(new Trap(getLabel(tcb.start), getLabel(tcb.end), getLabels(tcb.handler),
                        tcb.type));
                for (DexLabel h : tcb.handler) {
                    handlers.set(indexOf(h));
                }
                int endIndex = indexOf(tcb.end);
                for (int p = indexOf(tcb.start) + 1; p < endIndex; p++) {
                    DexStmtNode stmt = insnList.get(p);
                    if (stmt.op != null && stmt.op.canThrow()) {
                        BitSet x = exBranch[p];
                        if (x == null) {
                            x = exBranch[p] = new BitSet(insnList.size());
                        }
                        for (DexLabel h : tcb.handler) {
                            int hIndex = indexOf(h);
                            x.set(hIndex);
                            parentCount[hIndex]++;
                        }
                    }
                }
            }
        }
    }

    private void addPhi(DvmValue v, Set<com.googlecode.dex2jar.ir.expr.Value> phiValues, List<AssignStmt> phis) {
        if (v != null) {
            if (v.local != null) {
                if (v.parent != null) {
                    phiValues.add(getLocal(v.parent));
                }
                if (v.otherParent != null) {
                    for (DvmValue v2 : v.otherParent) {
                        phiValues.add(getLocal(v2));
                    }
                }
                if (phiValues.size() > 0) {
                    phis.add(Stmts.nAssign(v.local, Exprs
                            .nPhi(phiValues.toArray(new com.googlecode.dex2jar.ir.expr.Value[phiValues.size()]))));
                    phiValues.clear();
                }
            }
        }
    }

    Local getLocal(DvmValue value) {
        Local local = value.local;
        if (local == null) {
            local = value.local = newLocal();
        }
        return local;
    }

    private void addToQueue(Queue<DvmValue> queue, DvmValue v) {
        if (v != null) {
            if (v.local != null) {
                if (v.parent != null) {
                    if (v.parent.local == null) {
                        queue.add(v.parent);
                    }
                }
                if (v.otherParent != null) {
                    for (DvmValue v2 : v.otherParent) {
                        if (v2.local == null) {
                            queue.add(v2);
                        }
                    }
                }
            }
        }
    }

    private void setCurrentEmit(int index) {
        currentEmit = emitStmts[index];
        if (currentEmit == null) {
            currentEmit = emitStmts[index] = new ArrayList<>(1);
        }
    }

    private void dfs(BitSet[] exBranch, BitSet handlers, BitSet access, DvmInterpreter<DvmValue> interpreter) {
        currentEmit = preEmit;

        Dex2IrFrame first = initFirstFrame(dexCodeNode, target);
        if (parentCount[0] > 1) {
            merge(first, 0);
        } else {
            frames[0] = first;
        }
        Stack<DexStmtNode> stack = new Stack<>();
        stack.push(insnList.get(0));
        Dex2IrFrame tmp = new Dex2IrFrame(dexCodeNode.totalRegister);


        while (!stack.isEmpty()) {
            DexStmtNode p = stack.pop();
            int index = p.__index;
            if (!access.get(index)) {
                access.set(index);
            } else {
                continue;
            }
            Dex2IrFrame frame = frames[index];
            setCurrentEmit(index);

            if (p instanceof DexLabelStmtNode) {
                emit(getLabel(((DexLabelStmtNode) p).label));
                if (handlers.get(index)) {
                    Local ex = newLocal();
                    emit(Stmts.nIdentity(ex, Exprs.nExceptionRef("Ljava/lang/Throwable;")));
                    frame.setTmp(new DvmValue(ex));
                }
            }
            BitSet ex = exBranch[index];
            if (ex != null) {
                for (int i = ex.nextSetBit(0); i >= 0; i = ex.nextSetBit(i + 1)) {
                    merge(frame, i);
                    stack.push(insnList.get(i));
                }
            }

            tmp.init(frame);
            try {
                if (p.op != null) {
                    switch (p.op) {
                        case RETURN_VOID:
                            emit(nReturnVoid());
                            break;
                        case GOTO:
                        case GOTO_16:
                        case GOTO_32:
                            emit(nGoto(getLabel(((JumpStmtNode) p).label)));
                            break;
                        case NOP:
                            emit(nNop());
                            break;
                        case BAD_OP:
                            emit(nThrow(nInvokeNew(new Value[]{nString("bad dex opcode")}, new String[]{
                                            "Ljava/lang/String;"},
                                    "Ljava/lang/VerifyError;")));
                            break;
                        default:
                            tmp.execute(p, interpreter);
                            break;
                    }
                }
            } catch (Exception exception) {
                throw new RuntimeException("Fail on Op " + p.op + " index " + index, exception);
            }


            if (p.op != null) {
                Op op = p.op;
                if (op.canBranch()) {
                    JumpStmtNode jump = (JumpStmtNode) p;
                    int targetIndex = indexOf(jump.label);
                    stack.push(insnList.get(targetIndex));
                    merge(tmp, targetIndex);
                }
                if (op.canSwitch()) {
                    BaseSwitchStmtNode switchStmtNode = (BaseSwitchStmtNode) p;
                    for (DexLabel label : switchStmtNode.labels) {
                        int targetIndex = indexOf(label);
                        stack.push(insnList.get(targetIndex));
                        merge(tmp, targetIndex);
                    }
                }
                if (op.canContinue()) {
                    stack.push(insnList.get(index + 1));
                    merge(tmp, index + 1);
                }
            } else {

                stack.push(insnList.get(index + 1));
                merge(tmp, index + 1);

            }
            // cleanup frame it is useless
            if (parentCount[index] <= 1) {
                frames[index] = null;
            }

        }

    }

    private void relate(DvmValue parent, DvmValue child) {
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

    void merge(Dex2IrFrame src, int dst) {
        Dex2IrFrame distFrame = frames[dst];
        if (distFrame == null) {
            distFrame = frames[dst] = new Dex2IrFrame(dexCodeNode.totalRegister);
        }
        if (parentCount[dst] > 1) {
            for (int i = 0; i < src.getTotalRegisters(); i++) {
                DvmValue p = src.getReg(i);
                DvmValue q = distFrame.getReg(i);
                if (p != null) {
                    if (q == null) {
                        q = new DvmValue();
                        distFrame.setReg(i, q);
                    }
                    relate(p, q);
                }
            }
        } else {
            distFrame.init(src);
        }
    }

    private Local newLocal() {
        Local thiz = Exprs.nLocal(target.locals.size());
        target.locals.add(thiz);
        return thiz;
    }

    void emit(Stmt stmt) {
        currentEmit.add(stmt);
    }

    private Dex2IrFrame initFirstFrame(DexCodeNode methodNode, IrMethod target) {
        Dex2IrFrame first = new Dex2IrFrame(methodNode.totalRegister);
        int x = methodNode.totalRegister - methodArgCount(target.args);
        if (!target.isStatic) {// not static
            Local thiz = newLocal();
            emit(Stmts.nIdentity(thiz, Exprs.nThisRef(target.owner)));
            first.setReg(x - 1, new DvmValue(thiz));
        }
        for (int i = 0; i < target.args.length; i++) {
            Local p = newLocal();
            emit(Stmts.nIdentity(p, Exprs.nParameterRef(target.args[i], i)));
            first.setReg(x, new DvmValue(p));
            x += sizeofType(target.args[i]);
        }

        if (initAllToZero) {
            for (int i = 0; i < first.getTotalRegisters(); i++) {
                if (first.getReg(i) == null) {
                    Local p = newLocal();
                    emit(nAssign(p, nInt(0)));
                    first.setReg(i, new DvmValue(p));
                }
            }
        }

        return first;
    }

    private DvmInterpreter<DvmValue> buildInterpreter() {
        return new DvmInterpreter<DvmValue>() {
            DvmValue b(com.googlecode.dex2jar.ir.expr.Value value) {
                Local local = newLocal();
                emit(Stmts.nAssign(local, value));
                return new DvmValue(local);
            }

            @Override
            public DvmValue newOperation(DexStmtNode insn) {
                switch (insn.op) {
                    case CONST:
                    case CONST_16:
                    case CONST_4:
                    case CONST_HIGH16:
                        return b(nInt((Integer) ((ConstStmtNode) insn).value));
                    case CONST_WIDE:
                    case CONST_WIDE_16:
                    case CONST_WIDE_32:
                    case CONST_WIDE_HIGH16:
                        return b(nLong((Long) ((ConstStmtNode) insn).value));
                    case CONST_CLASS:
                        return b(nType(((DexType) ((ConstStmtNode) insn).value).desc));
                    case CONST_STRING:
                    case CONST_STRING_JUMBO:
                        return b(nString((String) ((ConstStmtNode) insn).value));
                    case SGET:
                    case SGET_BOOLEAN:
                    case SGET_BYTE:
                    case SGET_CHAR:
                    case SGET_OBJECT:
                    case SGET_SHORT:
                    case SGET_WIDE:
                        Field field = ((FieldStmtNode) insn).field;
                        return b(nStaticField(field.getOwner(), field.getName(), field.getType()));
                    case NEW_INSTANCE:
                        return b(nNew(((TypeStmtNode) insn).type));
                    default:
                }
                return null;
            }

            @Override
            public DvmValue copyOperation(DexStmtNode insn, DvmValue value) {
                if (value == null) {
                    emitNotFindOperand(insn);
                    return b(nInt(0));
                }
                return b(getLocal(value));
            }

            @Override
            public DvmValue unaryOperation(DexStmtNode insn, DvmValue value) {
                if (value == null) {
                    emitNotFindOperand(insn);
                    return b(nInt(0));
                }
                Local local = getLocal(value);
                switch (insn.op) {
                    case NOT_INT:
                        return b(nNot(local, "I"));
                    case NOT_LONG:
                        return b(nNot(local, "J"));

                    case NEG_DOUBLE:
                        return b(nNeg(local, "D"));

                    case NEG_FLOAT:
                        return b(nNeg(local, "F"));

                    case NEG_INT:
                        return b(nNeg(local, "I"));

                    case NEG_LONG:
                        return b(nNeg(local, "J"));
                    case INT_TO_BYTE:
                        return b(nCast(local, "I", "B"));

                    case INT_TO_CHAR:
                        return b(nCast(local, "I", "C"));

                    case INT_TO_DOUBLE:
                        return b(nCast(local, "I", "D"));

                    case INT_TO_FLOAT:
                        return b(nCast(local, "I", "F"));

                    case INT_TO_LONG:
                        return b(nCast(local, "I", "J"));

                    case INT_TO_SHORT:
                        return b(nCast(local, "I", "S"));

                    case FLOAT_TO_DOUBLE:
                        return b(nCast(local, "F", "D"));

                    case FLOAT_TO_INT:
                        return b(nCast(local, "F", "I"));

                    case FLOAT_TO_LONG:
                        return b(nCast(local, "F", "J"));

                    case DOUBLE_TO_FLOAT:
                        return b(nCast(local, "D", "F"));

                    case DOUBLE_TO_INT:
                        return b(nCast(local, "D", "I"));

                    case DOUBLE_TO_LONG:
                        return b(nCast(local, "D", "J"));

                    case LONG_TO_DOUBLE:
                        return b(nCast(local, "J", "D"));

                    case LONG_TO_FLOAT:
                        return b(nCast(local, "J", "F"));

                    case LONG_TO_INT:
                        return b(nCast(local, "J", "I"));

                    case ARRAY_LENGTH:
                        return b(nLength(local));

                    case IF_EQZ:
                        emit(nIf(Exprs
                                .nEq(local, nInt(0), TypeClass.ZIL.name), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_GEZ:
                        emit(nIf(Exprs.nGe(local, nInt(0), "I"), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_GTZ:
                        emit(nIf(Exprs.nGt(local, nInt(0), "I"), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_LEZ:
                        emit(nIf(Exprs.nLe(local, nInt(0), "I"), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_LTZ:
                        emit(nIf(Exprs.nLt(local, nInt(0), "I"), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_NEZ:
                        emit(nIf(Exprs
                                .nNe(local, nInt(0), TypeClass.ZIL.name), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case PACKED_SWITCH:
                    case SPARSE_SWITCH:
                        DexLabel[] labels = ((BaseSwitchStmtNode) insn).labels;
                        LabelStmt[] lss = new LabelStmt[labels.length];
                        for (int i = 0; i < labels.length; i++) {
                            lss[i] = getLabel(labels[i]);
                        }
                        LabelStmt d = new LabelStmt();
                        if (insn.op == Op.PACKED_SWITCH) {
                            emit(nTableSwitch(local, ((PackedSwitchStmtNode) insn).first_case, lss, d));
                        } else {
                            emit(nLookupSwitch(local, ((SparseSwitchStmtNode) insn).cases, lss, d));
                        }
                        emit(d);
                        return null;

                    case SPUT:
                    case SPUT_BOOLEAN:
                    case SPUT_BYTE:
                    case SPUT_CHAR:
                    case SPUT_OBJECT:
                    case SPUT_SHORT:
                    case SPUT_WIDE: {
                        Field field = ((FieldStmtNode) insn).field;
                        emit(nAssign(nStaticField(field.getOwner(), field.getName(), field.getType()), local));
                        return null;
                    }
                    case IGET:
                    case IGET_BOOLEAN:
                    case IGET_BYTE:
                    case IGET_CHAR:
                    case IGET_OBJECT:
                    case IGET_SHORT:
                    case IGET_WIDE: {
                        Field field = ((FieldStmtNode) insn).field;
                        return b(nField(local, field.getOwner(), field.getName(), field.getType()));
                    }
                    case INSTANCE_OF:
                        return b(nInstanceOf(local, ((TypeStmtNode) insn).type));

                    case NEW_ARRAY:
                        return b(nNewArray(((TypeStmtNode) insn).type.substring(1), local));

                    case CHECK_CAST:
                        return b(nCheckCast(local, ((TypeStmtNode) insn).type));

                    case MONITOR_ENTER:
                        emit(nLock(local));
                        return null;
                    case MONITOR_EXIT:
                        emit(nUnLock(local));
                        return null;
                    case THROW:
                        emit(nThrow(local));
                        return null;
                    case ADD_INT_LIT16:
                    case ADD_INT_LIT8:
                        return b(nAdd(local, nInt(((Stmt2R1NNode) insn).content), "I"));

                    case RSUB_INT_LIT8:
                    case RSUB_INT://
                        return b(nSub(nInt(((Stmt2R1NNode) insn).content), local, "I"));

                    case MUL_INT_LIT8:
                    case MUL_INT_LIT16:
                        return b(nMul(local, nInt(((Stmt2R1NNode) insn).content), "I"));

                    case DIV_INT_LIT16:
                    case DIV_INT_LIT8:
                        return b(nDiv(local, nInt(((Stmt2R1NNode) insn).content), "I"));

                    case REM_INT_LIT16:
                    case REM_INT_LIT8:
                        return b(nRem(local, nInt(((Stmt2R1NNode) insn).content), "I"));

                    case AND_INT_LIT16:
                    case AND_INT_LIT8:
                        return b(nAnd(local, nInt(((Stmt2R1NNode) insn).content), ((Stmt2R1NNode) insn).content < 0 || ((Stmt2R1NNode) insn).content > 1 ? "I" : TypeClass.ZI.name));

                    case OR_INT_LIT16:
                    case OR_INT_LIT8:
                        return b(nOr(local, nInt(((Stmt2R1NNode) insn).content), ((Stmt2R1NNode) insn).content < 0 || ((Stmt2R1NNode) insn).content > 1 ? "I" : TypeClass.ZI.name));

                    case XOR_INT_LIT16:
                    case XOR_INT_LIT8:
                        return b(nXor(local, nInt(((Stmt2R1NNode) insn).content), ((Stmt2R1NNode) insn).content < 0 || ((Stmt2R1NNode) insn).content > 1 ? "I" : TypeClass.ZI.name));

                    case SHL_INT_LIT8:
                        return b(nShl(local, nInt(((Stmt2R1NNode) insn).content), "I"));

                    case SHR_INT_LIT8:
                        return b(nShr(local, nInt(((Stmt2R1NNode) insn).content), "I"));

                    case USHR_INT_LIT8:
                        return b(nUshr(local, nInt(((Stmt2R1NNode) insn).content), "I"));
                    case FILL_ARRAY_DATA:
                        emit(nFillArrayData(local, nArrayValue(((FillArrayDataStmtNode) insn).array)));
                        return null;
                }
                throw new RuntimeException();
            }

            @Override
            public DvmValue binaryOperation(DexStmtNode insn, DvmValue value1, DvmValue value2) {
                if (value1 == null || value2 == null) {
                    emitNotFindOperand(insn);
                    return b(nInt(0));
                }
                Local local1 = getLocal(value1);
                Local local2 = getLocal(value2);
                switch (insn.op) {
                    case AGET:
                        return b(nArray(local1, local2, TypeClass.IF.name));

                    case AGET_BOOLEAN:
                        return b(nArray(local1, local2, "Z"));

                    case AGET_BYTE:
                        return b(nArray(local1, local2, "B"));

                    case AGET_CHAR:
                        return b(nArray(local1, local2, "C"));

                    case AGET_OBJECT:
                        return b(nArray(local1, local2, "L"));

                    case AGET_SHORT:
                        return b(nArray(local1, local2, "S"));

                    case AGET_WIDE:
                        return b(nArray(local1, local2, TypeClass.JD.name));

                    case CMP_LONG:
                        return b(nLCmp(local1, local2));

                    case CMPG_DOUBLE:
                        return b(nDCmpg(local1, local2));

                    case CMPG_FLOAT:
                        return b(nFCmpg(local1, local2));

                    case CMPL_DOUBLE:
                        return b(nDCmpl(local1, local2));

                    case CMPL_FLOAT:
                        return b(nFCmpl(local1, local2));

                    case ADD_DOUBLE:
                        return b(nAdd(local1, local2, "D"));

                    case ADD_FLOAT:
                        return b(nAdd(local1, local2, "F"));

                    case ADD_INT:
                        return b(nAdd(local1, local2, "I"));

                    case ADD_LONG:
                        return b(nAdd(local1, local2, "J"));

                    case SUB_DOUBLE:
                        return b(nSub(local1, local2, "D"));

                    case SUB_FLOAT:
                        return b(nSub(local1, local2, "F"));

                    case SUB_INT:
                        return b(nSub(local1, local2, "I"));

                    case SUB_LONG:
                        return b(nSub(local1, local2, "J"));

                    case MUL_DOUBLE:
                        return b(nMul(local1, local2, "D"));

                    case MUL_FLOAT:
                        return b(nMul(local1, local2, "F"));

                    case MUL_INT:
                        return b(nMul(local1, local2, "I"));

                    case MUL_LONG:
                        return b(nMul(local1, local2, "J"));

                    case DIV_DOUBLE:
                        return b(nDiv(local1, local2, "D"));

                    case DIV_FLOAT:
                        return b(nDiv(local1, local2, "F"));

                    case DIV_INT:
                        return b(nDiv(local1, local2, "I"));

                    case DIV_LONG:
                        return b(nDiv(local1, local2, "J"));

                    case REM_DOUBLE:
                        return b(nRem(local1, local2, "D"));

                    case REM_FLOAT:
                        return b(nRem(local1, local2, "F"));

                    case REM_INT:
                        return b(nRem(local1, local2, "I"));

                    case REM_LONG:
                        return b(nRem(local1, local2, "J"));

                    case AND_INT:
                        return b(nAnd(local1, local2, TypeClass.ZI.name));

                    case AND_LONG:
                        return b(nAnd(local1, local2, "J"));

                    case OR_INT:
                        return b(nOr(local1, local2, TypeClass.ZI.name));

                    case OR_LONG:
                        return b(nOr(local1, local2, "J"));

                    case XOR_INT:
                        return b(nXor(local1, local2, TypeClass.ZI.name));

                    case XOR_LONG:
                        return b(nXor(local1, local2, "J"));

                    case SHL_INT:
                        return b(nShl(local1, local2, "I"));

                    case SHL_LONG:
                        return b(nShl(local1, local2, "J"));

                    case SHR_INT:
                        return b(nShr(local1, local2, "I"));

                    case SHR_LONG:
                        return b(nShr(local1, local2, "J"));

                    case USHR_INT:
                        return b(nUshr(local1, local2, "I"));

                    case USHR_LONG:
                        return b(nUshr(local1, local2, "J"));

                    case IF_EQ:
                        emit(nIf(Exprs
                                .nEq(local1, local2, TypeClass.ZIL.name), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_GE:
                        emit(nIf(Exprs.nGe(local1, local2, "I"), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_GT:
                        emit(nIf(Exprs.nGt(local1, local2, "I"), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_LE:
                        emit(nIf(Exprs.nLe(local1, local2, "I"), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_LT:
                        emit(nIf(Exprs.nLt(local1, local2, "I"), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IF_NE:
                        emit(nIf(Exprs
                                .nNe(local1, local2, TypeClass.ZIL.name), getLabel(((JumpStmtNode) insn).label)));
                        return null;

                    case IPUT:
                    case IPUT_BOOLEAN:
                    case IPUT_BYTE:
                    case IPUT_CHAR:
                    case IPUT_OBJECT:
                    case IPUT_SHORT:
                    case IPUT_WIDE:
                        Field field = ((FieldStmtNode) insn).field;
                        emit(nAssign(nField(local1, field.getOwner(), field.getName(), field.getType()), local2));
                        return null;

                    case ADD_DOUBLE_2ADDR:
                        return b(nAdd(local1, local2, "D"));

                    case ADD_FLOAT_2ADDR:
                        return b(nAdd(local1, local2, "F"));

                    case ADD_INT_2ADDR:
                        return b(nAdd(local1, local2, "I"));

                    case ADD_LONG_2ADDR:
                        return b(nAdd(local1, local2, "J"));

                    case SUB_DOUBLE_2ADDR:
                        return b(nSub(local1, local2, "D"));

                    case SUB_FLOAT_2ADDR:
                        return b(nSub(local1, local2, "F"));

                    case SUB_INT_2ADDR:
                        return b(nSub(local1, local2, "I"));

                    case SUB_LONG_2ADDR:
                        return b(nSub(local1, local2, "J"));

                    case MUL_DOUBLE_2ADDR:
                        return b(nMul(local1, local2, "D"));

                    case MUL_FLOAT_2ADDR:
                        return b(nMul(local1, local2, "F"));

                    case MUL_INT_2ADDR:
                        return b(nMul(local1, local2, "I"));

                    case MUL_LONG_2ADDR:
                        return b(nMul(local1, local2, "J"));

                    case DIV_DOUBLE_2ADDR:
                        return b(nDiv(local1, local2, "D"));

                    case DIV_FLOAT_2ADDR:
                        return b(nDiv(local1, local2, "F"));

                    case DIV_INT_2ADDR:
                        return b(nDiv(local1, local2, "I"));

                    case DIV_LONG_2ADDR:
                        return b(nDiv(local1, local2, "J"));

                    case REM_DOUBLE_2ADDR:
                        return b(nRem(local1, local2, "D"));

                    case REM_FLOAT_2ADDR:
                        return b(nRem(local1, local2, "F"));

                    case REM_INT_2ADDR:
                        return b(nRem(local1, local2, "I"));

                    case REM_LONG_2ADDR:
                        return b(nRem(local1, local2, "J"));

                    case AND_INT_2ADDR:
                        return b(nAnd(local1, local2, TypeClass.ZI.name));

                    case AND_LONG_2ADDR:
                        return b(nAnd(local1, local2, "J"));

                    case OR_INT_2ADDR:
                        return b(nOr(local1, local2, TypeClass.ZI.name));

                    case OR_LONG_2ADDR:
                        return b(nOr(local1, local2, "J"));

                    case XOR_INT_2ADDR:
                        return b(nXor(local1, local2, TypeClass.ZI.name));

                    case XOR_LONG_2ADDR:
                        return b(nXor(local1, local2, "J"));

                    case SHL_INT_2ADDR:
                        return b(nShl(local1, local2, "I"));

                    case SHL_LONG_2ADDR:
                        return b(nShl(local1, local2, "J"));

                    case SHR_INT_2ADDR:
                        return b(nShr(local1, local2, "I"));

                    case SHR_LONG_2ADDR:
                        return b(nShr(local1, local2, "J"));

                    case USHR_INT_2ADDR:
                        return b(nUshr(local1, local2, "I"));

                    case USHR_LONG_2ADDR:
                        return b(nUshr(local1, local2, "J"));

                }
                throw new RuntimeException();
            }

            @Override
            public DvmValue ternaryOperation(DexStmtNode insn, DvmValue value1, DvmValue value2, DvmValue value3) {
                if (value1 == null || value2 == null || value3 == null) {
                    emitNotFindOperand(insn);
                    return b(nInt(0));
                }
                Local localArray = getLocal(value1);
                Local localIndex = getLocal(value2);
                Local localValue = getLocal(value3);
                switch (insn.op) {
                    case APUT:
                        emit(nAssign(nArray(localArray, localIndex, TypeClass.IF.name), localValue));
                        break;
                    case APUT_BOOLEAN:
                        emit(nAssign(nArray(localArray, localIndex, "Z"), localValue));
                        break;
                    case APUT_BYTE:
                        emit(nAssign(nArray(localArray, localIndex, "B"), localValue));
                        break;
                    case APUT_CHAR:
                        emit(nAssign(nArray(localArray, localIndex, "C"), localValue));
                        break;
                    case APUT_OBJECT:
                        emit(nAssign(nArray(localArray, localIndex, "L"), localValue));
                        break;
                    case APUT_SHORT:
                        emit(nAssign(nArray(localArray, localIndex, "S"), localValue));
                        break;
                    case APUT_WIDE:
                        emit(nAssign(nArray(localArray, localIndex, TypeClass.JD.name), localValue));
                        break;
                }
                return null;
            }

            @Override
            public DvmValue naryOperation(DexStmtNode insn, List<? extends DvmValue> values) {
                for (DvmValue v : values) {
                    if (v == null) {
                        emitNotFindOperand(insn);
                        return b(nInt(0));
                    }
                }


                switch (insn.op) {
                    case FILLED_NEW_ARRAY:
                    case FILLED_NEW_ARRAY_RANGE:
                        DvmValue value = new DvmValue();
                        FilledNewArrayStmtNode filledNewArrayStmtNode = (FilledNewArrayStmtNode) insn;
                        String type = filledNewArrayStmtNode.type;

                        String elem = type.substring(1);
                        emit(nAssign(getLocal(value), nNewArray(elem, nInt(values.size()))));
                        for (int i = 0; i < values.size(); i++) {
                            emit(nAssign(nArray(getLocal(value), nInt(i), elem), getLocal(values.get(i))));
                        }

                        return value;
                    default:
                        Op op = insn.op;
                        Method method = ((MethodStmtNode) insn).method;
                        Value[] vs = new Value[values.size()];
                        for (int i = 0; i < vs.length; i++) {
                            vs[i] = getLocal(values.get(i));
                        }


                        Value invoke = null;
                        switch (op) {
                            case INVOKE_VIRTUAL_RANGE:
                            case INVOKE_VIRTUAL:
                                invoke = nInvokeVirtual(vs, method.getOwner(), method.getName(), method
                                                .getParameterTypes(),
                                        method.getReturnType());
                                break;
                            case INVOKE_SUPER_RANGE:
                            case INVOKE_DIRECT_RANGE:
                            case INVOKE_SUPER:
                            case INVOKE_DIRECT:
                                invoke = nInvokeSpecial(vs, method.getOwner(), method.getName(), method
                                                .getParameterTypes(),
                                        method.getReturnType());
                                break;
                            case INVOKE_STATIC_RANGE:
                            case INVOKE_STATIC:
                                invoke = nInvokeStatic(vs, method.getOwner(), method.getName(), method
                                                .getParameterTypes(),
                                        method.getReturnType());
                                break;
                            case INVOKE_INTERFACE_RANGE:
                            case INVOKE_INTERFACE:
                                invoke = nInvokeInterface(vs, method.getOwner(), method.getName(), method
                                                .getParameterTypes(),
                                        method.getReturnType());
                                break;
                            default:
                                throw new RuntimeException();
                        }
                        if ("V".equals(method.getReturnType())) {
                            emit(nVoidInvoke(invoke));
                            return null;
                        } else {
                            return b(invoke);
                        }

                }


            }

            void emitNotFindOperand(DexStmtNode insn) {
                String msg;
                switch (insn.op) {
                    case MOVE_RESULT:
                    case MOVE_RESULT_OBJECT:
                    case MOVE_RESULT_WIDE:
                        msg = "can't get operand(s) for " + insn.op + ", wrong position ?";
                        break;
                    default:
                        msg = "can't get operand(s) for " + insn.op + ", out-of-range or not initialized ?";
                        break;
                }

                System.err.println("WARN: " + msg);
                emit(nThrow(nInvokeNew(new Value[]{nString("d2j: " + msg)},
                        new String[]{"Ljava/lang/String;"}, "Ljava/lang/VerifyError;")));
            }

            @Override
            public void returnOperation(DexStmtNode insn, DvmValue value) {
                if (value == null) {
                    emitNotFindOperand(insn);
                    return;
                }

                emit(nReturn(getLocal(value)));
            }
        };
    }

    private LabelStmt[] getLabels(DexLabel[] handler) {
        LabelStmt[] ts = new LabelStmt[handler.length];
        for (int i = 0; i < handler.length; i++) {
            ts[i] = getLabel(handler[i]);
        }
        return ts;
    }

    LabelStmt getLabel(DexLabel label) {
        LabelStmt ls = map.get(label);
        if (ls == null) {
            ls = Stmts.nLabel();
            map.put(label, ls);
        }
        return ls;
    }

    private void initParentCount(int[] parentCount) {
        parentCount[0] = 1; // first stmt always have one parent
        for (DexStmtNode p : insnList) {
            Op op = p.op;
            if (op == null) {
                if (p.__index < parentCount.length - 1) { // not the last label
                    parentCount[p.__index + 1]++;
                }
            } else {
                if (op.canBranch()) {
                    parentCount[indexOf(((JumpStmtNode) p).label)]++;
                }
                if (op.canSwitch()) {
                    BaseSwitchStmtNode switchStmtNode = (BaseSwitchStmtNode) p;
                    for (DexLabel label : switchStmtNode.labels) {
                        parentCount[indexOf(label)]++;
                    }
                }
                if (op.canContinue()) {
                    parentCount[p.__index + 1]++;
                }
            }
        }
    }

    int indexOf(DexLabel label) {
        DexLabelStmtNode dexLabelStmtNode = labelMap.get(label);
        return dexLabelStmtNode.__index;
    }

    static class DvmValue {
        public DvmValue parent;
        public Set<DvmValue> otherParent;
        Local local;

        public DvmValue(Local thiz) {
            this.local = thiz;
        }

        public DvmValue() {

        }
    }


}
