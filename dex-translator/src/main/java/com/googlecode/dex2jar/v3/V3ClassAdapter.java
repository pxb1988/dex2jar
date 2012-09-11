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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Annotation.Item;
import com.googlecode.dex2jar.DexType;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.v3.V3InnerClzGather.Clz;
import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class V3ClassAdapter implements DexClassVisitor {

    protected int access_flags;
    protected Map<String, Object> annotationDefaults;
    protected List<Annotation> anns = new ArrayList<Annotation>();
    protected boolean build = false;
    protected String className;
    protected Clz clz;
    protected int config;
    protected ClassVisitor cv;
    protected DexExceptionHandler exceptionHandler;
    protected String file;
    protected String[] interfaceNames;
    protected String superClass;

    public V3ClassAdapter(Clz clz, DexExceptionHandler exceptionHandler, ClassVisitor cv, int access_flags,
            String className, String superClass, String[] interfaceNames) {
        this(clz, exceptionHandler, cv, access_flags, className, superClass, interfaceNames, 0);
    }

    public V3ClassAdapter(Clz clz, DexExceptionHandler exceptionHandler, ClassVisitor cv, int access_flags,
            String className, String superClass, String[] interfaceNames, int config) {
        super();
        this.clz = clz;
        this.cv = cv;
        this.access_flags = access_flags;
        this.className = className;
        this.superClass = superClass;
        this.interfaceNames = interfaceNames;
        this.exceptionHandler = exceptionHandler;
        this.config = config;
    }

    protected void build() {
        if (!build) {
            String signature = null;
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
                }
            }

            int access = clz.access;
            boolean isInnerClass = clz.enclosingClass != null || clz.enclosingMethod != null;
            int accessInClass = clearClassAccess(isInnerClass, access);
            String[] nInterfaceNames = null;
            if (interfaceNames != null) {
                nInterfaceNames = new String[interfaceNames.length];
                for (int i = 0; i < interfaceNames.length; i++) {
                    nInterfaceNames[i] = Type.getType(interfaceNames[i]).getInternalName();
                }
            }
            cv.visit(Opcodes.V1_6, accessInClass, Type.getType(className).getInternalName(), signature,
                    superClass == null ? null : Type.getType(superClass).getInternalName(), nInterfaceNames);
            if (clz.inners != null) {
                for (Clz inner : clz.inners) {
                    if (inner.innerName == null) {// anonymous Innerclass
                        cv.visitInnerClass(Type.getType(inner.name).getInternalName(), null, null,
                                clearInnerAccess(inner.access));
                    } else {// non-anonymous Innerclass
                        cv.visitInnerClass(Type.getType(inner.name).getInternalName(), Type.getType(className)
                                .getInternalName(), inner.innerName, clearInnerAccess(inner.access));
                    }
                }
            }
            if (isInnerClass) {
                for (Clz p = clz; p != null; p = p.enclosingClass) {
                    Clz enclosingClass = p.enclosingClass;
                    if (enclosingClass == null) {
                        break;
                    }
                    Method enclosingMethod = p.enclosingMethod;
                    int accessInInner = clearInnerAccess(access);

                    if (p.innerName != null) {// non-anonymous Innerclass
                        String enclosingClassDesc = enclosingClass != null ? enclosingClass.name
                                : enclosingMethod != null ? enclosingMethod.getOwner() : null;
                        cv.visitInnerClass(Type.getType(p.name).getInternalName(), Type.getType(enclosingClassDesc)
                                .getInternalName(), p.innerName, accessInInner);
                    } else {// anonymous Innerclass
                        if (enclosingMethod != null) {
                            cv.visitOuterClass(Type.getType(enclosingMethod.getOwner()).getInternalName(),
                                    enclosingMethod.getName(), enclosingMethod.getDesc());
                        } else {
                            cv.visitOuterClass(Type.getType(enclosingClass.name).getInternalName(), null, null);
                        }
                        cv.visitInnerClass(Type.getType(p.name).getInternalName(), null, null, accessInInner);
                    }
                }
            }
            for (Annotation ann : anns) {
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

    private int clearClassAccess(boolean isInner, int access) {
        if ((access & Opcodes.ACC_INTERFACE) == 0) { // issue 55
            access |= Opcodes.ACC_SUPER;// 解决生成的class文件使用dx重新转换时使用的指令与原始指令不同的问题
        }
        // access in class has no acc_static or acc_private
        access &= ~(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE);
        if (isInner && (access & Opcodes.ACC_PROTECTED) != 0) {// protected inner class are public
            access &= ~Opcodes.ACC_PROTECTED;
            access |= Opcodes.ACC_PUBLIC;
        }
        return access;
    }

    private int clearInnerAccess(int access) {
        access &= (~Opcodes.ACC_SUPER);// inner class attr has no acc_super
        if (0 != (access & Opcodes.ACC_PRIVATE)) {// clear public/protected if it is private
            access &= ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
        } else if (0 != (access & Opcodes.ACC_PROTECTED)) {// clear public if it is protected
            access &= ~(Opcodes.ACC_PUBLIC);
        }
        return access;
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, boolean visible) {
        if (name.equals("Ldalvik/annotation/EnclosingClass;") || name.equals("Ldalvik/annotation/EnclosingMethod;")
                || "Ldalvik/annotation/InnerClass;".equals(name) || "Ldalvik/annotation/MemberClasses;".equals(name)) {
            return null;
        }
        if (name.equals("Ldalvik/annotation/AnnotationDefault;")) {
            return new EmptyVisitor() {
                @Override
                public DexAnnotationVisitor visitAnnotation(String name, String desc) {
                    return new EmptyVisitor() {
                        @Override
                        public void visit(String name, Object value) {
                            if (annotationDefaults == null) {
                                annotationDefaults = new HashMap<String, Object>();
                            }
                            if (value instanceof DexType) {
                                value = Type.getType(((DexType) value).desc);
                            }
                            annotationDefaults.put(name, value);
                        }
                    };
                }
            };
        } else {
            Annotation ann = new Annotation(name, visible);
            anns.add(ann);
            return new V3AnnAdapter(ann);
        }
    }

    @Override
    public void visitEnd() {
        build();
        cv.visitEnd();
    }

    @Override
    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
        build();
        if (value instanceof DexType) {
            value = Type.getType(((DexType) value).desc);
        }
        return new V3FieldAdapter(cv, accessFlags, field, value);
    }

    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        build();
        return new V3MethodAdapter(accessFlags, method, this.exceptionHandler, config) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                if (annotationDefaults != null) {
                    Object value = annotationDefaults.get(method.getName());
                    if (value != null) {
                        AnnotationVisitor av = methodNode.visitAnnotationDefault();
                        if (av != null) {
                            av.visit(null, value);
                            av.visitEnd();
                        }
                    }
                }
                methodNode.accept(cv);
            }
        };
    }

    @Override
    public void visitSource(String file) {
        this.file = file;
    }

}