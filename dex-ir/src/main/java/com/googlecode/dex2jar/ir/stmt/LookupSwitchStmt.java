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
 * Represent a LOOKUP_SWITCH statement
 * 
 * @see ST#LOOKUP_SWITCH
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class LookupSwitchStmt extends E1Stmt {

    public LabelStmt defaultTarget;
    public int[] lookupValues;
    public LabelStmt[] targets;

    public LookupSwitchStmt(ValueBox key, int[] lookupValues, LabelStmt[] targets, LabelStmt defaultTarget) {
        super(ST.LOOKUP_SWITCH, key);
        this.lookupValues = lookupValues;
        this.targets = targets;
        this.defaultTarget = defaultTarget;
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        LabelStmt[] nTargets = new LabelStmt[targets.length];
        for (int i = 0; i < nTargets.length; i++) {
            nTargets[i] = cloneLabel(map, targets[i]);
        }
        int nLookupValues[] = new int[lookupValues.length];
        System.arraycopy(lookupValues, 0, nLookupValues, 0, nLookupValues.length);

        return new LookupSwitchStmt(new ValueBox(op.value.clone()), nLookupValues, nTargets, cloneLabel(map,
                defaultTarget));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("switch(").append(op).append(") {");

        for (int i = 0; i < lookupValues.length; i++) {
            sb.append("\n case ").append(lookupValues[i]).append(": GOTO ").append(targets[i].getDisplayName())
                    .append(";");
        }
        sb.append("\n default : GOTO ").append(defaultTarget.getDisplayName()).append(";");
        sb.append("\n}");
        return sb.toString();
    }
}
