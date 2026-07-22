package com.googlecode.d2j.visitors;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexFileVisitor {

    protected DexFileVisitor visitor;

    public DexFileVisitor() {
        super();
    }

    public DexFileVisitor(DexFileVisitor visitor) {
        super();
        this.visitor = visitor;
    }

    public void visitDexFileVersion(int version) {
        if (visitor != null) {
            visitor.visitDexFileVersion(version);
        }
    }

    public DexClassVisitor visit(int accessFlags, String className, String superClass, String[] interfaceNames) {
        if (visitor == null) {
            return null;
        }
        return visitor.visit(accessFlags, className, superClass, interfaceNames);
    }

    public void visitEnd() {
        if (visitor == null) {
            return;
        }
        visitor.visitEnd();
    }

}
