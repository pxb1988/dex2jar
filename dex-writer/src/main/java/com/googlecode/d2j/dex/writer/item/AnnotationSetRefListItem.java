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

public class AnnotationSetRefListItem extends BaseItem {
    @Off
    final public AnnotationSetItem[] annotationSets;

    public AnnotationSetRefListItem(int size) {
        this.annotationSets = new AnnotationSetItem[size];
    }

    @Override
    public int place(int offset) {
        return offset + 4 + annotationSets.length * 4;
    }

    @Override
    public void write(DataOut out) {
        out.uint("size", annotationSets.length);
        for (AnnotationSetItem item : annotationSets) {
            out.uint("annotations_off", item == null ? 0 : item.offset);
        }
    }
}
