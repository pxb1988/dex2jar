/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

/**
 * Represent a local/constant/expression
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public abstract class Value implements Cloneable {
    public void setOp(Value op) {
    }

    public void setOp1(Value op) {
    }

    public void setOp2(Value op) {
    }

    public void setOps(Value[] op) {
    }

    /**
     * Represent an expression with no argument
     * 
     * @see ET#E0
     */
    public static abstract class E0Expr extends Value {

        public E0Expr(VT vt) {
            super(vt, ET.E0);
        }

    }

    /**
     * Represent an expression with 1 argument
     * 
     * @see ET#E1
     */
    public static abstract class E1Expr extends Value {

        public Value op;

        public void setOp(Value op) {
            this.op = op;
        }

        /**
         * @param vt
         * @param op
         *            the value should be trimmed
         */
        public E1Expr(VT vt, Value op) {
            super(vt, ET.E1);
            this.op = op;
        }

        @Override
        public Value getOp() {
            return op;
        }

        @Override
        protected void releaseMemory() {
            op = null;
        }
    }

    /**
     * Represent an expression with 2 arguments
     * 
     * @see ET#E2
     */
    public static abstract class E2Expr extends Value {

        public Value op1;
        public Value op2;

        public void setOp1(Value op1) {
            this.op1 = op1;
        }

        public void setOp2(Value op2) {
            this.op2 = op2;
        }

        public E2Expr(VT vt, Value op1, Value op2) {
            super(vt, ET.E2);
            this.op1 = op1;
            this.op2 = op2;
        }

        @Override
        public Value getOp1() {
            return op1;
        }

        @Override
        public Value getOp2() {
            return op2;
        }

        @Override
        protected void releaseMemory() {
            op1 = op2 = null;
        }
    }

    /**
     * Represent an expression with 3+ arguments
     * 
     * @see ET#En
     */
    public static abstract class EnExpr extends Value {

        public Value[] ops;

        public void setOps(Value[] ops) {
            this.ops = ops;
        }

        public EnExpr(VT vt, Value[] ops) {
            super(vt, ET.En);
            this.ops = ops;
        }

        protected Value[] cloneOps() {
            Value[] nOps = new Value[ops.length];
            for (int i = 0; i < nOps.length; i++) {
                nOps[i] = ops[i].trim().clone();
            }
            return nOps;
        }
        protected Value[] cloneOps(LabelAndLocalMapper mapper) {
            Value[] nOps = new Value[ops.length];
            for (int i = 0; i < nOps.length; i++) {
                nOps[i] = ops[i].clone(mapper);
            }
            return nOps;
        }

        @Override
        public Value[] getOps() {
            return ops;
        }

        @Override
        protected void releaseMemory() {
            ops = null;
        }
    }
    public static final int CAN_THROW = 1 << 3;
    public static final int MAY_THROW=1<<4;

    /**
     * Value Type
     */
    public static enum VT {

        ADD("+", MAY_THROW), AND("&", MAY_THROW), ARRAY(MAY_THROW), CAST(MAY_THROW), CHECK_CAST(CAN_THROW), CONSTANT(0), DCMPG(
                MAY_THROW), DCMPL(MAY_THROW), IDIV("/", CAN_THROW), LDIV("/", CAN_THROW), FDIV("/", MAY_THROW), DDIV("/", MAY_THROW), EQ("==", MAY_THROW), EXCEPTION_REF(0), FCMPG(
                MAY_THROW), FCMPL(MAY_THROW), FIELD(CAN_THROW), FILLED_ARRAY(CAN_THROW), GE(">=", MAY_THROW), GT(">",
                MAY_THROW), INSTANCE_OF(CAN_THROW), INVOKE_INTERFACE(CAN_THROW), //
        INVOKE_NEW(CAN_THROW), INVOKE_SPECIAL(CAN_THROW), INVOKE_STATIC(CAN_THROW), INVOKE_VIRTUAL(CAN_THROW), LCMP(
                MAY_THROW), //
        LE("<=", MAY_THROW), LENGTH(CAN_THROW), LOCAL(0), LT("<", MAY_THROW), MUL("*", MAY_THROW), NE("!=", MAY_THROW), NEG(
                MAY_THROW), //
        NEW(CAN_THROW), NEW_ARRAY(CAN_THROW), NEW_MUTI_ARRAY(CAN_THROW), NOT(MAY_THROW), OR("|", MAY_THROW), PARAMETER_REF(
                0), PHI(0), REM("%", MAY_THROW), SHL("<<", MAY_THROW), SHR(">>", MAY_THROW), STATIC_FIELD(CAN_THROW), SUB(
                "-", MAY_THROW), THIS_REF(MAY_THROW), USHR(">>>", MAY_THROW), XOR("^", MAY_THROW);
        private String name;
        private int flags;

        VT(int flags) {
            this(null, flags);
        }

        VT(String name, int flags) {
            this.name = name;
            this.flags = flags;
        }

        @Override
        public String toString() {
            return name == null ? super.toString() : name;
        }

        public boolean canThrow() {
            return CAN_THROW == flags;
        }

        public boolean mayThrow() {
            return MAY_THROW == flags;
        }
    }

    /**
     * The number of argument
     */
    final public ET et;

    private Value next;

    public String valueType;
    public Object tag;
    /**
     * Value Type
     */
    final public VT vt;

    /**
     * 
     * @param vt
     *            Value Type
     * @param et
     *            The number of argument
     */
    protected Value(VT vt, ET et) {
        super();
        this.vt = vt;
        this.et = et;
    }

    @Override
    public abstract Value clone();

    public abstract Value clone(LabelAndLocalMapper mapper);

    public Value getOp() {
        return null;
    }

    public Value getOp1() {
        return null;
    }

    public Value getOp2() {
        return null;
    }

    public Value[] getOps() {
        return null;
    }

    /**
     * clean resource used by this value,release memory
     */
    protected void releaseMemory() {
    }

    public final String toString() {
        return trim().toString0();
    }

    protected abstract String toString0();

    public Value trim() {
        Value a = this;
        while (a.next != null) {
            Value b = a.next;
            a.next = b;
            a = b;
        }
        return a;
    }
}
