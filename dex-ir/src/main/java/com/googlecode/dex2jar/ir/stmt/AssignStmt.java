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
package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;

/**
 * Represent an Assign statement
 * 
 * @see ST#ASSIGN
 * @see ST#IDENTITY
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class AssignStmt extends E2Stmt {

    public AssignStmt(ST type, ValueBox left, ValueBox right) {
        super(type, left, right);
    }

    public String toString() {
        switch (st) {
        case ASSIGN:
            return op1 + " = " + op2;
        case IDENTITY:
            return op1 + " := " + op2;
        }
        return super.toString();
    }

}
