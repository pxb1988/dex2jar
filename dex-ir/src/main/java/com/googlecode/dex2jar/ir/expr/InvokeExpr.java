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
 * Represent a method invocation expression. To represent a {@link VT#INVOKE_INTERFACE},{@link VT#INVOKE_SPECIAL} or
 * {@link VT#INVOKE_VIRTUAL} the first element of ops is the owner object,To represent a {@link VT#INVOKE_NEW} or
 * {@link VT#INVOKE_STATIC} all ops are arguments. The return type of {@link VT#INVOKE_NEW} is {@link #owner} instead of
 * {@link #ret}
 * 
 * @see VT#INVOKE_INTERFACE
 * @see VT#INVOKE_NEW
 * @see VT#INVOKE_SPECIAL
 * @see VT#INVOKE_STATIC
 * @see VT#INVOKE_VIRTUAL
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 */
public class InvokeExpr extends EnExpr {

    /**
     * argument type desc
     */
    public String[] args;
    public String name;
    /**
     * owner type desc
     */
    public String owner;
    /**
     * owner type desc
     */
    public String ret;

    @Override
    protected void releaseMemory() {
        args = null;
        ret = null;
        owner = null;
        name = null;
        super.releaseMemory();
    }

    public InvokeExpr(VT type, Value[] args, String ownerType, String methodName, String[] argmentTypes,
            String returnType) {
        super(type, args);
        this.ret = returnType;
        this.name = methodName;
        this.owner = ownerType;
        this.args = argmentTypes;
    }

    @Override
    public Value clone() {
        return new InvokeExpr(vt, cloneOps(), owner, name, args, ret);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new InvokeExpr(vt, cloneOps(mapper), owner, name, args, ret);
    }

    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder();

        if (super.vt == VT.INVOKE_NEW) {
            sb.append("new ").append(Util.toShortClassName(owner)).append('(');
        } else {
            sb.append(super.vt == VT.INVOKE_STATIC ? Util.toShortClassName(owner) : ops[0]).append('.')
                    .append(this.name).append('(');
        }
        boolean first = true;
        for (int i = (vt == VT.INVOKE_STATIC || vt == VT.INVOKE_NEW) ? 0 : 1; i < ops.length; i++) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(ops[i]);
        }
        sb.append(')');
        return sb.toString();
    }
}
