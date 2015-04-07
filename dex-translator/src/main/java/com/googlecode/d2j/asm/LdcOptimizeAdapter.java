/*
 * Copyright (c) 2009-2012 Panxiaobo
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
package com.googlecode.d2j.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class LdcOptimizeAdapter extends MethodVisitor implements Opcodes {

    /**
     * @param mv
     */
    public LdcOptimizeAdapter(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.MethodAdapter#visitLdcInsn(java.lang.Object)
     */
    @Override
    public void visitLdcInsn(Object cst) {
        if (cst == null) {
            this.visitInsn(ACONST_NULL);
        } else if (cst instanceof Integer) {
            int value = (Integer) cst;
            if (value >= -1 && value <= 5) {
                super.visitInsn(ICONST_0 + value);
            } else if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
                super.visitIntInsn(BIPUSH, value);
            } else if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
                super.visitIntInsn(SIPUSH, value);
            } else {
                super.visitLdcInsn(cst);
            }
        } else if (cst instanceof Long) {
            long value = (Long) cst;
            if (value == 0L || value == 1L) {
                super.visitInsn(LCONST_0 + ((int) value));
            } else {
                super.visitLdcInsn(cst);
            }
        } else if (cst instanceof Float) {
            float value = (Float) cst;
            if (value == 0.0F) {
                super.visitInsn(FCONST_0);
            } else if (value == 1.0F) {
                super.visitInsn(FCONST_1);
            } else if (value == 2.0F) {
                super.visitInsn(FCONST_2);
            } else {
                super.visitLdcInsn(cst);
            }
        } else if (cst instanceof Double) {
            double value = (Double) cst;
            if (value == 0.0D) {
                super.visitInsn(DCONST_0);
            } else if (value == 1.0D) {
                super.visitInsn(DCONST_1);
            } else {
                super.visitLdcInsn(cst);
            }
        } else if(cst instanceof Type){
            Type t= (Type) cst;
            switch (t.getSort()) {
                case Type.BOOLEAN:
                    super.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                    break;
                case Type.BYTE:
                    super.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                    break;
                case Type.CHAR:
                    super.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
                    break;
                case Type.DOUBLE:
                    super.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                    break;
                case Type.FLOAT:
                    super.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                    break;
                case Type.INT:
                    super.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                    break;
                case Type.LONG:
                    super.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                    break;
                case Type.SHORT:
                    super.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                    break;
                default:
                    super.visitLdcInsn(cst);
            }
        } else {
            super.visitLdcInsn(cst);
        }
    }

    public static MethodVisitor wrap(MethodVisitor mv) {
        return mv == null ? null : new LdcOptimizeAdapter(mv);
    }

    public static ClassVisitor wrap(ClassVisitor cv) {
        return cv == null ? null : new ClassVisitor(Opcodes.ASM4, cv) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return wrap(super.visitMethod(access, name, desc, signature, exceptions));
            }
        };
    }

}
