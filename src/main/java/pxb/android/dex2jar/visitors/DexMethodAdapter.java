/*
 * Copyright (c) 2009-2011 Panxiaobo
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
package pxb.android.dex2jar.visitors;

import org.objectweb.asm.AnnotationVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class DexMethodAdapter implements DexMethodVisitor {
    protected DexMethodVisitor mv;

    /**
     * @param mv
     */
    public DexMethodAdapter(DexMethodVisitor mv) {
        super();
        this.mv = mv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexAnnotationAble#visitAnnotation(java.lang.String, boolean)
     */
    public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
        return mv.visitAnnotation(name, visitable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitCode()
     */
    public DexCodeVisitor visitCode() {
        return mv.visitCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitEnd()
     */
    public void visitEnd() {
        mv.visitEnd();
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitParamesterAnnotation (int)
     */
    public DexAnnotationAble visitParamesterAnnotation(int index) {
        return mv.visitParamesterAnnotation(index);
    }

}
