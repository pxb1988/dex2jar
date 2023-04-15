package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.item.AnnotationItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetRefListItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.CodeItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

/*package*/ class MethodWriter extends DexMethodVisitor {

    public final ConstPool cp;

    private final ClassDataItem.EncodedMethod encodedMethod;

    final boolean isStatic;

    final Method method;

    private final int parameterSize;

    MethodWriter(ClassDataItem.EncodedMethod encodedMethod, Method m,
                 boolean isStatic, ConstPool cp) {
        this.encodedMethod = encodedMethod;
        this.parameterSize = m.getParameterTypes().length;
        this.cp = cp;
        this.method = m;
        this.isStatic = isStatic;
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name,
                                                Visibility visibility) {
        final AnnotationItem annItem = new AnnotationItem(cp.uniqType(name),
                visibility);
        AnnotationSetItem asi = encodedMethod.annotationSetItem;
        if (asi == null) {
            asi = new AnnotationSetItem();
            encodedMethod.annotationSetItem = asi;
        }
        asi.annotations.add(annItem);
        return new AnnotationWriter(annItem.annotation.elements, cp);
    }

    @Override
    public DexCodeVisitor visitCode() {
        encodedMethod.code = new CodeItem();
        return new CodeWriter(encodedMethod, encodedMethod.code, method, isStatic, cp);
    }

    @Override
    public DexAnnotationAble visitParameterAnnotation(final int index) {
        return (name, visibility) -> {
            AnnotationSetRefListItem asrl = encodedMethod.parameterAnnotation;
            if (asrl == null) {
                asrl = new AnnotationSetRefListItem(parameterSize);
                encodedMethod.parameterAnnotation = asrl;
            }
            AnnotationSetItem asi = asrl.annotationSets[index];
            if (asi == null) {
                asi = new AnnotationSetItem();
                asrl.annotationSets[index] = asi;
            }
            final AnnotationItem annItem = new AnnotationItem(
                    cp.uniqType(name), visibility);
            asi.annotations.add(annItem);
            return new AnnotationWriter(annItem.annotation.elements, cp);
        };
    }

}
