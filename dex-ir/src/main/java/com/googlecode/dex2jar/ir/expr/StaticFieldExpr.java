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
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.E0Expr;

/**
 * Represent a StaticField expression
 * 
 * @see VT#STATIC_FIELD
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 */
public class StaticFieldExpr extends E0Expr {

    /**
     * Field name
     */
    public String name;
    /**
     * Field owner type
     */
    public String owner;
    /**
     * Field type
     */
    public String type;

    @Override
    protected void releaseMemory() {
        name = null;
        owner = type = null;
        super.releaseMemory();
    }

    public StaticFieldExpr(String ownerType, String fieldName, String fieldType) {
        super(VT.STATIC_FIELD);
        this.type = fieldType;
        this.name = fieldName;
        this.owner = ownerType;
    }

    @Override
    public Value clone() {
        return new StaticFieldExpr(owner, name, type);
    }
    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new StaticFieldExpr(owner, name, type);
    }

    @Override
    public String toString0() {
        return Util.toShortClassName(owner) + "." + name;
    }

}
