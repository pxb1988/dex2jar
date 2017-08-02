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

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import com.googlecode.dex2jar.ir.expr.Value.EnExpr;

/**
 * Represent a method invocation expression. To represent a {@link VT#INVOKE_INTERFACE},{@link VT#INVOKE_SPECIAL} or
 * {@link VT#INVOKE_VIRTUAL} the first element of ops is the owner object,To represent a {@link VT#INVOKE_NEW} or
 * {@link VT#INVOKE_STATIC} all ops are arguments. The return type of {@link VT#INVOKE_NEW} is owner instead of ret
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 * @see VT#INVOKE_INTERFACE
 * @see VT#INVOKE_NEW
 * @see VT#INVOKE_SPECIAL
 * @see VT#INVOKE_STATIC
 * @see VT#INVOKE_VIRTUAL
 */
public class InvokeExpr extends AbstractInvokeExpr {

    public Method method;

    @Override
    protected void releaseMemory() {
        method = null;
        super.releaseMemory();
    }

    @Override
    public Proto getProto() {
        return method.getProto();
    }

    public InvokeExpr(VT type, Value[] args, String ownerType, String methodName, String[] argmentTypes,
                      String returnType) {
        super(type, args);
        this.method = new Method(ownerType, methodName, argmentTypes, returnType);
    }

    public InvokeExpr(VT type, Value[] args, Method method) {
        super(type, args);
        this.method = method;
    }

    @Override
    public Value clone() {
        return new InvokeExpr(vt, cloneOps(), method);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new InvokeExpr(vt, cloneOps(mapper), method);
    }

    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        if (super.vt == VT.INVOKE_NEW) {
            sb.append("new ").append(Util.toShortClassName(method.getOwner()));
        } else if (super.vt == VT.INVOKE_STATIC) {
            sb.append(Util.toShortClassName(method.getOwner())).append('.')
                    .append(this.method.getName());
        } else {
            sb.append(ops[i++]).append('.').append(this.method.getName());
        }
        sb.append('(');
        boolean first = true;
        for (; i < ops.length; i++) {
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

    public String getOwner() {
        return method.getOwner();
    }

    public String getRet() {
        return method.getReturnType();
    }

    public String getName() {
        return method.getName();
    }

    public String[] getArgs() {
        return method.getParameterTypes();
    }

}
