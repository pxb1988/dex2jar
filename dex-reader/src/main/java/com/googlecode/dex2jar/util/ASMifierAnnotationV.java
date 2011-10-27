/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package com.googlecode.dex2jar.util;

import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;

/**
 * @author Panxiaobo <pxb1988 at gmail.com>
 * @version $Id$
 */
public class ASMifierAnnotationV implements DexAnnotationVisitor {
    ArrayOut out;
    int i = 0;

    public ASMifierAnnotationV(String objName, ArrayOut out, String name, boolean visible) {
        this.out = out;
        out.s("{");
        out.push();
        out.s("DexAnnotationVisitor av%02d = %s.visitAnnotation(%s, %s);", i, objName, Escape.v(name),
                Escape.v(visible));
        out.s("if(av%02d != null) {", i);
        out.push();
    }

    @Override
    public void visit(String name, Object value) {
        out.s("av%02d.visit(%s, %s);", i, Escape.v(name), Escape.v(value));
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        out.s("av%02d.visitEnum(%s, %s, %s);", i, Escape.v(name), Escape.v(desc), Escape.v(value));
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {
        out.s("{");
        out.push();
        int old = i;
        int n = ++i;
        out.s("DexAnnotationVisitor av%02d = av%02d.visitAnnotation(%s, %s);", n, old, Escape.v(name), Escape.v(desc));
        out.s("if(av%02d != null) {", i);
        out.push();
        return this;
    }

    @Override
    public DexAnnotationVisitor visitArray(String name) {
        out.s("{");
        out.push();
        int old = i;
        int n = ++i;
        out.s("DexAnnotationVisitor av%02d = av%02d.visitArray(%s);", n, old, Escape.v(name));
        out.s("if(av%02d != null) {", i);
        out.push();
        return this;
    }

    @Override
    public void visitEnd() {
        out.s("av%02d.visitEnd();", i);
        i--;
        out.pop();
        out.s("}");
        out.pop();
        out.s("}");
    }
}
