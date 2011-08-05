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

import com.googlecode.dex2jar.ir.ToStringUtil;
import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E1Expr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;

/**
 * Represent a Field expression. represent a static file if op is null
 * 
 * @see VT#FIELD
 * 
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class FieldExpr extends E1Expr {

    /**
     * Field name
     */
    public String fieldName;
    /**
     * Field owner type
     */
    public Type fieldOwnerType;
    /**
     * Field type
     */
    public Type fieldType;

    public FieldExpr(ValueBox object, Type ownerType, String fieldName, Type fieldType) {
        super(VT.FIELD, object);
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.fieldOwnerType = ownerType;
    }

    @Override
    public Value clone() {
        return new FieldExpr(op == null ? null : new ValueBox(op.value.clone()), fieldOwnerType, fieldName, fieldType);
    }

    public String toString() {
        return (op == null ? ToStringUtil.toShortClassName(fieldOwnerType) : op) + "." + fieldName;
    }

}
