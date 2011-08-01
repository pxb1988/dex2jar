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

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

/**
 * Represent a TABLE_SWITCH statement
 * 
 * @see ST#TABLE_SWITCH
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class TableSwitchStmt extends E1Stmt {

    public LabelStmt defaultTarget;
    public int lowIndex, highIndex;
    public LabelStmt[] targets;

    public TableSwitchStmt() {
        super(ST.TABLE_SWITCH, null);
    }

    public TableSwitchStmt(Value key, int lowIndex, int highIndex, LabelStmt[] targets, LabelStmt defaultTarget) {
        super(ST.TABLE_SWITCH, new ValueBox(key));
        this.lowIndex = lowIndex;
        this.highIndex = highIndex;
        this.targets = targets;
        this.defaultTarget = defaultTarget;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("switch(").append(op).append(") {");

        for (int i = 0; i < targets.length; i++) {
            sb.append("\n case ").append(lowIndex + i).append(": GOTO ").append(targets[i].getDisplayName())
                    .append(";");
        }
        sb.append("\n default : GOTO ").append(defaultTarget.getDisplayName()).append(";");
        sb.append("\n}");
        return sb.toString();
    }

}
