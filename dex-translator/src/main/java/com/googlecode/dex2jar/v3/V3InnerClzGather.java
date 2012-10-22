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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Annotation.Item;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFileVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * gathering inner
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class V3InnerClzGather implements DexFileVisitor {
    public static class Clz {
        public int access;
        public Clz enclosingClass;
        public Method enclosingMethod;
        public String innerName;
        public Set<Clz> inners = null;
        public final String name;

        public Clz(String name) {
            super();
            this.name = name;
        }

        void addInner(Clz clz) {
            if (inners == null) {
                inners = new HashSet<Clz>();
            }
            inners.add(clz);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Clz other = (Clz) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        public String toString() {
            return "" + name;
        }
    }

    private Map<String, Clz> classes = new HashMap<String, Clz>();

    private Clz get(String name) {
        Clz clz = classes.get(name);
        if (clz == null) {
            clz = new Clz(name);
            classes.put(name, clz);
        }
        return clz;
    }

    public Map<String, Clz> getClasses() {
        return classes;
    }

    private static final int ACC_INTERFACE_ABSTRACT = (Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT);

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFileVisitor#visit(int, java.lang.String, java.lang.String,
     * java.lang.String[])
     */
    @Override
    public DexClassVisitor visit(int access_flags, final String className, String superClass, String[] interfaceNames) {

        final Clz clz = get(className);

        clz.access = (clz.access & ~ACC_INTERFACE_ABSTRACT) | access_flags;

        return new EmptyVisitor() {
            protected List<Annotation> anns = new ArrayList<Annotation>();

            @Override
            public DexAnnotationVisitor visitAnnotation(String name, boolean visible) {
                Annotation ann = new Annotation(name, visible);
                anns.add(ann);
                return new V3AnnAdapter(ann);
            }

            @Override
            public void visitEnd() {
                for (Annotation ann : anns) {
                    if (ann.type.equals("Ldalvik/annotation/EnclosingClass;")) {
                        for (Item i : ann.items) {
                            if (i.name.equals("value")) {
                                clz.enclosingClass = get(i.value.toString());
                                clz.enclosingClass.addInner(clz);
                            }
                        }
                    } else if (ann.type.equals("Ldalvik/annotation/EnclosingMethod;")) {
                        for (Item i : ann.items) {
                            if ("value".equals(i.name)) {
                                Method m = (Method) i.value;
                                if (m != null) {
                                    clz.enclosingMethod = m;
                                    clz.enclosingClass = get(clz.enclosingMethod.getOwner());
                                    clz.enclosingClass.addInner(clz);
                                }
                            }
                        }
                    } else if ("Ldalvik/annotation/InnerClass;".equals(ann.type)) {
                        for (Item it : ann.items) {
                            if ("accessFlags".equals(it.name)) {
                                clz.access |= (Integer) it.value & ~ACC_INTERFACE_ABSTRACT;
                            } else if ("name".equals(it.name)) {
                                clz.innerName = (String) it.value;
                            }
                        }
                    } else if ("Ldalvik/annotation/MemberClasses;".equals(ann.type)) {
                        for (Item it : ann.items) {
                            if ("value".equals(it.name)) {
                                Annotation v = (Annotation) it.value;
                                for (Item item : v.items) {
                                    Type type = (Type) item.value;
                                    Clz inner = get(type.getDescriptor());
                                    clz.addInner(inner);
                                    inner.enclosingClass = clz;
                                }
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
    @Override
    public void visitEnd() {
    }

}
