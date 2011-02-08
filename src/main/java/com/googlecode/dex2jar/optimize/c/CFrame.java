/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.googlecode.dex2jar.optimize.c;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

/**
 * !!!!!MODIFIED A symbolic execution stack frame. A stack frame contains a set of local variable slots, and an operand
 * stack. Warning: long and double values are represented by <i>two</i> slots in local variables, and by <i>one</i> slot
 * in the operand stack.
 * 
 * @author Eric Bruneton
 */
public class CFrame extends org.objectweb.asm.tree.analysis.Frame {

    /**
     * The expected return type of the analyzed method, or <tt>null</tt> if the method returns void.
     */
    private Value returnValue;

    /**
     * The local variables and operand stack of this frame.
     */
    private Value[] localValues;

    /**
     * The local variables and operand stack of this frame.
     */
    private Value[] stackValues;

    /**
     * The number of elements in the operand stack.
     */
    private int top;

    /**
     * Constructs a new frame with the given size.
     * 
     * @param nLocals
     *            the maximum number of local variables of the frame.
     * @param nStack
     *            the maximum stack size of the frame.
     */
    public CFrame(final int nLocals, final int nStack) {
        super(nLocals, nStack);
        this.localValues = new Value[nLocals];
        this.stackValues = new Value[nStack];
    }

    /**
     * Constructs a new frame that is identical to the given frame.
     * 
     * @param src
     *            a frame.
     */
    public CFrame(final CFrame src) {
        this(src.localValues.length, src.stackValues.length);
        init(src);
    }

    /**
     * Copies the state of the given frame into this frame.
     * 
     * @param src
     *            a frame.
     * @return this frame.
     */
    public CFrame init(final Frame s) {
        CFrame src = (CFrame) s;
        returnValue = src.returnValue;
        localValues = new Value[src.localValues.length];
        stackValues = new Value[src.stackValues.length];
        System.arraycopy(src.localValues, 0, localValues, 0, localValues.length);
        System.arraycopy(src.stackValues, 0, stackValues, 0, stackValues.length);
        top = src.top;
        return this;
    }

    /**
     * Sets the expected return type of the analyzed method.
     * 
     * @param v
     *            the expected return type of the analyzed method, or <tt>null</tt> if the method returns void.
     */
    public void setReturn(final Value v) {
        returnValue = v;
    }

    /**
     * Returns the value of the given local variable.
     * 
     * @param i
     *            a local variable index.
     * @return the value of the given local variable.
     * @throws IndexOutOfBoundsException
     *             if the variable does not exist.
     */
    public Value getLocal(final int i) throws IndexOutOfBoundsException {
        if (i >= localValues.length) {
            Value[] temp = new Value[i + 2];
            System.arraycopy(localValues, 0, temp, 0, localValues.length);
            localValues = temp;
            for (int j = localValues.length; j < temp.length; j++) {
                temp[j] = new CBasicValue(null);
            }
        }
        Value v = localValues[i];
        if (v == null) {
            v = new CBasicValue(null);
        }
        localValues[i] = v;
        return v;
    }

    /**
     * Sets the value of the given local variable.
     * 
     * @param i
     *            a local variable index.
     * @param value
     *            the new value of this local variable.
     * @throws IndexOutOfBoundsException
     *             if the variable does not exist.
     */
    public void setLocal(final int i, final Value value) throws IndexOutOfBoundsException {
        if (i >= localValues.length) {
            Value[] temp = new Value[i + 1];
            System.arraycopy(localValues, 0, temp, 0, localValues.length);
            localValues = temp;
            for (int j = localValues.length; j < temp.length; j++) {
                temp[j] = new CBasicValue(null);
            }
        }
        localValues[i] = value;
    }

    /**
     * Returns the number of values in the operand stack of this frame. Long and double values are treated as single
     * values.
     * 
     * @return the number of values in the operand stack of this frame.
     */
    public int getStackSize() {
        return top;
    }

    /**
     * Returns the value of the given operand stack slot.
     * 
     * @param i
     *            the index of an operand stack slot.
     * @return the value of the given operand stack slot.
     * @throws IndexOutOfBoundsException
     *             if the operand stack slot does not exist.
     */
    public Value getStack(final int i) throws IndexOutOfBoundsException {
        if (top >= stackValues.length || i >= stackValues.length) {
            Value[] temp = new Value[Math.max(top, i) + 1];
            System.arraycopy(stackValues, 0, temp, 0, stackValues.length);
            stackValues = temp;
            for (int j = stackValues.length; j < temp.length; j++) {
                temp[j] = new CBasicValue(null);
            }
        }
        return stackValues[i];
    }

