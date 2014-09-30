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
import com.googlecode.dex2jar.ir.expr.Value.E1Expr;

/**
 * Represent a non-static Field expression.
 * 
 * @see VT#FIELD
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 */
public class FieldExpr extends E1Expr {

    /**
     * Field name
     */
    public String name;
    /**
     * Field owner type descriptor
     */
    public String owner;
    /**
     * Field type descriptor
     */
    public String type;

    public FieldExpr(Value object, String ownerType, String fieldName, String fieldType) {
        super(VT.FIELD, object);
        this.type = fieldType;
        this.name = fieldName;
        this.owner = ownerType;
    }

    @Override
    protected void releaseMemory() {
        name = null;
        owner = type = null;
        super.releaseMemory();
    }

    @Override
    public Value clone() {
        return new FieldExpr(op.trim().clone(), owner, name, type);
    }
    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new FieldExpr(op.clone(mapper), owner, name, type);
    }

    @Override
    public String toString0() {
        return op + "." + name;
    }

}
