package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnnotationSetItem extends BaseItem {

    public List<AnnotationItem> annotations = new ArrayList<>(3);

    private static final Comparator<AnnotationItem> CMP
            = Comparator.comparing(o -> o.annotation.type);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnnotationSetItem that = (AnnotationSetItem) o;

        return annotations.equals(that.annotations);
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
        annotations.sort(CMP);
        out.uint("size", annotations.size());
        for (AnnotationItem item : annotations) {
            out.uint("annotation_off", item.offset);
        }
    }

}
