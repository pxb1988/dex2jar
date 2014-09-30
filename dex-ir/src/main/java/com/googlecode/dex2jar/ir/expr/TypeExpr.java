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
 * Represent a Type expression
 * 
 * @see VT#CHECK_CAST
 * @see VT#INSTANCE_OF
 * @see VT#NEW_ARRAY
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class TypeExpr extends E1Expr {

    public String type;

    @Override
    protected void releaseMemory() {
        type = null;
        super.releaseMemory();
    }

    public TypeExpr(VT vt, Value value, String desc) {
        super(vt, value);
        this.type = desc;

    }

    @Override
    public Value clone() {
        return new TypeExpr(vt, op.trim().clone(), type);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new TypeExpr(vt, op.clone(mapper), type);
    }


    @Override
    public String toString0() {
        switch (super.vt) {
        case CHECK_CAST:
            return "((" + Util.toShortClassName(type) + ")" + op + ")";
        case INSTANCE_OF:
            return "(" + op + " instanceof " + Util.toShortClassName(type) + ")";
        case NEW_ARRAY:
            if (type.charAt(0) == '[') {
                int dimension = 1;
                while (type.charAt(dimension) == '[') {
                    dimension++;
                }
                StringBuilder sb = new StringBuilder("new ")
                        .append(Util.toShortClassName(type.substring(dimension))).append("[").append(op)
                        .append("]");
                for (int i = 0; i < dimension; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            }
            return "new " + Util.toShortClassName(type) + "[" + op + "]";
        default:
        }
        return "UNKNOW";
    }
}
