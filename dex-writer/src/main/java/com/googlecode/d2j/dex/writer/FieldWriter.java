package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.item.AnnotationItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;

/*package*/ class FieldWriter extends DexFieldVisitor {

    public final ConstPool cp;

    private final ClassDataItem.EncodedField encodedField;

    FieldWriter(ClassDataItem.EncodedField encodedField, ConstPool cp) {
        this.encodedField = encodedField;
        this.cp = cp;
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name,
                                                Visibility visibility) {
        final AnnotationItem annItem = new AnnotationItem(cp.uniqType(name),
                visibility);
        AnnotationSetItem asi = encodedField.annotationSetItem;
        if (asi == null) {
            asi = new AnnotationSetItem();
            encodedField.annotationSetItem = asi;
        }
        asi.annotations.add(annItem);
        return new AnnotationWriter(annItem.annotation.elements, cp);
    }

}
