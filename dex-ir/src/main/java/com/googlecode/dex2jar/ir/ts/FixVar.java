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
package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LocalVar;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.expr.Value.VT;
import com.googlecode.dex2jar.ir.stmt.Stmts;

/**
 * the {@link LocalVar#reg} in {@link LocalVar} may be replace to a constant value in {@link ConstTransformer}. This
 * class try to insert a new local before {@link LocalVar#start}.
 * 
 * <p>
 * before:
 * </p>
 * 
 * <pre>
 *   ...
 * L0:
 *   return a0
 * L1:
 * ======
 * .var L0 ~ L1 1 -> test // int
 * </pre>
 * <p>
 * after:
 * </p>
 * 
 * <pre>
 *   ...
 *   d1 = 1
 * L0:
 *   return a0
 * L1:
 * ======
 * .var L0 ~ L1 d1 -> test // int
 * </pre>
 * 
 * @author Panxiaobo
 * 
 */
public class FixVar implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        int i = 0;
        for (LocalVar var : irMethod.vars) {
            if (var.reg.trim().vt != VT.LOCAL) {
                if (var.reg.trim().vt == VT.CONSTANT) {
                    Local n = new Local(i++);
                    Value old = var.reg.trim();
                    irMethod.stmts.insertBefore(var.start, Stmts.nAssign(n, old));
                    var.reg = n;
                    irMethod.locals.add(n);
                } else {
                    // throw new DexExcpeption("not support");
                }
            }
        }
    }
}
