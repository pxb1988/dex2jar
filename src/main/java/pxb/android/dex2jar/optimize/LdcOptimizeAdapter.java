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
package pxb.android.dex2jar.optimize;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
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
            if (value >= 0 && value <= 5) {
                super.visitInsn(ICONST_0 + value);
            } else if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
                super.visitIntInsn(BIPUSH, value);
            } else if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
                super.visitIntInsn(SIPUSH, value);
            } else {
                super.visitLdcInsn(cst);
            }
        } else {
            super.visitLdcInsn(cst);
        }
    }

}
