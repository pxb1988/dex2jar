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
package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;

/**
 * Represent a FILLED_ARRAY expression.
 *
 * @see VT#FILLED_ARRAY
 */
public class FilledArrayExpr extends EnExpr {

    public String type;
    @Override
    protected void releaseMemory() {
        type = null;
        super.releaseMemory();
    }
    public FilledArrayExpr(Value[] datas, String type) {
        super(VT.FILLED_ARRAY, datas);
        this.type = type;
    }

    @Override
    public Value clone() {
        return new FilledArrayExpr(cloneOps(), type);
    }
    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new FilledArrayExpr(cloneOps(mapper), type);
    }

    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder().append("new ").append(Util.toShortClassName(type)).append("[]{");
        for (int i = 0; i < ops.length; i++) {
            sb.append(ops[i]).append(", ");
        }
        if (ops.length > 0) {
            sb.setLength(sb.length() - 2); // remove tail ", "
        }
        sb.append('}');
        return sb.toString();
    }
}
