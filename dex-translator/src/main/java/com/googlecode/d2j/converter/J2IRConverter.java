package com.googlecode.d2j.converter;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert Instruction in Asm to IRMethod,
 * please set <code>ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES</code> where build
 * the ClassNode
 *
 * @author bob
 */
public class J2IRConverter implements Opcodes {

    public static final Value PLACEHOLDER = Exprs.nByte((byte) 0);
    Map<Label, LabelStmt> map = new HashMap<Label, LabelStmt>();

    public static String[] toDescArray(Type[] ts) {
        String[] ds = new String[ts.length];
        for (int i = 0; i < ts.length; i++) {
            ds[i] = ts[i].getDescriptor();
        }
        return ds;
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

    /**
     * convert the asm code into ir
     *
     * @param owner  class internal name
     * @param source src method node
     * @return the converted IR
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public IrMethod convert(String owner, final MethodNode source) {
        IrMethod target = populate(owner, source);
        if (source.instructions.size() == 0) {
            return target;
        }
        for (TryCatchBlockNode tc : source.tryCatchBlocks) {
        target.traps.add(new Trap(getLabel(tc.start), getLabel(tc.end), new LabelStmt[] { getLabel(tc.handler) },
        new String[] { tc.type }));
        }

        Analyzer an = new Analyzer(new BasicInterpreter());
        Frame[] frames;
        try {
            frames = an.analyze(owner, source);
        } catch (AnalyzerException ex) {
            throw new RuntimeException(ex);
        }

        final Value[] values = new Value[source.maxLocals + source.maxStack];
        for (int i = 0; i < values.length; i++) {
            Local local = Exprs.nLocal("a" + i);
            target.locals.add(local);
            values[i] = local;
        }

        int x = 0;
        final StmtList stmts = target.stmts;
        if (!target.isStatic) {// not static
            stmts.add(Stmts.nIdentity(values[x++], Exprs.nThisRef(target.owner)));
        }
        for (int i = 0; i < target.args.length; i++) {
            stmts.add(Stmts.nIdentity(values[x++], Exprs.nParameterRef(target.args[i], i)));
        }
        final Local voidLocal = Exprs.nLocal("ignore");
        target.locals.add(voidLocal);
        // FIXME clean all Label.info

        Frame fx = new Frame(source.maxLocals, source.maxStack) {

            public XValue pop() throws IndexOutOfBoundsException {
                org.objectweb.asm.tree.analysis.Value v = super.pop();
                int top = super.getStackSize();
                return new XValue(v.getSize(), values[top + super.getLocals()]);
            }

            @Override
            public org.objectweb.asm.tree.analysis.Value getStack(int i) throws IndexOutOfBoundsException {
                org.objectweb.asm.tree.analysis.Value v = super.getStack(i);
                return new XValue(v.getSize(), values[i + super.getLocals()]);
            }

            @Override
            public XValue getLocal(int i) throws IndexOutOfBoundsException {
                org.objectweb.asm.tree.analysis.Value v = super.getLocal(i);
                return new XValue(v.getSize(), values[i]);
            }

            @Override
            public void execute(AbstractInsnNode insn, Interpreter interpreter) throws AnalyzerException {
                switch (insn.getOpcode()) {
                    case DUP_X1:
                        // BEFORE: ..AB
                        // AFTER:  ..BAB
                        int stack = getStackSize() + getLocals();
                        Value A = values[stack - 2];
                        Value B = values[stack - 1]; // tos now
                        Value C = values[stack - 0]; // next stack position

                        stmts.add(Stmts.nAssign(C, B));
                        stmts.add(Stmts.nAssign(B, A));
                        stmts.add(Stmts.nAssign(A, C));

                        return;
                    case DUP_X2:
                    case DUP2:
                    case DUP2_X1:
                    case DUP2_X2:
                        throw new RuntimeException("not support opcode" + insn.getOpcode());

                }
                super.execute(insn, interpreter);
            }

            @Override
            public void setLocal(int i, org.objectweb.asm.tree.analysis.Value value) throws IndexOutOfBoundsException {
                Value v = ((XValue) value).value;
                if (v != PLACEHOLDER) {
                    stmts.add(Stmts.nAssign(values[i], v));
                }
            }

            public void push(org.objectweb.asm.tree.analysis.Value v) throws IndexOutOfBoundsException {
                int top = super.getStackSize();
                stmts.add(Stmts.nAssign(values[top + super.getLocals()], ((XValue) v).value));
                super.push(v);
            }

            public String toString() {
                return "[Frame]";
            }
        };

        Interpreter<XValue> interpreter = new Interpreter<XValue>(Opcodes.ASM4) {
            @Override
            public XValue newValue(Type type) {
                return new XValue(1, PLACEHOLDER);
            }

            @Override
            public XValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
                switch (insn.getOpcode()) {
                    case ACONST_NULL:
                        return new XValue(1, Exprs.nNull());
                    case ICONST_M1:
                    case ICONST_0:
                    case ICONST_1:
                    case ICONST_2:
                    case ICONST_3:
                    case ICONST_4:
                    case ICONST_5:
                        return new XValue(1, Exprs.nInt(insn.getOpcode() - ICONST_0));
                    case LCONST_0:
                    case LCONST_1:
                        return new XValue(2, Exprs.nLong(insn.getOpcode() - LCONST_0));
                    case FCONST_0:
                    case FCONST_1:
                    case FCONST_2:
                        return new XValue(1, Exprs.nFloat(insn.getOpcode() - FCONST_0));
                    case DCONST_0:
                    case DCONST_1:
                        return new XValue(2, Exprs.nDouble(insn.getOpcode() - DCONST_0));
                    case BIPUSH:
                    case SIPUSH:
                        return new XValue(1, Exprs.nInt(((IntInsnNode) insn).operand));
                    case LDC:
                        Object cst = ((LdcInsnNode) insn).cst;
                        if (cst instanceof Integer) {
                            return new XValue(1, Exprs.nInt((Integer) cst));
                        } else if (cst instanceof Float) {
                            return new XValue(1, Exprs.nFloat((Float) cst));
                        } else if (cst instanceof Long) {
                            return new XValue(2, Exprs.nLong((Long) cst));
                        } else if (cst instanceof Double) {
                            return new XValue(2, Exprs.nDouble((Double) cst));
                        } else if (cst instanceof String) {
                            return new XValue(1, Exprs.nString((String) cst));
                        } else if (cst instanceof Type) {
                            Type type = (Type) cst;
                            int sort = type.getSort();
                            if (sort == Type.OBJECT || sort == Type.ARRAY) {
                                return new XValue(1, Exprs.nType(type.getDescriptor()));
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
                        return new XValue(Type.getType(fin.desc).getSize(), Exprs.nStaticField("L" + fin.owner + ";", fin.name,
                                fin.desc));
                    case NEW:
                        return new XValue(1, Exprs.nNew("L" + ((TypeInsnNode) insn).desc + ";"));
                    default:
                        throw new Error("Internal error.");
                }
            }

            @Override
            public XValue copyOperation(AbstractInsnNode insn, XValue value) throws AnalyzerException {
                return value;
            }

            @Override
            public XValue unaryOperation(AbstractInsnNode insn, XValue value0) throws AnalyzerException {
                XValue value = (XValue) value0;
                switch (insn.getOpcode()) {
                    case INEG:
                        return new XValue(1, Exprs.nNeg(value.value, "I"));
                    case IINC:
                        return new XValue(1, Exprs.nAdd(value.value, Exprs.nInt(((IincInsnNode) insn).incr), "I"));
                    case L2I:
                        return new XValue(1, Exprs.nCast(value.value, "J", "I"));
                    case F2I:
                        return new XValue(1, Exprs.nCast(value.value, "F", "I"));
                    case D2I:
                        return new XValue(1, Exprs.nCast(value.value, "D", "I"));
                    case I2B:
                        return new XValue(1, Exprs.nCast(value.value, "I", "B"));
                    case I2C:
                        return new XValue(1, Exprs.nCast(value.value, "I", "C"));
                    case I2S:
                        return new XValue(1, Exprs.nCast(value.value, "I", "S"));
                    case FNEG:
                        return new XValue(1, Exprs.nNeg(value.value, "F"));
                    case I2F:
                        return new XValue(1, Exprs.nCast(value.value, "I", "F"));
                    case L2F:
                        return new XValue(1, Exprs.nCast(value.value, "J", "F"));
                    case D2F:
                        return new XValue(1, Exprs.nCast(value.value, "D", "F"));
                    case LNEG:
                        return new XValue(2, Exprs.nNeg(value.value, "J"));
                    case I2L:
                        return new XValue(2, Exprs.nCast(value.value, "I", "J"));
                    case F2L:
                        return new XValue(2, Exprs.nCast(value.value, "F", "J"));
                    case D2L:
                        return new XValue(2, Exprs.nCast(value.value, "D", "J"));
                    case DNEG:
                        return new XValue(2, Exprs.nNeg(value.value, "D"));
                    case I2D:
                        return new XValue(2, Exprs.nCast(value.value, "I", "D"));
                    case L2D:
                        return new XValue(2, Exprs.nCast(value.value, "J", "D"));
                    case F2D:
                        return new XValue(2, Exprs.nCast(value.value, "F", "D"));
                    case IFEQ:
                        stmts.add(Stmts.nIf(Exprs.nEq(value.value, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFNE:
                        stmts.add(Stmts.nIf(Exprs.nNe(value.value, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFLT:
                        stmts.add(Stmts.nIf(Exprs.nLt(value.value, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFGE:
                        stmts.add(Stmts.nIf(Exprs.nGe(value.value, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFGT:
                        stmts.add(Stmts.nIf(Exprs.nGt(value.value, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFLE:
                        stmts.add(Stmts.nIf(Exprs.nLe(value.value, Exprs.nInt(0), "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case TABLESWITCH: {
                        TableSwitchInsnNode ts = (TableSwitchInsnNode) insn;
                        LabelStmt targets[] = new LabelStmt[ts.labels.size()];
                        for (int i = 0; i < ts.labels.size(); i++) {
                            targets[i] = getLabel((LabelNode) ts.labels.get(i));
                        }
                        stmts.add(Stmts.nTableSwitch(value.value, ts.min, targets, getLabel(ts.dflt)));
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
                        stmts.add(Stmts.nLookupSwitch(value.value, lookupValues, targets, getLabel(ls.dflt)));
                        return null;
                    }
                    case IRETURN:
                    case LRETURN:
                    case FRETURN:
                    case DRETURN:
                    case ARETURN:
                        stmts.add(Stmts.nReturn(value.value));
                        return null;
                    case PUTSTATIC: {
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        stmts.add(Stmts.nAssign(Exprs.nStaticField("L" + fin.owner + ";", fin.name, fin.desc), value.value));
                        return null;
                    }
                    case GETFIELD: {
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        Type fieldType = Type.getType(fin.desc);
                        return new XValue(fieldType.getSize(), Exprs.nField(value.value, "L" + fin.owner + ";", fin.name, fin.desc));
                    }
                    case NEWARRAY:
                        switch (((IntInsnNode) insn).operand) {
                            case T_BOOLEAN:
                                return new XValue(1, Exprs.nNewArray("Z", value.value));
                            case T_CHAR:
                                return new XValue(1, Exprs.nNewArray("C", value.value));
                            case T_BYTE:
                                return new XValue(1, Exprs.nNewArray("B", value.value));
                            case T_SHORT:
                                return new XValue(1, Exprs.nNewArray("S", value.value));
                            case T_INT:
                                return new XValue(1, Exprs.nNewArray("I", value.value));
                            case T_FLOAT:
                                return new XValue(1, Exprs.nNewArray("F", value.value));
                            case T_DOUBLE:
                                return new XValue(1, Exprs.nNewArray("D", value.value));
                            case T_LONG:
                                return new XValue(1, Exprs.nNewArray("D", value.value));
                            default:
                                throw new AnalyzerException(insn, "Invalid array type");
                        }
                    case ANEWARRAY:
                        String desc = "L" + ((TypeInsnNode) insn).desc + ";";
                        return new XValue(1, Exprs.nNewArray(desc, value.value));
                    case ARRAYLENGTH:
                        return new XValue(1, Exprs.nLength(value.value));
                    case ATHROW:
                        stmts.add(Stmts.nThrow(value.value));
                        return null;
                    case CHECKCAST:
                        desc = "L" + ((TypeInsnNode) insn).desc + ";";
                        return new XValue(1, Exprs.nCheckCast(value.value, desc));
                    case INSTANCEOF:
                        return new XValue(1, Exprs.nInstanceOf(value.value, "L" + ((TypeInsnNode) insn).desc + ";"));
                    case MONITORENTER:
                        stmts.add(Stmts.nLock(value.value));
                        return null;
                    case MONITOREXIT:
                        stmts.add(Stmts.nUnLock(value.value));
                        return null;
                    case IFNULL:
                        stmts.add(Stmts.nIf(Exprs.nEq(value.value, Exprs.nNull(), "L"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IFNONNULL:
                        stmts.add(Stmts.nIf(Exprs.nNe(value.value, Exprs.nNull(), "L"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    default:
                        throw new Error("Internal error.");
                }
            }

            @Override
            public XValue binaryOperation(AbstractInsnNode insn, XValue value10, XValue value20)
                    throws AnalyzerException {
                XValue value1 = (XValue) value10;
                XValue value2 = (XValue) value20;
                switch (insn.getOpcode()) {

                    case IALOAD:
                        return new XValue(1, Exprs.nArray(value1.value, value2.value, "I"));
                    case BALOAD:
                        return new XValue(1, Exprs.nArray(value1.value, value2.value, "B"));
                    case CALOAD:
                        return new XValue(1, Exprs.nArray(value1.value, value2.value, "C"));
                    case SALOAD:
                        return new XValue(1, Exprs.nArray(value1.value, value2.value, "S"));
                    case FALOAD:
                        return new XValue(1, Exprs.nArray(value1.value, value2.value, "F"));
                    case AALOAD:
                        return new XValue(1, Exprs.nArray(value1.value, value2.value, "L"));
                    case DALOAD:
                        return new XValue(1, Exprs.nArray(value1.value, value2.value, "D"));
                    case LALOAD:
                        return new XValue(1, Exprs.nArray(value1.value, value2.value, "J"));
                    case IADD:
                        return new XValue(1, Exprs.nAdd(value1.value, value2.value, "I"));
                    case ISUB:
                        return new XValue(1, Exprs.nSub(value1.value, value2.value, "I"));
                    case IMUL:
                        return new XValue(1, Exprs.nMul(value1.value, value2.value, "I"));
                    case IDIV:
                        return new XValue(1, Exprs.nDiv(value1.value, value2.value, "I"));
                    case IREM:
                        return new XValue(1, Exprs.nRem(value1.value, value2.value, "I"));
                    case ISHL:
                        return new XValue(1, Exprs.nShl(value1.value, value2.value, "I"));
                    case ISHR:
                        return new XValue(1, Exprs.nShr(value1.value, value2.value, "I"));
                    case IUSHR:
                        return new XValue(1, Exprs.nUshr(value1.value, value2.value, "I"));
                    case IAND:
                        return new XValue(1, Exprs.nAnd(value1.value, value2.value, "I"));
                    case IOR:
                        return new XValue(1, Exprs.nOr(value1.value, value2.value, "I"));
                    case IXOR:
                        return new XValue(1, Exprs.nXor(value1.value, value2.value, "I"));
                    case FADD:
                        return new XValue(1, Exprs.nAdd(value1.value, value2.value, "F"));
                    case FSUB:
                        return new XValue(1, Exprs.nSub(value1.value, value2.value, "F"));
                    case FMUL:
                        return new XValue(1, Exprs.nMul(value1.value, value2.value, "F"));
                    case FDIV:
                        return new XValue(1, Exprs.nDiv(value1.value, value2.value, "F"));
                    case FREM:
                        return new XValue(1, Exprs.nRem(value1.value, value2.value, "F"));
                    case LADD:
                        return new XValue(2, Exprs.nAdd(value1.value, value2.value, "J"));
                    case LSUB:
                        return new XValue(2, Exprs.nSub(value1.value, value2.value, "J"));
                    case LMUL:
                        return new XValue(2, Exprs.nMul(value1.value, value2.value, "J"));
                    case LDIV:
                        return new XValue(2, Exprs.nDiv(value1.value, value2.value, "J"));
                    case LREM:
                        return new XValue(2, Exprs.nRem(value1.value, value2.value, "J"));
                    case LSHL:
                        return new XValue(2, Exprs.nShl(value1.value, value2.value, "J"));
                    case LSHR:
                        return new XValue(2, Exprs.nShr(value1.value, value2.value, "J"));
                    case LUSHR:
                        return new XValue(2, Exprs.nUshr(value1.value, value2.value, "J"));
                    case LAND:
                        return new XValue(2, Exprs.nAnd(value1.value, value2.value, "J"));
                    case LOR:
                        return new XValue(2, Exprs.nOr(value1.value, value2.value, "J"));
                    case LXOR:
                        return new XValue(2, Exprs.nXor(value1.value, value2.value, "J"));

                    case DADD:
                        return new XValue(2, Exprs.nAdd(value1.value, value2.value, "D"));
                    case DSUB:
                        return new XValue(2, Exprs.nSub(value1.value, value2.value, "D"));
                    case DMUL:
                        return new XValue(2, Exprs.nMul(value1.value, value2.value, "D"));
                    case DDIV:
                        return new XValue(2, Exprs.nDiv(value1.value, value2.value, "D"));
                    case DREM:
                        return new XValue(2, Exprs.nRem(value1.value, value2.value, "D"));

                    case LCMP:
                        return new XValue(2, Exprs.nLCmp(value1.value, value2.value));
                    case FCMPL:
                        return new XValue(1, Exprs.nFCmpl(value1.value, value2.value));
                    case FCMPG:
                        return new XValue(1, Exprs.nFCmpg(value1.value, value2.value));
                    case DCMPL:
                        return new XValue(2, Exprs.nDCmpl(value1.value, value2.value));
                    case DCMPG:
                        return new XValue(2, Exprs.nDCmpg(value1.value, value2.value));

                    case IF_ICMPEQ:
                        stmts.add(Stmts.nIf(Exprs.nEq(value1.value, value2.value, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPNE:
                        stmts.add(Stmts.nIf(Exprs.nNe(value1.value, value2.value, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPLT:
                        stmts.add(Stmts.nIf(Exprs.nLt(value1.value, value2.value, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPGE:
                        stmts.add(Stmts.nIf(Exprs.nGe(value1.value, value2.value, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPGT:
                        stmts.add(Stmts.nIf(Exprs.nGt(value1.value, value2.value, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ICMPLE:
                        stmts.add(Stmts.nIf(Exprs.nLe(value1.value, value2.value, "I"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ACMPEQ:
                        stmts.add(Stmts.nIf(Exprs.nEq(value1.value, value2.value, "L"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case IF_ACMPNE:
                        stmts.add(Stmts.nIf(Exprs.nNe(value1.value, value2.value, "L"),
                                getLabel(((JumpInsnNode) insn).label)));
                        return null;
                    case PUTFIELD:
                        FieldInsnNode fin = (FieldInsnNode) insn;
                        stmts.add(Stmts.nAssign(Exprs.nField(value1.value, "L" + fin.owner + ";", fin.name, fin.desc), value2.value));
                        return null;
                    default:
                        throw new Error("Internal error.");
                }
            }

            @Override
            public XValue ternaryOperation(AbstractInsnNode insn, XValue value1, XValue value2, XValue value3)
                    throws AnalyzerException {
                switch (insn.getOpcode()) {
                    case IASTORE:
                        stmts.add(Stmts.nAssign(Exprs.nArray(((XValue) value1).value, ((XValue) value2).value, "I"),
                                ((XValue) value3).value));
                        break;
                    case LASTORE:
                        stmts.add(Stmts.nAssign(Exprs.nArray(((XValue) value1).value, ((XValue) value2).value, "J"),
                                ((XValue) value3).value));
                        break;
                    case FASTORE:
                        stmts.add(Stmts.nAssign(Exprs.nArray(((XValue) value1).value, ((XValue) value2).value, "F"),
                                ((XValue) value3).value));
                        break;
                    case DASTORE:
                        stmts.add(Stmts.nAssign(Exprs.nArray(((XValue) value1).value, ((XValue) value2).value, "D"),
                                ((XValue) value3).value));
                        break;
                    case AASTORE:
                        stmts.add(Stmts.nAssign(Exprs.nArray(((XValue) value1).value, ((XValue) value2).value, "L"),
                                ((XValue) value3).value));
                        break;
                    case BASTORE:
                        stmts.add(Stmts.nAssign(Exprs.nArray(((XValue) value1).value, ((XValue) value2).value, "B"),
                                ((XValue) value3).value));
                        break;
                    case CASTORE:
                        stmts.add(Stmts.nAssign(Exprs.nArray(((XValue) value1).value, ((XValue) value2).value, "C"),
                                ((XValue) value3).value));
                        break;
                    case SASTORE:
                        stmts.add(Stmts.nAssign(Exprs.nArray(((XValue) value1).value, ((XValue) value2).value, "S"),
                                ((XValue) value3).value));
                        break;
                }

                return null;
            }

            @Override
            public XValue naryOperation(AbstractInsnNode insn, List xvalues) throws AnalyzerException {

                Value values[] = new Value[xvalues.size()];
                for (int i = 0; i < xvalues.size(); i++) {
                    values[i] = ((XValue) xvalues.get(i)).value;
                }
                if (insn.getOpcode() == MULTIANEWARRAY) {
                    throw new UnsupportedOperationException("Not supported yet.");
                } else {
                    MethodInsnNode mi = (MethodInsnNode) insn;
                    Value v = null;
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
                        stmts.add(Stmts.nVoidInvoke(v));
                        return null;
                    } else {
                        return new XValue(Type.getReturnType(mi.desc).getSize(), v);
                    }
                }
            }

            @Override
            public XValue merge(XValue v, XValue w) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void returnOperation(AbstractInsnNode insn, XValue value, XValue expected) throws AnalyzerException {
                stmts.add(Stmts.nReturn(((XValue) value).value));
            }
        };

        for (AbstractInsnNode p = source.instructions.getFirst(); p != null; p = p.getNext()) {
            if (p.getType() == AbstractInsnNode.LABEL) {
                stmts.add(getLabel((LabelNode) p));
            } else if (p.getOpcode() == GOTO) {
                stmts.add(Stmts.nGoto(getLabel(((JumpInsnNode) p).label)));
            } else if (p.getOpcode() == RETURN) {
                stmts.add(Stmts.nReturnVoid());
            } else {
                fx.init(frames[source.instructions.indexOf(p)]);
                fx.setReturn(null); // we don't need return value, cause ClassCastException if return not set to null
                try {
                    fx.execute(p, interpreter);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return target;
    }

    private IrMethod populate(String owner, MethodNode source) {
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

    static class XValue implements org.objectweb.asm.tree.analysis.Value {

        public Value value;
        int size;

        public XValue(int size, Value value) {
            this.size = size;
            this.value = value;
        }

        @Override
        public int getSize() {
            return size;
        }
    }
}
