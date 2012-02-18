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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.ValueBox;

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

        public ValueBox op;

        public E1Stmt(ST type, ValueBox op) {
            super(type, ET.E1);
            this.op = op;
        }
    }

    /**
     * Represent a statement with 2 arguments
     * 
     * @see ET#E2
     */
    public static abstract class E2Stmt extends Stmt {

        public ValueBox op1;
        public ValueBox op2;

        public E2Stmt(ST type, ValueBox op1, ValueBox op2) {
            super(type, ET.E2);
            this.op1 = op1;
            this.op2 = op2;
        }
    }

    /**
     * Represent a statement with 3+ arguments
     * 
     * @see ET#En
     */
    public static abstract class EnStmt extends Stmt {

        public ValueBox[] ops;

        public EnStmt(ST type, ValueBox[] ops) {
            super(type, ET.E1);
            this.ops = ops;
        }
    }

    /**
     * Statement Type
     * 
     */
    public static enum ST {

        ASSIGN, GOTO, IDENTITY, IF, LABEL, LOCK, LOOKUP_SWITCH, //
        NOP, RETURN, RETURN_VOID, TABLE_SWITCH, THROW, UNLOCK, 
        LINENUMBER, LOCALVARIABLE
    }

    /**
     * Used in construct of a method CFG, Previous {@link Stmt} nodes
     */
    public Set<Stmt> _cfg_froms;

    /**
     * Used in construct of a method CFG, After {@link Stmt} nodes
     */
    public Set<Stmt> _cfg_tos;

    /**
     * Used in visit the method CFG
     */
    public boolean _cfg_visited;

    /**
     * Used in Local Split, forward frame of the {@link Stmt}
     */
    public Object _ls_forward_frame;

    public Stmt _ts_default_next;
    public List<Stmt> _ts_tos;

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

    public abstract Stmt clone(Map<LabelStmt, LabelStmt> map);

    protected LabelStmt cloneLabel(Map<LabelStmt, LabelStmt> map, LabelStmt label) {
        LabelStmt nTarget = map.get(label);
        if (nTarget == null) {
            nTarget = Stmts.nLabel();
            map.put(label, nTarget);
        }
        return nTarget;
    }

    /**
     * 
     * @return Next statement in {@link StmtList}, null if it is the last statement in {@link StmtList}
     */
    public final Stmt getNext() {
        return next;
    }

    /**
     * 
     * @return Previous statement in {@link StmtList}, null if it is the first statement in {@link StmtList}
     */
    public final Stmt getPre() {
        return pre;
    }
}
