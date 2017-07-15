/*
 * Copyright (c) 2009-2017 Panxiaobo
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

public class InvokePolymorphicExpr extends AbstractInvokeExpr {
    public Proto proto;
    public Method method;

    @Override
    protected void releaseMemory() {
        method = null;
        proto = null;
        super.releaseMemory();
    }

    @Override
    public Proto getProto() {
        return proto;
    }

    public InvokePolymorphicExpr(VT type, Value[] args, Proto proto, Method method) {
        super(type, args);
        this.proto = proto;
        this.method = method;
    }

    @Override
    public Value clone() {
        return new InvokePolymorphicExpr(vt, cloneOps(), proto, method);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new InvokePolymorphicExpr(vt, cloneOps(mapper), proto, method);
    }

    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder();

        sb.append("InvokePolymorphicExpr(...)");
        return sb.toString();
    }
}
