package com.googlecode.d2j.visitors;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public class DexClassVisitor implements DexAnnotationAble {

    protected DexClassVisitor visitor;

    public DexClassVisitor() {
        super();
    }

    /**
     *
     */
    public DexClassVisitor(DexClassVisitor dcv) {
        super();
        this.visitor = dcv;
    }

    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitAnnotation(name, visibility);
    }

    public void visitEnd() {
        if (visitor == null) {
            return;
        }
        visitor.visitEnd();
    }

    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitField(accessFlags, field, value);
    }

    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitMethod(accessFlags, method);
    }

    public void visitSource(String file) {
        if (visitor == null) {
            return;
        }
        visitor.visitSource(file);
    }

}
