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

import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.ev.EncodedAnnotation;
import com.googlecode.d2j.dex.writer.io.DataOut;

public class AnnotationItem extends BaseItem {
    final public Visibility visibility;
    final public EncodedAnnotation annotation = new EncodedAnnotation();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationItem that = (AnnotationItem) o;

        if (!annotation.equals(that.annotation)) return false;
        if (visibility != that.visibility) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = visibility.hashCode();
        result = 31 * result + annotation.hashCode();
        return result;
    }

    public AnnotationItem(TypeIdItem type, Visibility visibility) {
        this.visibility = visibility;
        annotation.type = type;
    }

    @Override
    public int place(int offset) {
        offset += 1;
        return annotation.place(offset);
    }

    @Override
    public void write(DataOut out) {
        out.ubyte("visibility", visibility.value);
        annotation.write(out);
    }
}
