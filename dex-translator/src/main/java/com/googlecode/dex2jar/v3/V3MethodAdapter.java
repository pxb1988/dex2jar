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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Annotation.Item;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.asm.LdcOptimizeAdapter;
import com.googlecode.dex2jar.ir.ts.LocalRemove;
import com.googlecode.dex2jar.ir.ts.LocalSplit;
import com.googlecode.dex2jar.ir.ts.Transformer;
import com.googlecode.dex2jar.visitors.DexAnnotationAble;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class V3MethodAdapter implements DexMethodVisitor, Opcodes {
    final protected List<Annotation> anns = new ArrayList<Annotation>();
    final protected ClassVisitor cv;
    final protected Method method;
    final protected List<Annotation>[] paramAnns;
    final protected MethodNode methodNode = new MethodNode();
    private static final Logger log = LoggerFactory.getLogger(V3MethodAdapter.class);

    static {
        log.debug("InsnList.check=false");
        // Optimize Tree Analyzer
        InsnList.check = false;
    }

    /**
     * @param cv
     * @param method
     */
    @SuppressWarnings("unchecked")
    public V3MethodAdapter(ClassVisitor cv, Method method) {
        super();
        this.cv = cv;
        this.method = method;
        List<Annotation>[] paramAnns = new List[method.getType().getParameterTypes().length];
        for (int i = 0; i < paramAnns.length; i++) {
            paramAnns[i] = new ArrayList<Annotation>();
        }
        this.paramAnns = paramAnns;
        methodNode.tryCatchBlocks = new ArrayList<Object>();
    }

    private void build() {
        List<String> exceptions = new ArrayList<String>();
        String signature = null;
        for (Iterator<Annotation> it = anns.iterator(); it.hasNext();) {
            Annotation ann = it.next();
            if ("Ldalvik/annotation/Throws;".equals(ann.type)) {
                it.remove();
                for (Item item : ann.items) {
                    if (item.name.equals("value")) {
                        Annotation values = (Annotation) item.value;
                        for (Item i : values.items) {
                            exceptions.add(i.value.toString());
                        }
                    }
                }
            } else if ("Ldalvik/annotation/Signature;".equals(ann.type)) {
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

        MethodNode methodNode = this.methodNode;
        methodNode.access = method.getAccessFlags();
        methodNode.name = method.getName();
        methodNode.desc = method.getType().getDesc();
        methodNode.signature = signature;
        methodNode.exceptions = exceptions;
        for (Annotation ann : anns) {
            AnnotationVisitor av = methodNode.visitAnnotation(ann.type, ann.visible);
            V3AnnAdapter.accept(ann.items, av);
            av.visitEnd();
        }

        for (int i = 0; i < paramAnns.length; i++) {
            for (Annotation ann : paramAnns[i]) {
                AnnotationVisitor av = methodNode.visitParameterAnnotation(i, ann.type, ann.visible);
                V3AnnAdapter.accept(ann.items, av);
                av.visitEnd();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitAnnotation(java.lang .String, boolean)
     */
    public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
        Annotation ann = new Annotation(name, visitable);
        anns.add(ann);
        return new V3AnnAdapter(ann);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitCode()
     */
    public DexCodeVisitor visitCode() {
        return new V3CodeAdapter(method) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                if (irMethod.stmts.getSize() > 1) {
                    for (Transformer ts : new Transformer[] { new LocalSplit(), new LocalRemove() }) {
                        ts.transform(irMethod);
                    }
                }
                // convert irMethod to methodNode;
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitEnd()
     */
    @SuppressWarnings("unchecked")
    public void visitEnd() {
        build();
        MethodNode methodNode = this.methodNode;
        MethodVisitor mv = cv.visitMethod(methodNode.access, methodNode.name, methodNode.desc, methodNode.signature,
                (String[]) methodNode.exceptions.toArray(new String[methodNode.exceptions.size()]));
        if (mv != null) {
            try {
                methodNode.accept(new LdcOptimizeAdapter(mv));
            } catch (Exception e) {
                throw new RuntimeException("Error visit method:" + this.method, e);
            }
        }
    }

    void dump(MethodNode methodNode) {
        TraceMethodVisitor tmv = new TraceMethodVisitor();
        methodNode.instructions.accept(tmv);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Object o : tmv.text) {
            sb.append(i++).append(o);
        }
        System.out.println(sb);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitParamesterAnnotation (int)
     */
    public DexAnnotationAble visitParamesterAnnotation(int index) {
        final List<Annotation> panns = paramAnns[index];
        return new DexAnnotationAble() {
            public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
                Annotation ann = new Annotation(name, visitable);
                panns.add(ann);
                return new V3AnnAdapter(ann);
            }
        };
    }

}
