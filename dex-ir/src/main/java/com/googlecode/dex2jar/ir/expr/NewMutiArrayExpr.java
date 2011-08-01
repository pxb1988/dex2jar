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
package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;

/**
 * Represent a NEW_MUTI_ARRAY expression.
 * 
 * @see VT#NEW_MUTI_ARRAY
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class NewMutiArrayExpr extends EnExpr {

    public Type baseType;
    public int dimension;

    public NewMutiArrayExpr(Type base, int dimension, ValueBox[] sizes) {
        super(VT.NEW_MUTI_ARRAY, sizes);
        this.baseType = base;
        this.dimension = dimension;
        this.ops = new ValueBox[sizes.length];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(baseType);
        for (int i = 0; i < dimension; i++) {
            sb.append('[').append(ops[i]).append(']');
        }
        return sb.toString();
    }

}
