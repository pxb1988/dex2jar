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
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

public class UnopStmt extends E1Stmt {

    public UnopStmt(ST type, Value op) {
        super(type, op);
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        return new UnopStmt(st, op.clone(mapper));
    }

    @Override
    public String toString() {
        switch (super.st) {
        case LOCK:
            return "lock " + op;
        case UNLOCK:
            return "unlock " + op;
        case THROW:
            return "throw " + op;
        case RETURN:
            return "return " + op;
        case LOCAL_END:
            return op + " ::END";
        default:
        }
        return super.toString();
    }
}
