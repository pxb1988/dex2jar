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
package com.googlecode.dex2jar.visitors;

import org.objectweb.asm.AnnotationVisitor;

/**
 * @author Panxiaobo [pxb1988 at gmail.com]
 * 
 */
public class DexFieldAdaptor implements DexFieldVisitor {

    private DexFieldVisitor dfv;

    /**
     * @param dfv
     */
    public DexFieldAdaptor(DexFieldVisitor dfv) {
        super();
        this.dfv = dfv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationAble#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String name, boolean visitable) {

        return dfv.visitAnnotation(name, visitable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFieldVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        dfv.visitEnd();
    }

}
