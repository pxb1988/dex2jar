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
package com.googlecode.dex2jar.asm;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class LdcOptimizeAdapter extends MethodAdapter implements Opcodes {

    /**
     * @param mv
     */
    public LdcOptimizeAdapter(MethodVisitor mv) {
        super(mv);
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
        } else {
            super.visitLdcInsn(cst);
        }
    }

}
