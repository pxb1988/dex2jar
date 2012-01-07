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
package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.ir.Constant.nLong;
import static com.googlecode.dex2jar.ir.expr.Exprs.nAdd;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLocal;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;

import java.util.ArrayList;

import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Local;
import com.googlecode.dex2jar.ir.ts.LocalRemove;
import com.googlecode.dex2jar.ir.ts.LocalSplit;
import com.googlecode.dex2jar.ir.ts.LocalType;
import com.googlecode.dex2jar.ir.ts.Transformer;
import com.googlecode.dex2jar.v3.EndRemover;
import com.googlecode.dex2jar.v3.ExceptionHandlerCurrect;
import com.googlecode.dex2jar.v3.IrMethod2AsmMethod;
import com.googlecode.dex2jar.v3.LocalCurrect;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
public class Issue71Test {

    @Test
    public void shortTest() {
        IrMethod irMethod = new IrMethod();
        irMethod.name = "test";
        irMethod.args = new Type[] {};
        irMethod.ret = Type.VOID_TYPE;
        Local a = nLocal("a");
        irMethod.locals.add(a);

        irMethod.stmts.add(nAssign(a, nLong(0L)));
        irMethod.stmts.add(nAssign(a, nAdd(a, nLong(2))));

        Transformer[] tses = new Transformer[] { new ExceptionHandlerCurrect(), new LocalSplit(), new LocalRemove(),
                new LocalType(), new LocalCurrect() };
        Transformer endremove = new EndRemover();
        endremove.transform(irMethod);

        // indexLabelStmt4Debug(irMethod.stmts);

        for (Transformer ts : tses) {
            ts.transform(irMethod);
        }
        MethodNode node = new MethodNode();
        node.tryCatchBlocks = new ArrayList();
        new IrMethod2AsmMethod().convert(irMethod, node);
    }

}
