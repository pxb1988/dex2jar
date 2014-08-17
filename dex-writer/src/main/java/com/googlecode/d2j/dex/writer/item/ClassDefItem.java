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
package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ann.Idx;
import com.googlecode.d2j.dex.writer.ann.Off;
import com.googlecode.d2j.dex.writer.ev.EncodedArray;
import com.googlecode.d2j.dex.writer.ev.EncodedValue;
import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.ClassDataItem.EncodedField;
import com.googlecode.d2j.dex.writer.item.ClassDataItem.EncodedMethod;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClassDefItem extends BaseItem {
    @Idx
    public TypeIdItem clazz;
    public int accessFlags;
    @Idx
    public TypeIdItem superclazz;
    @Off
    public TypeListItem interfaces;
    @Idx
    public StringIdItem sourceFile;
    @Off
    public ClassDataItem classData;
    //
    public AnnotationSetItem classAnnotations;
    @Off
    private AnnotationsDirectoryItem annotations;// Build later
    @Off
    private EncodedArrayItem staticValues; // Build later

    @Override
    public int place(int offset) {
        return offset + 0x20;
    }

    public void prepare(ConstPool cp) {
        if (classData != null) {
            classData.prepare(cp);
        }
        preparteAnnotationsDirectoryItem(cp);
        prepareEncodedArrayItem(cp);
    }

    private void prepareEncodedArrayItem(ConstPool cp) {
        if (classData == null) {
            return;
        }
        List<EncodedField> fs = classData.staticFields;
        int count = -1;
        for (int i = 0; i < fs.size(); i++) {
            EncodedField f = fs.get(i);
            EncodedValue ev = f.staticValue;
            if (ev != null) {
                if (!ev.isDefaultValueForType()) {
                    count = i;
                }
            }
        }

        if (count >= 0) {
            EncodedArrayItem encodedArrayItem = cp.putEnCodedArrayItem();
            EncodedArray array = encodedArrayItem.value;
            for (int i = 0; i <= count; i++) {
                EncodedField f = fs.get(i);
                EncodedValue ev = f.staticValue;
                if (ev == null) {
                    array.values.add(EncodedValue.defaultValueForType(f.field.getTypeString()));
                } else {
                    array.values.add(ev);
                }
            }
            staticValues = encodedArrayItem;
        }
    }

    private void preparteAnnotationsDirectoryItem(ConstPool cp) {
        Map<FieldIdItem, AnnotationSetItem> fieldAnnotations = new TreeMap<>();
        Map<MethodIdItem, AnnotationSetItem> methodAnnotations = new TreeMap<>();
        Map<MethodIdItem, AnnotationSetRefListItem> parameterAnnotations = new TreeMap<>();
        if (classData != null) {
            collectField(fieldAnnotations, classData.staticFields, cp);
            collectField(fieldAnnotations, classData.instanceFields, cp);
            collectMethod(methodAnnotations, parameterAnnotations, classData.directMethods, cp);
            collectMethod(methodAnnotations, parameterAnnotations, classData.virtualMethods, cp);
        }
        if (this.classAnnotations != null || fieldAnnotations.size() > 0 || methodAnnotations.size() > 0
                || parameterAnnotations.size() > 0) {
            AnnotationsDirectoryItem annotationsDirectoryItem = cp.putAnnotationDirectoryItem();
            this.annotations = annotationsDirectoryItem;
            if (classAnnotations != null) {
                annotationsDirectoryItem.classAnnotations = cp.uniqAnnotationSetItem(classAnnotations);
            }
            if (fieldAnnotations.size() > 0) {
                annotationsDirectoryItem.fieldAnnotations = fieldAnnotations;
            }
            if (methodAnnotations.size() > 0) {
                annotationsDirectoryItem.methodAnnotations = methodAnnotations;
            }
            if (parameterAnnotations.size() > 0) {
                annotationsDirectoryItem.parameterAnnotations = parameterAnnotations;
            }
        }
    }

    private void collectMethod(Map<MethodIdItem, AnnotationSetItem> methodAnnotations,
                               Map<MethodIdItem, AnnotationSetRefListItem> parameterAnnotations, List<EncodedMethod> ms, ConstPool cp) {
        for (EncodedMethod m : ms) {
            if (m.annotationSetItem != null) {
                methodAnnotations.put(m.method, cp.uniqAnnotationSetItem(m.annotationSetItem));
            }
            if (m.parameterAnnotation != null) {
                parameterAnnotations.put(m.method, cp.uniqAnnotationSetRefListItem(m.parameterAnnotation));
            }
        }
    }

    private void collectField(Map<FieldIdItem, AnnotationSetItem> fieldAnnotations, List<EncodedField> fs, ConstPool cp) {
        for (EncodedField f : fs) {
            if (f.annotationSetItem != null) {
                fieldAnnotations.put(f.field, cp.uniqAnnotationSetItem(f.annotationSetItem));
            }
        }
    }

    @Override
    public void write(DataOut out) {
        out.uint("class_idx", clazz.index);
        out.uint("access_flags", this.accessFlags);
        out.uint("superclass_idx", superclazz == null ? NO_INDEX : superclazz.index);
        out.uint("interfaces_off", (interfaces == null || interfaces.items.size() == 0) ? 0 : interfaces.offset);
        out.uint("source_file_idx", sourceFile == null ? NO_INDEX : sourceFile.index);
        out.uint("annotations_off", annotations == null ? 0 : annotations.offset);
        out.uint("class_data_off", classData == null ? 0 : classData.offset);
        out.uint("static_values_off", staticValues == null ? 0 : staticValues.offset);
    }

}
