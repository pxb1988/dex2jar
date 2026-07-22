package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.ev.EncodedAnnotation;
import com.googlecode.d2j.dex.writer.io.DataOut;

public class AnnotationItem extends BaseItem {

    public final Visibility visibility;

    public final EncodedAnnotation annotation = new EncodedAnnotation();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnnotationItem that = (AnnotationItem) o;

        if (!annotation.equals(that.annotation)) {
            return false;
        }
        return visibility == that.visibility;
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
