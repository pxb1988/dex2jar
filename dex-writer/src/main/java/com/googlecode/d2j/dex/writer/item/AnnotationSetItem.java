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

import com.googlecode.d2j.dex.writer.io.DataOut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnnotationSetItem extends BaseItem {
    public List<AnnotationItem> annotations = new ArrayList<>(3);
    private static final Comparator<AnnotationItem> cmp = new Comparator<AnnotationItem>() {
        @Override
        public int compare(AnnotationItem o1, AnnotationItem o2) {
            return o1.annotation.type.compareTo(o2.annotation.type);
        }
    };
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationSetItem that = (AnnotationSetItem) o;

        if (!annotations.equals(that.annotations)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return annotations.hashCode();
    }

    @Override
    public int place(int offset) {
        return offset + 4 + annotations.size() * 4;
    }

    @Override
    public void write(DataOut out) {
        Collections.sort(annotations, cmp);
        out.uint("size", annotations.size());
        for (AnnotationItem item : annotations) {
            out.uint("annotation_off", item.offset);
        }
    }
}
