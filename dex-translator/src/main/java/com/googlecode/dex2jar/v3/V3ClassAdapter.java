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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Annotation.Item;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class V3ClassAdapter implements DexClassVisitor {

    protected int access_flags;
    protected Map<String, Integer> innerAccessFlagsMap;
    protected Map<String, String> innerNameMap;
    Map<String, Set<String>> extraMemberClass;
    protected List<Annotation> anns = new ArrayList<Annotation>();
    protected boolean build = false;
    protected String className;
    protected ClassVisitor cv;
    protected String file;
    protected String[] interfaceNames;
    protected String superClass;

    /**
     * @param innerNameMap
     * @param extraMemberClass
     * @param cv
     * @param access_flags
     * @param className
     * @param superClass
     * @param interfaceNames
     */
    public V3ClassAdapter(Map<String, Integer> accessFlagsMap, Map<String, String> innerNameMap,
            Map<String, Set<String>> extraMemberClass, ClassVisitor cv, int access_flags, String className,
            String superClass, String[] interfaceNames) {
        super();
        this.innerAccessFlagsMap = accessFlagsMap;
        this.innerNameMap = innerNameMap;
        this.extraMemberClass = extraMemberClass;
        this.cv = cv;
        this.access_flags = access_flags;
        this.className = className;
        this.superClass = superClass;
        this.interfaceNames = interfaceNames;
    }

    protected void build() {
        if (!build) {
            String signature = null;
            String enclosingClass = null;
            for (Iterator<Annotation> it = anns.iterator(); it.hasNext();) {
                Annotation ann = it.next();
                if ("Ldalvik/annotation/Signature;".equals(ann.type)) {
                    it.remove();
                    for (Item item : ann.items) {
                        if (item.name.equals("value")) {
                            Annotation values = (Annotation) item.value;
                            StringBuilder sb = new StringBuilder();
                            for (Item i : values.items) {
                                sb.append(i.value.toString());
                            }
                            signature = sb.toString();
                        }
                    }
                } else if (ann.type.equals("Ldalvik/annotation/EnclosingClass;")) {
                    it.remove();
                    for (Item i : ann.items) {
                        if (i.name.equals("value")) {
                            enclosingClass = i.value.toString();
                        }
                    }
                } else if (ann.type.equals("Ldalvik/annotation/EnclosingMethod;")) {
                    for (Item i : ann.items) {
                        if ("value".equals(i.name)) {
                            Method m = (Method) i.value;
                            enclosingClass = m.getOwner();
                        }
                    }
                }
            }

            if (isInnerClass) {
                Integer i = innerAccessFlagsMap.get(className);
                if (i != null) {
                    access_flags = i;
                }
            }
            int accessInClass = access_flags;
            if ((access_flags & Opcodes.ACC_INTERFACE) == 0) { // issue 55
                accessInClass |= Opcodes.ACC_SUPER;// 解决生成的class文件使用dx重新转换时使用的指令与原始指令不同的问题
            }

            // access in class has no acc_static or acc_private
            accessInClass &= ~(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE);

            if (isInnerClass && (access_flags & Opcodes.ACC_PROTECTED) != 0) {
                accessInClass &= ~Opcodes.ACC_PROTECTED;
                accessInClass |= Opcodes.ACC_PUBLIC;
            }

            String[] nInterfaceNames = null;
            if (interfaceNames != null) {
                nInterfaceNames = new String[interfaceNames.length];
                for (int i = 0; i < interfaceNames.length; i++) {
                    nInterfaceNames[i] = Type.getType(interfaceNames[i]).getInternalName();
                }
            }
            cv.visit(Opcodes.V1_6, accessInClass, Type.getType(className).getInternalName(), signature,
                    superClass == null ? null : Type.getType(superClass).getInternalName(), nInterfaceNames);

            Set<String> extraMember = extraMemberClass.get(className);

            if (extraMember != null) {
                for (String innerName : extraMember) {
                    cv.visitInnerClass(Type.getType(innerName).getInternalName(), null, null, 0);
                }
            }

            for (Annotation ann : anns) {
                if (ann.type.equals("Ldalvik/annotation/MemberClasses;")) {
                    for (Item i : ann.items) {
                        if (i.name.equals("value")) {
                            for (Item j : ((Annotation) i.value).items) {
                                String name = j.value.toString();
                                Integer access = innerAccessFlagsMap.get(name);
                                String innerName = innerNameMap.get(name);
                                cv.visitInnerClass(Type.getType(name).getInternalName(), Type.getType(className)
                                        .getInternalName(), innerName, access == null ? 0 : access);
                            }
                        }
                    }
                    continue;
                } else if (ann.type.equals("Ldalvik/annotation/InnerClass;")) {
                    String name = null;
                    for (Item i : ann.items) {
                        if (i.name.equals("name")) {
                            name = (String) i.value;
                        }
                    }
                    int accessInInnerClassAttr = access_flags & (~Opcodes.ACC_SUPER);// inner class attr has no
                                                                                     // acc_super

                    if (name == null) {
                        cv.visitOuterClass(Type.getType(enclosingClass).getInternalName(), null, null);
                        cv.visitInnerClass(Type.getType(className).getInternalName(), null, null,
                                accessInInnerClassAttr);
                    } else {
                        cv.visitInnerClass(Type.getType(className).getInternalName(), Type.getType(enclosingClass)
                                .getInternalName(), name, accessInInnerClassAttr);
                    }

                    continue;
                } else if (ann.type.equals("Ldalvik/annotation/EnclosingMethod;")) {
                    for (Item it : ann.items) {
                        if ("value".equals(it.name)) {
                            Method m = (Method) it.value;
                            cv.visitOuterClass(Type.getType(m.getOwner()).getInternalName(), m.getName(), m.getType()
                                    .getDesc());
                        }
                    }
                    continue;
                }
                AnnotationVisitor av = cv.visitAnnotation(ann.type, ann.visible);
                V3AnnAdapter.accept(ann.items, av);
                av.visitEnd();
            }
            if (file != null) {
                cv.visitSource(file, null);
            }
            build = true;
        }
    }

    boolean isInnerClass = false;

    public AnnotationVisitor visitAnnotation(String name, boolean visible) {
        if (!isInnerClass) {
            isInnerClass = "Ldalvik/annotation/InnerClass;".equals(name);
        }
        Annotation ann = new Annotation(name, visible);
        anns.add(ann);
        return new V3AnnAdapter(ann);
    }

    public void visitEnd() {
        build();
        cv.visitEnd();
    }

    public DexFieldVisitor visitField(Field field, Object value) {
        build();
        return new V3FieldAdapter(cv, field, value);
    }

    public DexMethodVisitor visitMethod(Method method) {
        build();
        return new V3MethodAdapter(cv, method);
    }

    public void visitSource(String file) {
        this.file = file;
    }

}