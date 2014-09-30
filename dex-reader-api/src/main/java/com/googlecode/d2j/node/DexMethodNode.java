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

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

public class DexMethodNode extends DexMethodVisitor {
    public int access;
    public List<DexAnnotationNode> anns;
    public DexCodeNode codeNode;
    public Method method;
    public List<DexAnnotationNode> parameterAnns[];

    public DexMethodNode(DexMethodVisitor mv, int access, Method method) {
        super(mv);
        this.access = access;
        this.method = method;
    }

    public DexMethodNode(int access, Method method) {
        super();
        this.access = access;
        this.method = method;
    }

    public void accept(DexClassVisitor dcv) {
        DexMethodVisitor mv = dcv.visitMethod(access, method);
        if (mv != null) {
            accept(mv);
            mv.visitEnd();
        }

    }

    public void accept(DexMethodVisitor mv) {
        if (anns != null) {
            for (DexAnnotationNode ann : anns) {
                ann.accept(mv);
            }
        }

        if (parameterAnns != null) {
            for (int i = 0; i < parameterAnns.length; i++) {
                List<DexAnnotationNode> ps = parameterAnns[i];
                if (ps != null) {
                    DexAnnotationAble av = mv.visitParameterAnnotation(i);
                    if (av != null) {
                        for (DexAnnotationNode p : ps) {
                            p.accept(av);
                        }
                    }
                }
            }
        }
        if (codeNode != null) {
            codeNode.accept(mv);
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
    public DexCodeVisitor visitCode() {
        DexCodeNode codeNode = new DexCodeNode(super.visitCode());
        this.codeNode = codeNode;
        return codeNode;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DexAnnotationAble visitParameterAnnotation(final int index) {
        if (parameterAnns == null) {
            parameterAnns = new List[method.getParameterTypes().length];
        }

        return new DexAnnotationAble() {

            @Override
            public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
                List<DexAnnotationNode> pas = parameterAnns[index];
                if (pas == null) {
                    pas = new ArrayList<DexAnnotationNode>(5);
                    parameterAnns[index] = pas;
                }
                DexAnnotationNode annotation = new DexAnnotationNode(name, visibility);
                pas.add(annotation);
                return annotation;
            }
        };
    }

}
