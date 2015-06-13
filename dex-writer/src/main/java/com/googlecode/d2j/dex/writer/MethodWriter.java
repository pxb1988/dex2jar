/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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
package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.item.*;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

/*package*/class MethodWriter extends DexMethodVisitor {
    final public ConstPool cp;
    private final ClassDataItem.EncodedMethod encodedMethod;
    final boolean isStatic;
    final Method method;
    private final int parameterSize;

    public MethodWriter(ClassDataItem.EncodedMethod encodedMethod, Method m,
                        boolean isStatic, ConstPool cp) {
        this.encodedMethod = encodedMethod;
        this.parameterSize = m.getParameterTypes().length;
        this.cp = cp;
        this.method = m;
        this.isStatic = isStatic;
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name,
                                                Visibility visibility) {
        final AnnotationItem annItem = new AnnotationItem(cp.uniqType(name),
                visibility);
        AnnotationSetItem asi = encodedMethod.annotationSetItem;
        if (asi == null) {
            asi = new AnnotationSetItem();
            encodedMethod.annotationSetItem = asi;
        }
        asi.annotations.add(annItem);
        return new AnnotationWriter(annItem.annotation.elements, cp);
    }

    @Override
    public DexCodeVisitor visitCode() {
        encodedMethod.code = new CodeItem();
        return new CodeWriter(encodedMethod, encodedMethod.code, method, isStatic, cp);
    }

    @Override
    public DexAnnotationAble visitParameterAnnotation(final int index) {
        return new DexAnnotationAble() {
            @Override
            public DexAnnotationVisitor visitAnnotation(String name,
                                                        Visibility visibility) {
                AnnotationSetRefListItem asrl = encodedMethod.parameterAnnotation;
                if (asrl == null) {
                    asrl = new AnnotationSetRefListItem(parameterSize);
                    encodedMethod.parameterAnnotation = asrl;
                }
                AnnotationSetItem asi = asrl.annotationSets[index];
                if (asi == null) {
                    asi = new AnnotationSetItem();
                    asrl.annotationSets[index] = asi;
                }
                final AnnotationItem annItem = new AnnotationItem(
                        cp.uniqType(name), visibility);
                asi.annotations.add(annItem);
                return new AnnotationWriter(annItem.annotation.elements, cp);
            }
        };
    }
}
