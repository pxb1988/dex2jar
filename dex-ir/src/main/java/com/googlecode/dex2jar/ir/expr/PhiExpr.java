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
package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;

public class PhiExpr extends EnExpr {

    public PhiExpr(Value[] ops) {
        super(VT.PHI, ops);
    }

    @Override
    public Value clone() {
        return new PhiExpr(cloneOps());
    }
    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new PhiExpr(cloneOps(mapper));
    }
    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder("φ(");
        boolean first = true;
        for (Value vb : ops) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(vb);
        }
        sb.append(")");
        return sb.toString();
    }

}
