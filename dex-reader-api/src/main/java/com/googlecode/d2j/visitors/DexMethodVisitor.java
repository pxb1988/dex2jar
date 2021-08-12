package com.googlecode.d2j.visitors;

import com.googlecode.d2j.Visibility;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexMethodVisitor implements DexAnnotationAble {

    protected DexMethodVisitor visitor;

    public DexMethodVisitor() {
        super();
    }

    /**
     *
     */
    public DexMethodVisitor(DexMethodVisitor mv) {
        super();
        this.visitor = mv;
    }

    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitAnnotation(name, visibility);
    }

    public DexCodeVisitor visitCode() {
        if (visitor == null) {
            return null;
        }
        return visitor.visitCode();
    }

    public void visitEnd() {
        if (visitor == null) {
            return;
        }
        visitor.visitEnd();
    }

    public DexAnnotationAble visitParameterAnnotation(int index) {
        if (visitor == null) {
            return null;
        }
        return visitor.visitParameterAnnotation(index);
    }

}
