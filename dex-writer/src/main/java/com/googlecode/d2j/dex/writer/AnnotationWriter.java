package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.DexType;
import com.googlecode.d2j.dex.writer.ev.EncodedAnnotation;
import com.googlecode.d2j.dex.writer.ev.EncodedAnnotation.AnnotationElement;
import com.googlecode.d2j.dex.writer.ev.EncodedArray;
import com.googlecode.d2j.dex.writer.ev.EncodedValue;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import java.util.List;

/*package*/ class AnnotationWriter extends DexAnnotationVisitor {

    ConstPool cp;

    List<AnnotationElement> elements;

    AnnotationWriter(List<AnnotationElement> elements, ConstPool cp) {
        this.elements = elements;
        this.cp = cp;
    }

    AnnotationElement newAnnotationElement(String name) {
        AnnotationElement ae = new AnnotationElement();
        ae.name = cp.uniqString(name);
        elements.add(ae);
        return ae;
    }

    // int,int long
    public void visit(String name, Object value) {
        if (value instanceof Object[]) {
            DexAnnotationVisitor s = visitArray(name);
            if (s != null) {
                for (Object v : (Object[]) value) {
                    s.visit(null, v);
                }
                s.visitEnd();
            }
        } else {
            AnnotationElement ae = newAnnotationElement(name);
            ae.value = EncodedValue.wrap(cp.wrapEncodedItem(value));
        }
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {
        EncodedValue encodedValue;
        EncodedAnnotation encodedAnnotation = new EncodedAnnotation();
        encodedAnnotation.type = cp.uniqType(desc);
        encodedValue = new EncodedValue(EncodedValue.VALUE_ANNOTATION,
                encodedAnnotation);
        AnnotationElement ae = newAnnotationElement(name);
        ae.value = encodedValue;
        return new AnnotationWriter(encodedAnnotation.elements, cp);
    }

    @Override
    public DexAnnotationVisitor visitArray(String name) {
        AnnotationElement ae = newAnnotationElement(name);
        final EncodedArray encodedArray = new EncodedArray();
        ae.value = new EncodedValue(EncodedValue.VALUE_ARRAY, encodedArray);
        return new EncodedArrayAnnWriter(encodedArray);
    }

    @Override
    public void visitEnum(String name, String fower, String fname) {
        AnnotationElement ae = newAnnotationElement(name);
        ae.value = new EncodedValue(EncodedValue.VALUE_ENUM, cp.uniqField(
                fower, fname, fower));
    }

    class EncodedArrayAnnWriter extends DexAnnotationVisitor {

        final EncodedArray encodedArray;

        EncodedArrayAnnWriter(EncodedArray encodedArray) {
            super();
            this.encodedArray = encodedArray;
        }

        @Override
        public void visit(String name, Object value) {
            EncodedValue encodedValue;
            if (value instanceof String) {
                encodedValue = new EncodedValue(EncodedValue.VALUE_STRING,
                        cp.uniqString((String) value));
            } else if (value instanceof DexType) {
                encodedValue = new EncodedValue(EncodedValue.VALUE_TYPE,
                        cp.uniqType(((DexType) value).desc));
            } else {
                encodedValue = EncodedValue.wrap(value);
            }
            encodedArray.values.add(encodedValue);
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(String name, String desc) {
            EncodedValue encodedValue;
            EncodedAnnotation encodedAnnotation = new EncodedAnnotation();
            encodedAnnotation.type = cp.uniqType(desc);
            encodedValue = new EncodedValue(EncodedValue.VALUE_ANNOTATION,
                    encodedAnnotation);
            encodedArray.values.add(encodedValue);
            return new AnnotationWriter(encodedAnnotation.elements, cp);
        }

        @Override
        public DexAnnotationVisitor visitArray(String name) {
            EncodedValue encodedValue;
            encodedValue = new EncodedValue(EncodedValue.VALUE_ARRAY,
                    encodedArray);
            encodedArray.values.add(encodedValue);
            return new EncodedArrayAnnWriter(encodedArray);
        }

        @Override
        public void visitEnum(String name, String fower, String fname) {
            EncodedValue encodedValue;
            encodedValue = new EncodedValue(EncodedValue.VALUE_ENUM,
                    cp.uniqField(fower, fname, fower));
            encodedArray.values.add(encodedValue);
        }

    }

}
