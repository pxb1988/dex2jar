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

import org.junit.Test;

import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
public class Issue71Test implements DexOpcodes {
    public static void i71(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_STATIC, new Method("La;", "test", new String[] {}, "V"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitArguments(2, new int[] {});
                code.visitConstStmt(OP_CONST, 0, 0L, TYPE_WIDE);
                code.visitConstStmt(OP_CONST, 1, 2L, TYPE_WIDE);
                code.visitBinopStmt(OP_ADD, 0, 0, 1, TYPE_LONG);
                code.visitReturnStmt(OP_RETURN_VOID);
                code.visitEnd();
            }
            mv.visitEnd();
        }
    }

    @Test
    public void shortTest() throws Exception {
        TestUtils.testDexASMifier(getClass(), "i71");
    }

}
