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
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.ValueBox;

/**
 * Represent a LENGTH,NEG expression
 * 
 * @see VT#LENGTH
 * @see VT#NEG
 * @see VT#NOT
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class UnopExpr extends E1Expr {
    public Type type;

    /**
     * @param vt
     * @param value
     * @param type
     */
    public UnopExpr(VT vt, Value value, Type type) {
        super(vt, new ValueBox(value));
        this.type = type;
    }

    @Override
    public Value clone() {
        return new UnopExpr(vt, op.value.clone(), type);
    }

    @Override
    public String toString() {
        switch (vt) {
        case LENGTH:
            return op + ".length";
        case NEG:
            return "(-" + op + ")";
        case NOT:
            return "(!" + op + ")";
        }
        return super.toString();
    }

}
