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
import com.googlecode.dex2jar.ir.expr.Value.E1Expr;

/**
 * * @see VT#CAST
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class CastExpr extends E1Expr {
    public String from;
    public String to;

    public CastExpr(Value value, String from, String to) {
        super(VT.CAST, value);
        this.from = from;
        this.to = to;
    }

    @Override
    protected void releaseMemory() {
        from = to = null;
        super.releaseMemory();
    }

    @Override
    public Value clone() {
        return new CastExpr(op.trim().clone(), from, to);
    }
    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new CastExpr(op.clone(mapper), from, to);
    }
    @Override
    public String toString0() {
        return "((" + Util.toShortClassName(to) + ")" + op + ")";
    }
}
