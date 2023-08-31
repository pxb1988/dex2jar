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

import com.googlecode.d2j.CallSite;
import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class InvokeCustomExpr extends AbstractInvokeExpr {
    public CallSite callSite;

    @Override
    protected void releaseMemory() {
        callSite = null;
        super.releaseMemory();
    }

    @Override
    public Proto getProto() {
        return callSite.getMethodProto();
    }

    public InvokeCustomExpr(VT type, Value[] args, CallSite callSite) {
        super(type, args);
        this.callSite = callSite;
    }

    @Override
    public Value clone() {
        return new InvokeCustomExpr(vt, cloneOps(), callSite);
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return new InvokeCustomExpr(vt, cloneOps(mapper), callSite);
    }

    @Override
    public String toString0() {
        StringBuilder sb = new StringBuilder();

        sb.append("InvokeCustomExpr(....)");
        return sb.toString();
    }
}
