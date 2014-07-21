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

import com.googlecode.d2j.dex.writer.ann.Off;
import com.googlecode.d2j.dex.writer.io.DataOut;

import java.util.Map;
import java.util.Map.Entry;

public class AnnotationsDirectoryItem extends BaseItem {
    @Off
    public AnnotationSetItem classAnnotations;
    public Map<FieldIdItem, AnnotationSetItem> fieldAnnotations;
    public Map<MethodIdItem, AnnotationSetItem> methodAnnotations;
    public Map<MethodIdItem, AnnotationSetRefListItem> parameterAnnotations;

    @Override
    public int place(int offset) {
        offset += 16;
        if (fieldAnnotations != null) {
            offset += fieldAnnotations.size() * 8;
        }
        if (methodAnnotations != null) {
            offset += methodAnnotations.size() * 8;
        }
        if (parameterAnnotations != null) {
            offset += parameterAnnotations.size() * 8;
        }
        return offset;
    }

    @Override
    public void write(DataOut out) {
        out.uint("class_annotations_off", classAnnotations == null ? 0 : classAnnotations.offset);
        out.uint("fields_size", fieldAnnotations == null ? 0 : fieldAnnotations.size());
        out.uint("annotated_methods_size", methodAnnotations == null ? 0 : methodAnnotations.size());
        out.uint("annotated_parameter_size", parameterAnnotations == null ? 0 : parameterAnnotations.size());
        if (fieldAnnotations != null) {
            for (Entry<FieldIdItem, AnnotationSetItem> fe : fieldAnnotations.entrySet()) {
                out.uint("field_idx", fe.getKey().index);
                out.uint("annotations_off", fe.getValue().offset);
            }
        }
        if (methodAnnotations != null) {
            for (Entry<MethodIdItem, AnnotationSetItem> fe : methodAnnotations.entrySet()) {
                out.uint("method_idx", fe.getKey().index);
                out.uint("annotations_off", fe.getValue().offset);
            }
        }
        if (parameterAnnotations != null) {
            for (Entry<MethodIdItem, AnnotationSetRefListItem> fe : parameterAnnotations.entrySet()) {
                out.uint("method_idx", fe.getKey().index);
                out.uint("annotations_off", fe.getValue().offset);
            }
        }
    }
}
