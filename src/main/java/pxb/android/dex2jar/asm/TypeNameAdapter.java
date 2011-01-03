/*
 * Copyright (c) 2009-2010 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pxb.android.dex2jar.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class TypeNameAdapter extends ClassAdapter {

    protected static class AnnNameAdapter implements AnnotationVisitor {
        AnnotationVisitor av;

        /**
         * @param av
         */
        public AnnNameAdapter(AnnotationVisitor av) {
            super();
            this.av = av;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.AnnotationVisitor#visit(java.lang.String, java.lang.Object)
         */
        public void visit(String name, Object value) {
            av.visit(name, value);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.AnnotationVisitor#visitAnnotation(java.lang.String, java.lang.String)
         */
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            AnnotationVisitor _av = av.visitAnnotation(name, desc);
            if (_av == null)
                return null;
            return new AnnNameAdapter(_av);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.AnnotationVisitor#visitArray(java.lang.String)
         */
        public AnnotationVisitor visitArray(String name) {
            AnnotationVisitor _av = av.visitArray(name);
            if (_av == null)
                return null;
            return new AnnNameAdapter(_av);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.AnnotationVisitor#visitEnd()
         */
        public void visitEnd() {
            av.visitEnd();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.AnnotationVisitor#visitEnum(java.lang.String, java.lang.String, java.lang.String)
         */
        public void visitEnum(String name, String desc, String value) {
            av.visitEnum(name, x(desc), value);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.ClassAdapter#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(desc, visible);
        if (av == null)
            return null;
        // return new AnnNameAdapter(av);
        return av;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.ClassAdapter#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(x(name), x(outerName), innerName, access);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.ClassAdapter#visitOuterClass(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(owner, name, desc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.Object)
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        if (fv != null)
            fv = new FieldNameAdapter(fv);
        return fv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (exceptions != null) {
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = x(exceptions[i]);
            }
        }
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null)
            mv = new MethodNameAdapter(mv);
        return mv;
    }

    /**
     * @param cv
     */
    public TypeNameAdapter(ClassVisitor cv) {
        super(cv);
    }

    public static String x(String s) {
        return Type.getType(s).getInternalName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        name = x(name);
        superName = x(superName);
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                interfaces[i] = x(interfaces[i]);
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    static protected class MethodNameAdapter extends MethodAdapter {

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.MethodAdapter#visitTryCatchBlock(org.objectweb.asm. Label, org.objectweb.asm.Label,
         * org.objectweb.asm.Label, java.lang.String)
         */
        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            super.visitTryCatchBlock(start, end, handler, type != null ? x(type) : null);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int, java.lang.String, java.lang.String,
         * java.lang.String)
         */
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, x(owner), name, desc);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String, java.lang.String,
         * java.lang.String)
         */
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, x(owner), name, desc);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.objectweb.asm.MethodAdapter#visitTypeInsn(int, java.lang.String)
         */
        @Override
        public void visitTypeInsn(int opcode, String type) {

            if (opcode == Opcodes.ANEWARRAY) {
                type = Type.getType(type).getElementType().getInternalName();
            } else {
                type = x(type);
            }
            super.visitTypeInsn(opcode, type);
        }

        /**
         * @param mv
         */
        public MethodNameAdapter(MethodVisitor mv) {
            super(mv);
        }

    }

    static protected class FieldNameAdapter extends FieldAdapter {

        /**
         * @param fv
         */
        public FieldNameAdapter(FieldVisitor fv) {
            super(fv);
        }

    }
}
