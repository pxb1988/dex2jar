/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2013 Panxiaobo
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
package com.googlecode.d2j.node;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

/**
 * @author Bob Pan
 * 
 */
public class DexClassNode extends DexClassVisitor {
    public int access;
    public List<DexAnnotationNode> anns;
    public String className;
    public List<DexFieldNode> fields;
    public String[] interfaceNames;
    public List<DexMethodNode> methods;
    public String source;
    public String superClass;

    public DexClassNode(DexClassVisitor v, int access, String className, String superClass, String[] interfaceNames) {
        super(v);
        this.access = access;
        this.className = className;
        this.superClass = superClass;
        this.interfaceNames = interfaceNames;
    }

    public DexClassNode(int access, String className, String superClass, String[] interfaceNames) {
        super();
        this.access = access;
        this.className = className;
        this.superClass = superClass;
        this.interfaceNames = interfaceNames;
    }

    public void accept(DexClassVisitor dcv) {
        if (anns != null) {
            for (DexAnnotationNode ann : anns) {
                ann.accept(dcv);
            }
        }
        if (methods != null) {
            for (DexMethodNode m : methods) {
                m.accept(dcv);
            }
        }
        if (fields != null) {
            for (DexFieldNode f : fields) {
                f.accept(dcv);
            }
        }
        if (source != null) {
            dcv.visitSource(source);
        }
    }

    public void accept(DexFileVisitor dfv) {
        DexClassVisitor dcv = dfv.visit(access, className, superClass, interfaceNames);
        if (dcv != null) {
            accept(dcv);
            dcv.visitEnd();
        }
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
        if (anns == null) {
            anns = new ArrayList<DexAnnotationNode>(5);
        }
        DexAnnotationNode annotation = new DexAnnotationNode(name, visibility);
        anns.add(annotation);
        return annotation;
    }

    @Override
    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
        if (fields == null) {
            fields = new ArrayList<DexFieldNode>();
        }
        DexFieldNode fieldNode = new DexFieldNode(accessFlags, field, value);
        fields.add(fieldNode);
        return fieldNode;
    }

    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        if (methods == null) {
            methods = new ArrayList<DexMethodNode>();
        }
        DexMethodNode methodNode = new DexMethodNode(accessFlags, method);
        methods.add(methodNode);
        return methodNode;
    }

    @Override
    public void visitSource(String file) {
        this.source = file;
    }

}
