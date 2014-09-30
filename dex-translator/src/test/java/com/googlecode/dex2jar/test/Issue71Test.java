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

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.runner.RunWith;

import static com.googlecode.d2j.reader.Op.*;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * 
 */
@RunWith(DexTranslatorRunner.class)
public class Issue71Test implements DexConstants {
    @Test
    public static void i71(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_STATIC, new Method("La;", "test", new String[] {}, "V"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitRegister(2);
                code.visitConstStmt(CONST_WIDE, 0, 0L);
                code.visitConstStmt(CONST_WIDE, 1, 2L);
                code.visitStmt3R(ADD_LONG, 0, 0, 1);
                code.visitStmt0R(RETURN_VOID);
                code.visitEnd();
            }
            mv.visitEnd();
        }
    }
}
