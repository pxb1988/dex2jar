package com.googlecode.d2j.visitors;

/**
 * A visitor to visit a Java annotation. The methods of this interface must be called in the following order: (
 * <tt>visit</tt> | <tt>visitEnum</tt> | <tt>visitAnnotation</tt> | <tt>visitArray</tt>)* <tt>visitEnd</tt>.
 *
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class DexAnnotationVisitor {

    protected DexAnnotationVisitor visitor;

    public DexAnnotationVisitor() {
        super();
    }

    public DexAnnotationVisitor(DexAnnotationVisitor visitor) {
        super();
        this.visitor = visitor;
    }

    /**
     * Visits a primitive value of the annotation.
     *
     * @param name  the value name.
     * @param value the actual value, whose type must be {@link Byte}, {@link Boolean}, {@link Character},
     *              {@link Short},
     *              {@link Integer}, {@link Long}, {@link Float}, {@link Double}, {@link String} or
     *              {@link com.googlecode.d2j.DexType}.
     */
    public void visit(String name, Object value) {
        if (visitor != null) {
            visitor.visit(name, value);
        }
    }

    /**
     * Visits an enumeration value of the annotation.
     *
     * @param name  the value name.
     * @param desc  the descriptor of the enumeration-class.
     * @param value the actual enumeration value.
     */
    public void visitEnum(String name, String desc, String value) {
        if (this.visitor != null) {
            visitor.visitEnum(name, desc, value);
        }
    }

    /**
     * Visits a nested annotation value of the annotation.
     *
     * @param name the value name.
     * @param desc the descriptor of the nested annotation-class.
     * @return a visitor to visit the actual nested annotation value, or <tt>null</tt> if this visitor is not interested
     *         in visiting this nested annotation. <i>The nested annotation value must be fully visited before calling
     *         other methods on this annotation visitor</i>.
     */
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {
        if (this.visitor != null) {
            return this.visitor.visitAnnotation(name, desc);
        }
        return null;
    }

    public DexAnnotationVisitor visitArray(String name) {
        if (visitor != null) {
            return visitor.visitArray(name);
        }
        return null;
    }

    /**
     * Visits the end of the annotation.
     */
    public void visitEnd() {
        if (this.visitor != null) {
            visitor.visitEnd();
        }
    }

}
