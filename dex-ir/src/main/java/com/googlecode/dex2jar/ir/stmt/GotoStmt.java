/*
 * dex2jar - Tools to work with android .dex and java .class files
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
import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

/**
 * Represent a GOTO statement
 * 
 * @see ST#GOTO
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
public class GotoStmt extends E0Stmt implements JumpStmt {
    public LabelStmt target;

    public LabelStmt getTarget() {
        return target;
    }

    public void setTarget(LabelStmt target) {
        this.target = target;
    }

    public GotoStmt(LabelStmt target) {
        super(ST.GOTO);
        this.target = target;
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        LabelStmt nTarget = mapper.map(target);
        return new GotoStmt(nTarget);
    }

    @Override
    public String toString() {
        return "GOTO " + target.getDisplayName();
    }
}
