package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ann.Off;
import com.googlecode.d2j.dex.writer.io.DataOut;

public class AnnotationSetRefListItem extends BaseItem {

    @Off
    public final AnnotationSetItem[] annotationSets;

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
