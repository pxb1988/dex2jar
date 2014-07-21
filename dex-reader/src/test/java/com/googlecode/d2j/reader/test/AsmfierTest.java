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

package com.googlecode.d2j.reader.test;

import java.io.File;

import org.junit.Test;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.util.ASMifierFileV;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

/**
 * @author bob
 * 
 */
public class AsmfierTest implements DexConstants {
    @Test
    public void test() {
        ASMifierFileV fv = new ASMifierFileV(new File("target/asmftest").toPath(), "a.b");
        DexClassVisitor cv = fv.visit(ACC_PUBLIC, "La/f;", "Ljava/lang/Object;", null);
        DexFieldVisitor f2v = cv.visitField(ACC_PUBLIC, new Field("La/f;", "abc", "I"), null);
        f2v.visitEnd();
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La/f;", "zz", new String[0], "I"));

        DexAnnotationAble pv = mv.visitParameterAnnotation(2);
        DexAnnotationVisitor dav = pv.visitAnnotation("Leeeff;", Visibility.BUILD);
        dav.visitEnd();
        DexCodeVisitor dcv = mv.visitCode();
        dcv.visitConstStmt(Op.FILL_ARRAY_DATA, 0, new int[] { 1, 2, 3 });
        dcv.visitStmt0R(Op.RETURN_VOID);
        dcv.visitEnd();
        mv.visitEnd();
        cv.visitEnd();
        fv.visitEnd();
    }
}
