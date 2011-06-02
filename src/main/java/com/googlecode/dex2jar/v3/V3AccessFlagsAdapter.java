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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Annotation.Item;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;
import com.googlecode.dex2jar.visitors.DexFileVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class V3AccessFlagsAdapter implements DexFileVisitor {
    private Map<String, Integer> map = new HashMap<String, Integer>();
    private Map<String, String> innerNameMap = new HashMap<String, String>();

    /**
     * @return the innerNameMap
     */
    public Map<String, String> getInnerNameMap() {
        return innerNameMap;
    }

    public Map<String, Integer> getAccessFlagsMap() {
        return map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    public DexClassVisitor visit(int access_flags, final String className, String superClass, String... interfaceNames) {

        return new DexClassVisitor() {
            protected List<Annotation> anns = new ArrayList<Annotation>();

            @Override
            public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
                Annotation ann = new Annotation(name, visitable);
                anns.add(ann);
                return new V3AnnAdapter(ann);
            }

            @Override
            public void visitSource(String file) {
            }

            @Override
            public DexMethodVisitor visitMethod(Method method) {
                return null;
            }

            @Override
            public DexFieldVisitor visitField(Field field, Object value) {
                return null;
            }

            @Override
            public void visitEnd() {
                for (Annotation ann : anns) {
                    if ("Ldalvik/annotation/InnerClass;".equals(ann.type)) {
                        for (Item it : ann.items) {
                            if ("accessFlags".equals(it.name)) {
                                map.put(className, (Integer) it.value);
                            } else if ("name".equals(it.name)) {
                                innerNameMap.put(className, (String) it.value);
                            }
                        }
                    }
                }
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visitEnd()
     */
    public void visitEnd() {
    }

}
