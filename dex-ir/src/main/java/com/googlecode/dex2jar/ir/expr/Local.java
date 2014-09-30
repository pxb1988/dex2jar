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
import com.googlecode.dex2jar.ir.expr.Value.E0Expr;

/**
 * TODO DOC
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Local extends E0Expr {
    public int _ls_index;
    public String signature;
    public String debugName;

    public Local(String debugName) {
        super(Value.VT.LOCAL);
        this.debugName = debugName;
    }

    public Local(int index, String debugName) {
        super(Value.VT.LOCAL);
        this.debugName = debugName;
        this._ls_index = index;
    }

    public Local() {
        super(Value.VT.LOCAL);
    }

    public Local(int index) {
        super(Value.VT.LOCAL);
        this._ls_index = index;
    }

    @Override
    public Value clone() {
        Local clone = new Local(_ls_index);
        clone.debugName = debugName;
        clone.signature = this.signature;
        clone.valueType = this.valueType;
        return clone;
    }

    @Override
    public Value clone(LabelAndLocalMapper mapper) {
        return mapper.map(this);
    }

    @Override
    public String toString0() {
        if (debugName == null) {
            return "a" + _ls_index;
        } else {
            return debugName + "_" + _ls_index;
        }
    }
}
