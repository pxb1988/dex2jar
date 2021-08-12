package com.googlecode.d2j.visitors;

import com.googlecode.d2j.DexLabel;

public class DexDebugVisitor {

    protected DexDebugVisitor visitor;

    public DexDebugVisitor() {
    }

    public DexDebugVisitor(DexDebugVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * @param parameterIndex start with the first index of DexMethod.getParameterTypes(), no 'this'
     */
    public void visitParameterName(int parameterIndex, String name) {
        if (visitor != null) {
            visitor.visitParameterName(parameterIndex, name);
        }
    }

    public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
        if (visitor != null) {
            visitor.visitStartLocal(reg, label, name, type, signature);
        }
    }

    public void visitLineNumber(int line, DexLabel label) {
        if (visitor != null) {
            visitor.visitLineNumber(line, label);
        }
    }

    public void visitEndLocal(int reg, DexLabel label) {
        if (visitor != null) {
            visitor.visitEndLocal(reg, label);
        }
    }

    public void visitSetFile(String file) {
        if (visitor != null) {
            visitor.visitSetFile(file);
        }
    }

    public void visitPrologue(DexLabel dexLabel) {
        if (visitor != null) {
            visitor.visitPrologue(dexLabel);
        }
    }

    public void visitEpiogue(DexLabel dexLabel) {
        if (visitor != null) {
            visitor.visitEpiogue(dexLabel);
        }
    }

    public void visitRestartLocal(int reg, DexLabel label) {
        if (visitor != null) {
            visitor.visitRestartLocal(reg, label);
        }
    }

    public void visitEnd() {
        if (visitor != null) {
            visitor.visitEnd();
        }
    }

}
