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
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ToStringUtil;
import com.googlecode.dex2jar.ir.ValueBox;

/**
 * Represent a method invocation expression. To represent a {@link VT#INVOKE_INTERFACE},{@link VT#INVOKE_SPECIAL} or
 * {@link VT#INVOKE_VIRTUAL} the first element of ops is the owner object,To represent a {@link VT#INVOKE_NEW} or
 * {@link VT#INVOKE_STATIC} all ops are arguments. The return type of {@link VT#INVOKE_NEW} is {@link #methodOwnerType}
 * instead of {@link #methodReturnType}
 * 
 * @see VT#INVOKE_INTERFACE
 * @see VT#INVOKE_NEW
 * @see VT#INVOKE_SPECIAL
 * @see VT#INVOKE_STATIC
 * @see VT#INVOKE_VIRTUAL
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class InvokeExpr extends EnExpr {

    public Type[] argmentTypes;
    public String methodName;
    public Type methodOwnerType;
    public Type methodReturnType;

    public InvokeExpr(VT type, ValueBox[] args, Type ownerType, String methodName, Type[] argmentTypes, Type returnType) {
        super(type, args);
        this.methodReturnType = returnType;
        this.methodName = methodName;
        this.methodOwnerType = ownerType;
        this.argmentTypes = argmentTypes;
    }

    @Override
    public Value clone() {
        ValueBox[] nOps = new ValueBox[ops.length];
        for (int i = 0; i < nOps.length; i++) {
            nOps[i] = new ValueBox(ops[i].value.clone());
        }
        return new InvokeExpr(vt, nOps, methodOwnerType, methodName, argmentTypes, methodReturnType);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (super.vt == VT.INVOKE_NEW) {
            sb.append("new ").append(ToStringUtil.toShortClassName(methodOwnerType)).append('(');
        } else {
            sb.append(super.vt == VT.INVOKE_STATIC ? ToStringUtil.toShortClassName(methodOwnerType) : ops[0])
                    .append('.').append(this.methodName).append('(');
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
