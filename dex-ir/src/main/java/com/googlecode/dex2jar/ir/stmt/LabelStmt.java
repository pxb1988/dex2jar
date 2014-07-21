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

import java.util.ArrayList;
import java.util.List;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

/**
 * Represent a Label statement
 * 
 * @see ST#LABEL
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class LabelStmt extends E0Stmt {

    public String displayName;
    public int lineNumber = -1;
    public List<AssignStmt> phis;
    public Object tag;

    public LabelStmt() {
        super(ST.LABEL);
    }

    @Override
    public LabelStmt clone(LabelAndLocalMapper mapper) {
        LabelStmt labelStmt = mapper.map(this);
        if (phis != null && labelStmt.phis == null) {
            labelStmt.phis = new ArrayList<>(phis.size());
            for (AssignStmt phi : phis) {
                labelStmt.phis.add((AssignStmt) phi.clone(mapper));
            }
        }
        return labelStmt;
    }

    public String getDisplayName() {
        if (displayName != null) {
            return displayName;
        }
        int x = hashCode();
        return String.format("L%08x", x);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDisplayName()).append(":");

        if (phis != null && phis.size() > 0) {
            sb.append(" // ").append(phis);
        }
        if (lineNumber >= 0) {
            sb.append(" // line ").append(lineNumber);
        }
        return sb.toString();
    }

}
