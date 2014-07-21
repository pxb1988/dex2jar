/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.googlecode.d2j.visitors;

import com.googlecode.d2j.DexType;

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
     * @param name
     *            the value name.
     * @param value
     *            the actual value, whose type must be {@link Byte}, {@link Boolean}, {@link Character}, {@link Short},
     *            {@link Integer}, {@link Long}, {@link Float}, {@link Double}, {@link String} or {@link DexType}.
     */
    public void visit(String name, Object value) {
        if (visitor != null) {
            visitor.visit(name, value);
        }
    }

    /**
     * Visits an enumeration value of the annotation.
     * 
     * @param name
     *            the value name.
     * @param desc
     *            the class descriptor of the enumeration class.
     * @param value
     *            the actual enumeration value.
     */
    public void visitEnum(String name, String desc, String value) {
        if (this.visitor != null) {
            visitor.visitEnum(name, desc, value);
        }
    }

    /**
     * Visits a nested annotation value of the annotation.
     * 
     * @param name
     *            the value name.
     * @param desc
     *            the class descriptor of the nested annotation class.
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
