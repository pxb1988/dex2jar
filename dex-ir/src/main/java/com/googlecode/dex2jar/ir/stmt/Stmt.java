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
package com.googlecode.dex2jar.ir.stmt;

import java.util.Set;
import java.util.TreeSet;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;

/**
 * Represent a statement
 * 
 * @see ST
 * @see ET
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public abstract class Stmt {

    /**
     * Represent a statement with no argument
     * 
     * @see ET#E0
     */
    public static abstract class E0Stmt extends Stmt {

        public E0Stmt(ST type) {
            super(type, ET.E0);
        }
    }

    /**
     * Represent a statement with 1 argument
     * 
     * @see ET#E1
     */
    public static abstract class E1Stmt extends Stmt {

        public Value op;

        public E1Stmt(ST type, Value op) {
            super(type, ET.E1);
            this.op = op;
        }

        @Override
        public Value getOp() {
            return op;
        }

        public void setOp(Value op) {
            this.op = op;
        }

    }

    /**
     * Represent a statement with 2 arguments
     * 
     * @see ET#E2
     */
    public static abstract class E2Stmt extends Stmt {

        public Value op1;
        public Value op2;

        public E2Stmt(ST type, Value op1, Value op2) {
            super(type, ET.E2);
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

        public void setOp1(Value op1) {
            this.op1 = op1;
        }

        public void setOp2(Value op2) {
            this.op2 = op2;
        }

    }

    public static final int CAN_CONTINUE = 1 << 0;
    public static final int CAN_BRNANCH = 1 << 1;
    public static final int CAN_SWITCH = 1 << 2;
    public static final int CAN_THROW = 1 << 3;
    public static final int MAY_THROW=1<<4;

    /**
     * Statement Type
     * 
     */
    public static enum ST {

        LOCAL_START(CAN_CONTINUE), // same as ASSIGN but left must keep and must be local
        LOCAL_END(CAN_CONTINUE), // must keep and op must be local
        ASSIGN(CAN_CONTINUE | MAY_THROW), IDENTITY(CAN_CONTINUE), LABEL(CAN_CONTINUE), LOCK(CAN_CONTINUE | CAN_THROW), NOP(
                CAN_CONTINUE), UNLOCK(CAN_CONTINUE | CAN_THROW), VOID_INVOKE(CAN_CONTINUE | CAN_THROW), FILL_ARRAY_DATA(
                CAN_CONTINUE | CAN_THROW), //
        RETURN(MAY_THROW), RETURN_VOID(0), THROW(CAN_THROW), //
        GOTO(CAN_BRNANCH), IF(CAN_CONTINUE | CAN_BRNANCH | MAY_THROW), //
        LOOKUP_SWITCH(CAN_SWITCH | MAY_THROW), TABLE_SWITCH(CAN_SWITCH | MAY_THROW), ;
        private int config;

        ST(int config) {
            this.config = config;
        }

        public boolean canBranch() {
            return 0 != (CAN_BRNANCH & config);
        }

        public boolean canContinue() {
            return 0 != (CAN_CONTINUE & config);
        }

        public boolean canSwitch() {
            return 0 != (CAN_SWITCH & config);
        }

        public boolean mayThrow() {
            return 0 != (MAY_THROW & config);
        }

        public boolean canThrow() {
            return 0 != (CAN_THROW & config);
        }
    }

    /**
     * Used in construct of a method CFG, Previous {@link Stmt} nodes
     */
    public Set<Stmt> _cfg_froms;

    /**
     * Used in construct of a method CFG, After {@link Stmt} nodes
     */
    public Set<LabelStmt> exceptionHandlers;

    /**
     * Used in visit the method CFG
     */
    public boolean visited;

    /**
     * Used in Local Split, forward frame of the {@link Stmt}
     */
    public Object frame;

    public Stmt _ts_default_next;

    /**
     * The number of argument
     */
    public final ET et;
    /**
     * Used in ordering statements in a {@link TreeSet}, id of the {@link Stmt} in its {@link StmtList}
     */
    public int id;

    /**
     * Owner of the statement
     */
    /* default */
    StmtList list;

    /**
     * Next statement in {@link StmtList}
     */
    /* default */
    Stmt next;

    /**
     * Previous statement in {@link StmtList}
     */
    /* default */
    Stmt pre;
    /**
     * Statement Type
     */
    public final ST st;

    /**
     * 
     * @param st
     *            Statement Type
     * @param et
     *            The number of argument
     */
    protected Stmt(ST st, ET et) {
        this.st = st;
        this.et = et;
    }

    public abstract Stmt clone(LabelAndLocalMapper mapper);

    /**
     * 
     * @return Next statement in {@link StmtList}, null if it is the last statement in {@link StmtList}
     */
    public final Stmt getNext() {
        return next;
    }

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
     * 
     * @return Previous statement in {@link StmtList}, null if it is the first statement in {@link StmtList}
     */
    public final Stmt getPre() {
        return pre;
    }

    public void setOp(Value op) {
    }

    public void setOp1(Value op) {
    }

    public void setOp2(Value op) {
    }

    public void setOps(Value[] op) {
    }
}