    /**
     * Clears the operand stack of this frame.
     */
    public void clearStack() {
        top = 0;
    }

    /**
     * Pops a value from the operand stack of this frame.
     * 
     * @return the value that has been popped from the stack.
     * @throws IndexOutOfBoundsException
     *             if the operand stack is empty.
     */
    public Value pop() throws IndexOutOfBoundsException {
        if (top == 0) {
            throw new IndexOutOfBoundsException("Cannot pop operand off an empty stack.");
        }
        return this.stackValues[--top];
    }

    public Value peek() throws IndexOutOfBoundsException {
        if (top == 0) {
            throw new IndexOutOfBoundsException("Cannot pop operand off an empty stack.");
        }
        return this.stackValues[top - 1];
    }

    /**
     * Pushes a value into the operand stack of this frame.
     * 
     * @param value
     *            the value that must be pushed into the stack.
     * @throws IndexOutOfBoundsException
     *             if the operand stack is full.
     */
    public void push(final Value value) throws IndexOutOfBoundsException {
        if (top >= stackValues.length) {
            Value[] temp = new Value[top + 1];
            System.arraycopy(stackValues, 0, temp, 0, stackValues.length);
            stackValues = temp;
        }
        stackValues[top++] = value;
    }

    public void execute(final AbstractInsnNode insn, final Interpreter interpreter) throws AnalyzerException {
        Value value1, value2, value3, value4;
        List values;
        int var;

        switch (insn.getOpcode()) {
        case Opcodes.NOP:
            break;
        case Opcodes.ACONST_NULL:
        case Opcodes.ICONST_M1:
        case Opcodes.ICONST_0:
        case Opcodes.ICONST_1:
        case Opcodes.ICONST_2:
        case Opcodes.ICONST_3:
        case Opcodes.ICONST_4:
        case Opcodes.ICONST_5:
        case Opcodes.LCONST_0:
        case Opcodes.LCONST_1:
        case Opcodes.FCONST_0:
        case Opcodes.FCONST_1:
        case Opcodes.FCONST_2:
        case Opcodes.DCONST_0:
        case Opcodes.DCONST_1:
        case Opcodes.BIPUSH:
        case Opcodes.SIPUSH:
        case Opcodes.LDC:
            push(interpreter.newOperation(insn));
            break;
        case Opcodes.ILOAD:
        case Opcodes.LLOAD:
        case Opcodes.FLOAD:
        case Opcodes.DLOAD:
        case Opcodes.ALOAD:
            push(interpreter.copyOperation(insn, getLocal(((VarInsnNode) insn).var)));
            break;
        case Opcodes.IALOAD:
        case Opcodes.LALOAD:
        case Opcodes.FALOAD:
        case Opcodes.DALOAD:
        case Opcodes.AALOAD:
        case Opcodes.BALOAD:
        case Opcodes.CALOAD:
        case Opcodes.SALOAD:
            value2 = pop();
            value1 = pop();
            push(interpreter.binaryOperation(insn, value1, value2));
            break;
        case Opcodes.ISTORE:
        case Opcodes.LSTORE:
        case Opcodes.FSTORE:
        case Opcodes.DSTORE:
        case Opcodes.ASTORE:
            value1 = interpreter.copyOperation(insn, pop());
            var = ((VarInsnNode) insn).var;
            setLocal(var, value1);
            if (value1.getSize() == 2) {
                setLocal(var + 1, interpreter.newValue(null));
            }
            if (var > 0) {
                Value local = getLocal(var - 1);
                if (local != null && local.getSize() == 2) {
                    setLocal(var - 1, interpreter.newValue(null));
                }
            }
            break;
        case Opcodes.IASTORE:
        case Opcodes.LASTORE:
        case Opcodes.FASTORE:
        case Opcodes.DASTORE:
        case Opcodes.AASTORE:
        case Opcodes.BASTORE:
        case Opcodes.CASTORE:
        case Opcodes.SASTORE:
            value3 = pop();
            value2 = pop();
            value1 = pop();
            interpreter.ternaryOperation(insn, value1, value2, value3);
            break;
        case Opcodes.POP:
            if (pop().getSize() == 2) {
                throw new AnalyzerException("Illegal use of POP");
            }
            break;
        case Opcodes.POP2:
            if (pop().getSize() == 1) {
                if (pop().getSize() != 1) {
                    throw new AnalyzerException("Illegal use of POP2");
                }
            }
            break;
        case Opcodes.DUP:
            value1 = pop();
            if (value1.getSize() != 1) {
                throw new AnalyzerException("Illegal use of DUP");
            }
            push(value1);
            push(interpreter.copyOperation(insn, value1));
            break;
        case Opcodes.DUP_X1:
            value1 = pop();
            value2 = pop();
            if (value1.getSize() != 1 || value2.getSize() != 1) {
                throw new AnalyzerException("Illegal use of DUP_X1");
            }
            push(interpreter.copyOperation(insn, value1));
            push(value2);
            push(value1);
            break;
        case Opcodes.DUP_X2:
            value1 = pop();
            if (value1.getSize() == 1) {
                value2 = pop();
                if (value2.getSize() == 1) {
                    value3 = pop();
                    if (value3.getSize() == 1) {
                        push(interpreter.copyOperation(insn, value1));
                        push(value3);
                        push(value2);
                        push(value1);
                        break;
                    }
                } else {
                    push(interpreter.copyOperation(insn, value1));
                    push(value2);
                    push(value1);
                    break;
                }
            }
            throw new AnalyzerException("Illegal use of DUP_X2");
        case Opcodes.DUP2:
            value1 = pop();
            if (value1.getSize() == 1) {
                value2 = pop();
                if (value2.getSize() == 1) {
                    push(value2);
                    push(value1);
                    push(interpreter.copyOperation(insn, value2));
                    push(interpreter.copyOperation(insn, value1));
                    break;
                }
            } else {
                push(value1);
                push(interpreter.copyOperation(insn, value1));
                break;
            }
            throw new AnalyzerException("Illegal use of DUP2");
        case Opcodes.DUP2_X1:
            value1 = pop();
            if (value1.getSize() == 1) {
                value2 = pop();
                if (value2.getSize() == 1) {
                    value3 = pop();
                    if (value3.getSize() == 1) {
                        push(interpreter.copyOperation(insn, value2));
                        push(interpreter.copyOperation(insn, value1));
                        push(value3);
                        push(value2);
                        push(value1);
                        break;
                    }
                }
            } else {
                value2 = pop();
                if (value2.getSize() == 1) {
                    push(interpreter.copyOperation(insn, value1));
                    push(value2);
                    push(value1);
                    break;
                }
            }
            throw new AnalyzerException("Illegal use of DUP2_X1");
        case Opcodes.DUP2_X2:
            value1 = pop();
            if (value1.getSize() == 1) {
                value2 = pop();
                if (value2.getSize() == 1) {
                    value3 = pop();
                    if (value3.getSize() == 1) {
                        value4 = pop();
                        if (value4.getSize() == 1) {
                            push(interpreter.copyOperation(insn, value2));
                            push(interpreter.copyOperation(insn, value1));
                            push(value4);
                            push(value3);
                            push(value2);
                            push(value1);
                            break;
                        }
                    } else {
                        push(interpreter.copyOperation(insn, value2));
                        push(interpreter.copyOperation(insn, value1));
                        push(value3);
                        push(value2);
                        push(value1);
                        break;
                    }
                }
            } else {
                value2 = pop();
                if (value2.getSize() == 1) {
                    value3 = pop();
                    if (value3.getSize() == 1) {
                        push(interpreter.copyOperation(insn, value1));
                        push(value3);
                        push(value2);
                        push(value1);
                        break;
                    }
                } else {
                    push(interpreter.copyOperation(insn, value1));
                    push(value2);
                    push(value1);
                    break;
                }
            }
            throw new AnalyzerException("Illegal use of DUP2_X2");
        case Opcodes.SWAP:
            value2 = pop();
            value1 = pop();
            if (value1.getSize() != 1 || value2.getSize() != 1) {
                throw new AnalyzerException("Illegal use of SWAP");
            }
            push(interpreter.copyOperation(insn, value2));
            push(interpreter.copyOperation(insn, value1));
            break;
        case Opcodes.IADD:
        case Opcodes.LADD:
        case Opcodes.FADD:
        case Opcodes.DADD:
        case Opcodes.ISUB:
        case Opcodes.LSUB:
        case Opcodes.FSUB:
        case Opcodes.DSUB:
        case Opcodes.IMUL:
        case Opcodes.LMUL:
        case Opcodes.FMUL:
        case Opcodes.DMUL:
        case Opcodes.IDIV:
        case Opcodes.LDIV:
        case Opcodes.FDIV:
        case Opcodes.DDIV:
        case Opcodes.IREM:
        case Opcodes.LREM:
        case Opcodes.FREM:
        case Opcodes.DREM:
            value2 = pop();
            value1 = pop();
            push(interpreter.binaryOperation(insn, value1, value2));
            break;
        case Opcodes.INEG:
        case Opcodes.LNEG:
        case Opcodes.FNEG:
        case Opcodes.DNEG:
            push(interpreter.unaryOperation(insn, pop()));
            break;
        case Opcodes.ISHL:
        case Opcodes.LSHL:
        case Opcodes.ISHR:
        case Opcodes.LSHR:
        case Opcodes.IUSHR:
        case Opcodes.LUSHR:
        case Opcodes.IAND:
        case Opcodes.LAND:
        case Opcodes.IOR:
        case Opcodes.LOR:
        case Opcodes.IXOR:
        case Opcodes.LXOR:
            value2 = pop();
            value1 = pop();
            push(interpreter.binaryOperation(insn, value1, value2));
            break;
        case Opcodes.IINC:
            var = ((IincInsnNode) insn).var;
            setLocal(var, interpreter.unaryOperation(insn, getLocal(var)));
            break;
        case Opcodes.I2L:
        case Opcodes.I2F:
        case Opcodes.I2D:
        case Opcodes.L2I:
        case Opcodes.L2F:
        case Opcodes.L2D:
        case Opcodes.F2I:
        case Opcodes.F2L:
        case Opcodes.F2D:
        case Opcodes.D2I:
        case Opcodes.D2L:
        case Opcodes.D2F:
        case Opcodes.I2B:
        case Opcodes.I2C:
        case Opcodes.I2S:
            push(interpreter.unaryOperation(insn, pop()));
            break;
        case Opcodes.LCMP:
        case Opcodes.FCMPL:
        case Opcodes.FCMPG:
        case Opcodes.DCMPL:
        case Opcodes.DCMPG:
            value2 = pop();
            value1 = pop();
            push(interpreter.binaryOperation(insn, value1, value2));
            break;
        case Opcodes.IFEQ:
        case Opcodes.IFNE:
        case Opcodes.IFLT:
        case Opcodes.IFGE:
        case Opcodes.IFGT:
        case Opcodes.IFLE:
            interpreter.unaryOperation(insn, pop());
            break;
        case Opcodes.IF_ICMPEQ:
        case Opcodes.IF_ICMPNE:
        case Opcodes.IF_ICMPLT:
        case Opcodes.IF_ICMPGE:
        case Opcodes.IF_ICMPGT:
        case Opcodes.IF_ICMPLE:
        case Opcodes.IF_ACMPEQ:
        case Opcodes.IF_ACMPNE:
            value2 = pop();
            value1 = pop();
            interpreter.binaryOperation(insn, value1, value2);
            break;
        case Opcodes.GOTO:
            break;
        case Opcodes.JSR:
            push(interpreter.newOperation(insn));
            break;
        case Opcodes.RET:
            break;
        case Opcodes.TABLESWITCH:
        case Opcodes.LOOKUPSWITCH:
            interpreter.unaryOperation(insn, pop());
            break;
        case Opcodes.IRETURN:
        case Opcodes.LRETURN:
        case Opcodes.FRETURN:
        case Opcodes.DRETURN:
        case Opcodes.ARETURN:
            value1 = pop();
            interpreter.unaryOperation(insn, value1);
            interpreter.returnOperation(insn, value1, returnValue);
            break;
        case Opcodes.RETURN:
            if (returnValue != null) {
                throw new AnalyzerException("Incompatible return type");
            }
            break;
        case Opcodes.GETSTATIC:
            push(interpreter.newOperation(insn));
            break;
        case Opcodes.PUTSTATIC:
            interpreter.unaryOperation(insn, pop());
            break;
        case Opcodes.GETFIELD:
            push(interpreter.unaryOperation(insn, pop()));
            break;
        case Opcodes.PUTFIELD:
            value2 = pop();
            value1 = pop();
            interpreter.binaryOperation(insn, value1, value2);
            break;
        case Opcodes.INVOKEVIRTUAL:
        case Opcodes.INVOKESPECIAL:
        case Opcodes.INVOKESTATIC:
        case Opcodes.INVOKEINTERFACE:
        case Opcodes.INVOKEDYNAMIC:
            values = new ArrayList();
            String desc = ((MethodInsnNode) insn).desc;
            for (int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
                values.add(0, pop());
            }
            if (insn.getOpcode() != Opcodes.INVOKESTATIC && insn.getOpcode() != Opcodes.INVOKEDYNAMIC) {
                values.add(0, pop());
            }
            if (Type.getReturnType(desc) == Type.VOID_TYPE) {
                interpreter.naryOperation(insn, values);
            } else {
                push(interpreter.naryOperation(insn, values));
            }
            break;
        case Opcodes.NEW:
            push(interpreter.newOperation(insn));
            break;
        case Opcodes.NEWARRAY:
        case Opcodes.ANEWARRAY:
        case Opcodes.ARRAYLENGTH:
            push(interpreter.unaryOperation(insn, pop()));
            break;
        case Opcodes.ATHROW:
            interpreter.unaryOperation(insn, pop());
            break;
        case Opcodes.CHECKCAST:
        case Opcodes.INSTANCEOF:
            push(interpreter.unaryOperation(insn, pop()));
            break;
        case Opcodes.MONITORENTER:
        case Opcodes.MONITOREXIT:
            interpreter.unaryOperation(insn, pop());
            break;
        case Opcodes.MULTIANEWARRAY:
            values = new ArrayList();
            for (int i = ((MultiANewArrayInsnNode) insn).dims; i > 0; --i) {
                values.add(0, pop());
            }
            push(interpreter.naryOperation(insn, values));
            break;
        case Opcodes.IFNULL:
        case Opcodes.IFNONNULL:
            interpreter.unaryOperation(insn, pop());
            break;
        default:
            throw new RuntimeException("Illegal opcode " + insn.getOpcode());
        }
    }

