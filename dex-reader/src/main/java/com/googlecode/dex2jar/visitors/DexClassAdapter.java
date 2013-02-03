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
package com.googlecode.dex2jar.visitors;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexClassAdapter implements DexClassVisitor {
    protected DexClassVisitor dcv;

    /**
     * @param dcv
     */
    public DexClassAdapter(DexClassVisitor dcv) {
        super();
        this.dcv = dcv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitAnnotation(java.lang .String, boolean)
     */
    @Override
    public DexAnnotationVisitor visitAnnotation(String name, boolean visible) {
        return dcv.visitAnnotation(name, visible);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        dcv.visitEnd();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitField(int, com.googlecode.dex2jar .Field,
     * java.lang.Object)
     */
    @Override
    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
        return dcv.visitField(accessFlags, field, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitMethod(int, com.googlecode. dex2jar.Method)
     */
    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        return dcv.visitMethod(accessFlags, method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexClassVisitor#visitSource(java.lang.String )
     */
    @Override
    public void visitSource(String file) {
        dcv.visitSource(file);
    }

}
