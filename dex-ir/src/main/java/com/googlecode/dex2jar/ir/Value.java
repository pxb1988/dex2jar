package com.googlecode.dex2jar.ir;

public abstract class Value {
    public static enum VT {
        ADD, AND, ARRAY, CAST, CMP, CMPG, CMPL, DIV, EQ, FIELD, GE, GT, INSTANCEOF, INVOKE_INTERFACE, LE, LENGTH, LT, MUL, NE, NEG, NEW_ARRAY, NEW_MUTI_ARRAY, OR, REM, SHL, SHR, INVOKE_SPECIAL, //
        INVOKE_STATIC, SUB, USHR, INVOKE_VIRTUAL, INVOKE_NEW, XOR, LOCAL, CONSTANT, THIS_REF, PARAMETER_REF, EXCEPTION_REF, NOT
    }

    final public VT vt;
    final public ET et;

    /**
     * @param vt
     */
    public Value(VT vt, ET et) {
        super();
        this.vt = vt;
        this.et = et;
    }

    public static abstract class E0Expr extends Value {
        public E0Expr(VT vt) {
            super(vt, ET.E0);
        }

    }

    public static abstract class E1Expr extends Value {
        public ValueBox op;

        public E1Expr(VT vt, ValueBox op) {
            super(vt, ET.E1);
            this.op = op;
        }

    }

    public static abstract class E2Expr extends Value {
        public ValueBox op1;
        public ValueBox op2;

        public E2Expr(VT vt, ValueBox op1, ValueBox op2) {
            super(vt, ET.E2);
            this.op1 = op1;
            this.op2 = op2;
        }

    }

    public static abstract class EnExpr extends Value {
        public ValueBox[] ops;

        public EnExpr(VT vt, ValueBox[] ops) {
            super(vt, ET.En);
            this.ops = ops;
        }
    }

}
