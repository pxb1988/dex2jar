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
 * @author bob
 * 
 */
public class DexFieldVisitor implements DexAnnotationAble {
    protected DexFieldVisitor visitor;

    public DexFieldVisitor(DexFieldVisitor visitor) {
        super();
        this.visitor = visitor;
    }

    public DexFieldVisitor() {
    }

    public void visitEnd() {
        if (visitor == null) {
            return;
        }
        visitor.visitEnd();
    }

    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitAnnotation(name, visibility);
    }
}
