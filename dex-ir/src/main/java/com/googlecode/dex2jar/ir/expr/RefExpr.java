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

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E0Expr;
import com.googlecode.dex2jar.ir.Value.VT;

/**
 * Represent a Reference expression
 * 
 * @see VT#THIS_REF
 * @see VT#PARAMETER_REF
 * @see VT#EXCEPTION_REF
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class RefExpr extends E0Expr {

    public int parameterIndex;

    public Type type;

    public RefExpr(VT vt, Type refType, int index) {
        super(vt);
        this.type = refType;
        this.parameterIndex = index;
    }

    @Override
    public Value clone() {
        return new RefExpr(vt, type, parameterIndex);
    }

    @Override
    public String toString() {
        switch (vt) {
        case THIS_REF:
            return "@this";
        case PARAMETER_REF:
            return "@parameter_" + parameterIndex;
        case EXCEPTION_REF:
            return "@Exception";
        }
        return super.toString();
    }

}
