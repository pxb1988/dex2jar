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
import com.googlecode.dex2jar.ir.expr.Value.E2Expr;

/**
 * Represent an Array expression
 * 
 * @see VT#ARRAY
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class ArrayExpr extends E2Expr {

    public ArrayExpr() {
        super(VT.ARRAY, null, null);
    }

    public String elementType;

    public ArrayExpr(Value base, Value index, String elementType) {
        super(VT.ARRAY, base, index);
        this.elementType = elementType;
    }

    @Override
    public Value clone() {
        return new ArrayExpr(op1.trim().clone(), op2.trim().clone(), this.elementType);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new ArrayExpr(op1.clone(mapper), op2.clone(mapper), this.elementType);
    }

    @Override
    public String toString0() {
        return op1 + "[" + op2 + "]";
    }
}
