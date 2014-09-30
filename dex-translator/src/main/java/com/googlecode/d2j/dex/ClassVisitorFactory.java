package com.googlecode.d2j.dex;

import org.objectweb.asm.ClassVisitor;

public interface ClassVisitorFactory {

    /**
     * 
     * @param classInternalName
     *            class name
     * @return a ClassVisitor, to generate validate .class file, ClassWriter.COMPUTE_MAXS is required for ClassWriter
     */
    ClassVisitor create(String classInternalName);

}
