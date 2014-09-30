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

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;

/**
 * Represent an Assign statement
 * 
 * @see ST#ASSIGN
 * @see ST#IDENTITY
 * @see ST#FILL_ARRAY_DATA
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 8da5a5faa6bd $
 */
public class AssignStmt extends E2Stmt {

    public AssignStmt(ST type, Value left, Value right) {
        super(type, left, right);
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        return new AssignStmt(st, op1.clone(mapper), op2.clone(mapper));
    }

    @Override
    public String toString() {
        switch (st) {
        case ASSIGN:
            return op1 + " = " + op2;
        case LOCAL_START:
        case IDENTITY:
            return op1 + " := " + op2;
        case FILL_ARRAY_DATA:
            return op1 + " <- " + op2;
        default:
        }
        return super.toString();
    }

}