    /**
     * Merges this frame with the given frame.
     * 
     * @param frame
     *            a frame.
     * @param interpreter
     *            the interpreter used to merge values.
     * @return <tt>true</tt> if this frame has been changed as a result of the merge operation, or <tt>false</tt>
     *         otherwise.
     * @throws AnalyzerException
     *             if the frames have incompatible sizes.
     */
    public boolean merge(final Frame f, final Interpreter interpreter) throws AnalyzerException {
        CFrame frame = (CFrame) f;
        if (top != frame.top) {
            throw new AnalyzerException("Incompatible stack heights");
        }
        boolean changes = false;
        for (int i = 0; i < Math.min(localValues.length, frame.localValues.length); ++i) {
            if (localValues[i] == null)
                continue;
            Value v = interpreter.merge(localValues[i], frame.localValues[i]);
            if (v != localValues[i]) {
                localValues[i] = v;
                changes |= true;
            }
        }
        for (int i = 0; i < top; ++i) {
            Value v = interpreter.merge(stackValues[i], frame.stackValues[i]);
            if (v != stackValues[i]) {
                stackValues[i] = v;
                changes |= true;
            }
        }
        return changes;
    }

    /**
     * Merges this frame with the given frame (case of a RET instruction).
     * 
     * @param frame
     *            a frame
     * @param access
     *            the local variables that have been accessed by the subroutine to which the RET instruction
     *            corresponds.
     * @return <tt>true</tt> if this frame has been changed as a result of the merge operation, or <tt>false</tt>
     *         otherwise.
     */
    public boolean merge(final Frame f, final boolean[] access) {
        CFrame frame = (CFrame) f;
        boolean changes = false;
        for (int i = 0; i < Math.min(frame.localValues.length, localValues.length); ++i) {
            if (!access[i] && !localValues[i].equals(frame.localValues[i])) {
                localValues[i] = frame.localValues[i];
                changes = true;
            }
        }
        return changes;
    }

    /**
     * Returns a string representation of this frame.
     * 
     * @return a string representation of this frame.
     */
    public String toString() {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < localValues.length; ++i) {
            Value v = getLocal(i);
            if (v == null) {
                b.append("N");
            } else {
                b.append(v);
            }
        }
        b.append(' ');
        for (int i = 0; i < getStackSize(); ++i) {
            Value v = getStack(i);
            if (v == null) {
                b.append("N");
            } else {
                b.append(v.toString());
            }
        }
        return b.toString();
    }

    public String toString(int length) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < localValues.length; ++i) {
            Value v = getLocal(i);
            if (v == null) {
                b.append("N");
            } else {
                b.append(v);
            }
        }
        String L = b.toString();
        b.setLength(0);
        for (int i = getStackSize() - 1; i >= 0; --i) {
            Value v = getStack(i);
            if (v == null) {
                b.append("N");
            } else {
                b.append(v.toString());
            }
        }
        String S = b.toString();
        return String.format("%s%" + (length - L.length()) + "s", L, S);
    }
}
