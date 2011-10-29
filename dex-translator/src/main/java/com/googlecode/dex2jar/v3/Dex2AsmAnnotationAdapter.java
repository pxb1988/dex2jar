package com.googlecode.dex2jar.v3;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.DexType;
import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;

public class Dex2AsmAnnotationAdapter implements DexAnnotationVisitor {
    protected AnnotationVisitor asm;

    public Dex2AsmAnnotationAdapter(AnnotationVisitor asm) {
        super();
        this.asm = asm;
    }

    @Override
    public void visit(String name, Object value) {
        if (value instanceof DexType) {
            value = Type.getType(((DexType) value).desc);
        }
        asm.visit(name, value);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        asm.visitEnum(name, desc, value);
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {
        AnnotationVisitor n = asm.visitAnnotation(name, desc);
        if (n != null) {
            return new Dex2AsmAnnotationAdapter(n);
        }
        return null;
    }

    @Override
    public DexAnnotationVisitor visitArray(String name) {
        AnnotationVisitor n = asm.visitArray(name);
        if (n != null) {
            return new Dex2AsmAnnotationAdapter(n);
        }
        return null;
    }

    @Override
    public void visitEnd() {
        asm.visitEnd();
    }

}
