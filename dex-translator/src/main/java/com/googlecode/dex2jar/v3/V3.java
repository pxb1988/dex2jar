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
package com.googlecode.dex2jar.v3;

import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFileVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class V3 implements DexFileVisitor {

    public static final int REUSE_REGISTER = 1 << 0;
    public static final int TOPOLOGICAL_SORT = 1 << 1;
    protected ClassVisitorFactory cvf;
    protected Map<String, Integer> accessFlagsMap;
    protected Map<String, String> innerNameMap;
    protected Map<String, Set<String>> extraMemberClass;
    protected DexExceptionHandler exceptionHandler;
    protected int config;

    public V3(Map<String, Integer> innerAccessFlagsMap, Map<String, String> innerNameMap,
            Map<String, Set<String>> extraMemberClass, DexExceptionHandler exceptionHandler,
            ClassVisitorFactory classVisitorFactory) {
        this(innerAccessFlagsMap, innerNameMap, extraMemberClass, exceptionHandler, classVisitorFactory, 0);
    }

    public V3(Map<String, Integer> innerAccessFlagsMap, Map<String, String> innerNameMap,
            Map<String, Set<String>> extraMemberClass, DexExceptionHandler exceptionHandler,
            ClassVisitorFactory classVisitorFactory, int config) {
        this.accessFlagsMap = innerAccessFlagsMap;
        this.innerNameMap = innerNameMap;
        this.extraMemberClass = extraMemberClass;
        this.exceptionHandler = exceptionHandler;
        this.cvf = classVisitorFactory;
        this.config = config;
    }

    @Override
    public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
        final ClassVisitor cv = cvf.create(Type.getType(className).getInternalName());
        if (cv == null) {
            return null;
        }
        return new V3ClassAdapter(accessFlagsMap, innerNameMap, this.extraMemberClass, this.exceptionHandler, cv,
                access_flags, className, superClass, interfaceNames, config);
    }

    @Override
    public void visitEnd() {
    }

}
