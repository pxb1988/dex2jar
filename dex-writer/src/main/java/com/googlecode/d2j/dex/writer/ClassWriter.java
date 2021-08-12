package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.ev.EncodedValue;
import com.googlecode.d2j.dex.writer.item.AnnotationItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.ClassDefItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

/*package*/ class ClassWriter extends DexClassVisitor implements DexConstants {

    public final ConstPool cp;

    public ClassDefItem defItem;

    ClassDataItem dataItem = new ClassDataItem();

    ClassWriter(ClassDefItem defItem, ConstPool cp) {
        super();
        this.defItem = defItem;
        this.cp = cp;
    }

    @Override
    public AnnotationWriter visitAnnotation(String type, Visibility visibility) {
        final AnnotationItem annItem = new AnnotationItem(cp.uniqType(type),
                visibility);
        AnnotationSetItem asi = defItem.classAnnotations;
        if (asi == null) {
            asi = new AnnotationSetItem();
            defItem.classAnnotations = asi;
        }
        asi.annotations.add(annItem);
        return new AnnotationWriter(annItem.annotation.elements, cp);
    }

    public void visitEnd() {
        if (dataItem != null && dataItem.getMemberSize() > 0) {
            cp.addClassDataItem(dataItem);
            defItem.classData = dataItem;
        }
        defItem.prepare(cp);

    }

    @Override
    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
        final ClassDataItem.EncodedField encodedField = new ClassDataItem.EncodedField();
        encodedField.accessFlags = accessFlags;
        encodedField.field = cp.uniqField(field);
        if (value != null) {
            encodedField.staticValue = EncodedValue.wrap(cp.wrapEncodedItem(value));
        }
        if (0 != (ACC_STATIC & accessFlags)) { // is static
            dataItem.staticFields.add(encodedField);
        } else {
            dataItem.instanceFields.add(encodedField);
        }

        return new FieldWriter(encodedField, cp);
    }

    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        final ClassDataItem.EncodedMethod encodedMethod = new ClassDataItem.EncodedMethod();
        encodedMethod.accessFlags = accessFlags;
        encodedMethod.method = cp.uniqMethod(method);
        if (0 != (accessFlags & (ACC_STATIC | ACC_PRIVATE | ACC_CONSTRUCTOR))) {
            dataItem.directMethods.add(encodedMethod);
        } else {
            dataItem.virtualMethods.add(encodedMethod);
        }

        return new MethodWriter(encodedMethod, method,
                0 != (accessFlags & ACC_STATIC), cp);
    }

    @Override
    public void visitSource(String file) {
        defItem.sourceFile = cp.uniqString(file);
    }

}
