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
package com.googlecode.dex2jar.ir;


/**
 * Represent a local/constant/expression
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public abstract class Value {

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

        public ValueBox op;

        public E1Expr(VT vt, ValueBox op) {
            super(vt, ET.E1);
            this.op = op;
        }
    }

    /**
     * Represent an expression with 2 arguments
     * 
     * @see ET#E2
     */
    public static abstract class E2Expr extends Value {

        public ValueBox op1;
        public ValueBox op2;

        public E2Expr(VT vt, ValueBox op1, ValueBox op2) {
            super(vt, ET.E2);
            this.op1 = op1;
            this.op2 = op2;
        }
    }

    /**
     * Represent an expression with 3+ arguments
     * 
     * @see ET#En
     */
    public static abstract class EnExpr extends Value {

        public ValueBox[] ops;

        public EnExpr(VT vt, ValueBox[] ops) {
            super(vt, ET.En);
            this.ops = ops;
        }
    }

    /**
     * Value Type
     */
    public static enum VT {

        ADD("+"), AND("&"), ARRAY, CAST, CMP, CMPG, CMPL, CONSTANT, DIV("/"), EQ("=="), EXCEPTION_REF, FIELD, GE(">="), GT(
                ">"), INSTANCEOF, //
        INVOKE_INTERFACE, INVOKE_NEW, INVOKE_SPECIAL, INVOKE_STATIC, INVOKE_VIRTUAL, //
        LE("<="), LENGTH, LOCAL, LT("<"), MUL("*"), NE("!="), NEG, //
        NEW_ARRAY, NEW_MUTI_ARRAY, NOT, OR("|"), PARAMETER_REF, REM("%"), SHL(">>"), SHR("<<"), SUB("-"), THIS_REF, USHR(
                ">>>"), XOR("^");
        private String name;

        VT() {
            this(null);
        }

        VT(String name) {
            this.name = name;
        }

        public String toString() {
            return name == null ? super.toString() : name;
        }
    }

    /**
     * The number of argument
     */
    final public ET et;
    /**
     * Value Type
     */
    final public VT vt;

    public Object _lt_type;

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
}
