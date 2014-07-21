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

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class DexClassVisitor implements DexAnnotationAble {
    protected DexClassVisitor visitor;

    public DexClassVisitor() {
        super();
    }

    /**
     * @param dcv
     */
    public DexClassVisitor(DexClassVisitor dcv) {
        super();
        this.visitor = dcv;
    }

    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitAnnotation(name, visibility);
    }

    public void visitEnd() {
        if (visitor == null) {
            return;
        }
        visitor.visitEnd();
    }

    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitField(accessFlags, field, value);
    }

    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitMethod(accessFlags, method);
    }

    public void visitSource(String file) {
        if (visitor == null) {
            return;
        }
        visitor.visitSource(file);
    }

}
