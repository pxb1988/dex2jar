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
package com.googlecode.dex2jar.v3;

import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFileVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class V3 implements DexFileVisitor {
    protected ClassVisitorFactory cvf;
    protected Map<String, Integer> accessFlagsMap;
    protected Map<String, String> innerNameMap;
    protected Map<String, Set<String>> extraMemberClass;

    /**
     * @param innerAccessFlagsMap
     * @param classVisitorFactory
     */
    public V3(Map<String, Integer> innerAccessFlagsMap, Map<String, String> innerNameMap,
            Map<String, Set<String>> extraMemberClass, ClassVisitorFactory classVisitorFactory) {
        this.accessFlagsMap = innerAccessFlagsMap;
        this.innerNameMap = innerNameMap;
        this.extraMemberClass = extraMemberClass;
        this.cvf = classVisitorFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    public DexClassVisitor visit(int access_flags, String className, String superClass, String... interfaceNames) {
        final ClassVisitor cv = cvf.create(Type.getType(className).getInternalName());
        if (cv == null)
            return null;
        return new V3ClassAdapter(accessFlagsMap, innerNameMap, this.extraMemberClass, cv, access_flags, className,
                superClass, interfaceNames);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visitEnd()
     */
    public void visitEnd() {
    }

}
