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
package com.googlecode.d2j.visitors;

import com.googlecode.d2j.Visibility;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexMethodVisitor implements DexAnnotationAble {
    protected DexMethodVisitor visitor;

    public DexMethodVisitor() {
        super();
    }

    /**
     * @param mv
     */
    public DexMethodVisitor(DexMethodVisitor mv) {
        super();
        this.visitor = mv;
    }

    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitAnnotation(name, visibility);
    }

    public DexCodeVisitor visitCode() {
        if (visitor == null) {
            return null;
        }
        return visitor.visitCode();
    }

    public void visitEnd() {
        if (visitor == null) {
            return;
        }
        visitor.visitEnd();
    }

    public DexAnnotationAble visitParameterAnnotation(int index) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitParameterAnnotation(index);
    }

}
