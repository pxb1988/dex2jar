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

import com.googlecode.dex2jar.DexType;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public interface DexClassVisitor extends DexAnnotationAble {

    void visitSource(String file);

    /**
     * @param accessFlags
     * @param field
     * @param value
     *            the actual value, whose type must be {@link Byte}, {@link Boolean}, {@link Character}, {@link Short},
     *            {@link Integer}, {@link Long}, {@link Float}, {@link Double}, {@link String} or {@link DexType}.
     * @return
     */
    DexFieldVisitor visitField(int accessFlags, Field field, Object value);

    DexMethodVisitor visitMethod(int accessFlags, Method method);

    void visitEnd();
}
