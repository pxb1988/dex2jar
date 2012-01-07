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

import java.util.Map;

import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

/**
 * Represent a Jump statement, the op is null if it is a GOTO statement
 * 
 * @see ST#GOTO
 * @see ST#IF
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class JumpStmt extends E1Stmt {

    public LabelStmt target;

    /**
     * GOTO
     * 
     * @param type
     * @param target
     */
    public JumpStmt(ST type, LabelStmt target) {
        this(type, null, target);
    }

    /**
     * IF
     * 
     * @param type
     * @param condition
     * @param target
     */
    public JumpStmt(ST type, ValueBox condition, LabelStmt target) {
        super(type, condition);
        this.target = target;
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        LabelStmt nTarget = cloneLabel(map, target);
        if (op != null) {
            return new JumpStmt(st, new ValueBox(op.value.clone()), target);
        } else {
            return new JumpStmt(st, nTarget);
        }
    }

    public String toString() {
        switch (st) {
        case GOTO:
            return "GOTO " + target.getDisplayName();
        case IF:
            return "if " + op + " GOTO " + target.getDisplayName();
        }
        return super.toString();
    }
}
