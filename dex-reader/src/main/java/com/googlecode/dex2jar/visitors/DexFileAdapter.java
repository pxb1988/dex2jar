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

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexFileAdapter implements DexFileVisitor {
    protected DexFileVisitor dfv;

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
        return dfv.visit(access_flags, className, superClass, interfaceNames);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visitEnd()
     */
    public void visitEnd() {
        dfv.visitEnd();
    }

}
