/*
 * Copyright (c) 2009-2010 Panxiaobo
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
package pxb.android.dex2jar.v3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import pxb.android.dex2jar.Field;
import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.asm.TypeNameAdapter;
import pxb.android.dex2jar.v3.Ann.Item;
import pxb.android.dex2jar.visitors.DexClassVisitor;
import pxb.android.dex2jar.visitors.DexFieldVisitor;
import pxb.android.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * @version $Id$
 */
public class V3ClassAdapter implements DexClassVisitor {

    protected ClassVisitor cv;
    protected boolean build = false;
    protected int access_flags;
    protected String className;
    protected String superClass;
    protected String[] interfaceNames;

    protected void build() {
        if (!build) {
            String signature = null;
            for (Iterator<Ann> it = anns.iterator(); it.hasNext();) {
                Ann ann = it.next();
                if ("Ldalvik/annotation/Signature;".equals(ann.type)) {
                    it.remove();
                    for (Item item : ann.items) {
                        if (item.name.equals("value")) {
                            Ann values = (Ann) item.value;
                            StringBuilder sb = new StringBuilder();
                            for (Item i : values.items) {
                                sb.append(i.value.toString());
                            }
                            signature = sb.toString();
                        }
                    }
                }
            }
            access_flags |= Opcodes.ACC_SUPER;// 解决生成的class文件使用dx重新转换时使用的指令与原始指令不同的问题
            cv.visit(Opcodes.V1_6, access_flags | 0x20, className, signature, superClass, interfaceNames);
            for (Ann ann : anns) {
                if (ann.type.equals("Ldalvik/annotation/MemberClasses;")) {
                    for (Item i : ann.items) {
                        if (i.name.equals("value")) {
                            for (Item j : ((Ann) i.value).items) {
                                String name = j.value.toString();
                                Integer access = accessFlagsMap.get(name);
                                int d = name.lastIndexOf('$');
                                String innerName = name.substring(d + 1, name.length() - 1);
                                // TODO设置默认内部类修饰符
                                cv.visitInnerClass(name, className, innerName, access == null ? 0 : access);
                            }
                        }
                    }
                    continue;
                } else if (ann.type.equals("Ldalvik/annotation/EnclosingClass;")) {
                    for (Item i : ann.items) {
                        if (i.name.equals("value")) {
                            Type t = (Type) i.value;
                            int d = className.lastIndexOf('$');
                            String innerName = className.substring(d + 1, className.length() - 1);
                            cv.visitInnerClass(className, t.toString(), innerName, access_flags);
                        }
                    }
                    continue;
                } else if (ann.type.equals("Ldalvik/annotation/InnerClass;")) {
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

    protected List<Ann> anns = new ArrayList<Ann>();
    Map<String, Integer> accessFlagsMap;

    /**
     * @param cv
     * @param access_flags
     * @param className
     * @param superClass
     * @param interfaceNames
     */
    public V3ClassAdapter(Map<String, Integer> accessFlagsMap, ClassVisitor cv, int access_flags, String className, String superClass, String[] interfaceNames) {
        super();
        this.accessFlagsMap = accessFlagsMap;
        this.cv = new TypeNameAdapter(cv);
        this.access_flags = access_flags;
        this.className = className;
        this.superClass = superClass;
        this.interfaceNames = interfaceNames;
    }

    public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
        Ann ann = new Ann(name, visitable);
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

    protected String file;

    public void visitSource(String file) {
        this.file = file;
    }

}