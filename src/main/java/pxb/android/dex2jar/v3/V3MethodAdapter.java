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
package pxb.android.dex2jar.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.dex2jar.Method;
import pxb.android.dex2jar.optimize.B;
import pxb.android.dex2jar.optimize.C;
import pxb.android.dex2jar.optimize.LdcOptimizeAdapter;
import pxb.android.dex2jar.optimize.MethodTransformer;
import pxb.android.dex2jar.v3.Ann.Item;
import pxb.android.dex2jar.visitors.DexAnnotationAble;
import pxb.android.dex2jar.visitors.DexCodeVisitor;
import pxb.android.dex2jar.visitors.DexMethodVisitor;

/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class V3MethodAdapter implements DexMethodVisitor, Opcodes {
    final protected List<Ann> anns = new ArrayList<Ann>();
    final protected ClassVisitor cv;
    final protected Method method;
    final protected List<Ann>[] paramAnns;
    final protected MethodNode methodNode = new MethodNode();
    private static final Logger log = LoggerFactory.getLogger(V3MethodAdapter.class);

    static {
        log.debug("InsnList.check=false");
        // Optmize Tree Analyzer
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
        List<Ann>[] paramAnns = new List[method.getType().getParameterTypes().length];
        for (int i = 0; i < paramAnns.length; i++) {
            paramAnns[i] = new ArrayList<Ann>();
        }
        this.paramAnns = paramAnns;
        methodNode.tryCatchBlocks = new ArrayList<Object>();
    }

    private void build() {
        List<String> exceptions = new ArrayList<String>();
        String signature = null;
        for (Iterator<Ann> it = anns.iterator(); it.hasNext();) {
            Ann ann = it.next();
            if ("Ldalvik/annotation/Throws;".equals(ann.type)) {
                it.remove();
                for (Item item : ann.items) {
                    if (item.name.equals("value")) {
                        Ann values = (Ann) item.value;
                        for (Item i : values.items) {
                            exceptions.add(i.value.toString());
                        }
                    }
                }
            } else if ("Ldalvik/annotation/Signature;".equals(ann.type)) {
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

        MethodNode methodNode = this.methodNode;
        methodNode.access = method.getAccessFlags();
        methodNode.name = method.getName();
        methodNode.desc = method.getType().getDesc();
        methodNode.signature = signature;
        methodNode.exceptions = exceptions;
        for (Ann ann : anns) {
            AnnotationVisitor av = methodNode.visitAnnotation(ann.type, ann.visible);
            V3AnnAdapter.accept(ann.items, av);
            av.visitEnd();
        }

        for (int i = 0; i < paramAnns.length; i++) {
            for (Ann ann : paramAnns[i]) {
                AnnotationVisitor av = methodNode.visitParameterAnnotation(i, ann.type, ann.visible);
                V3AnnAdapter.accept(ann.items, av);
                av.visitEnd();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitAnnotation(java.lang .String, boolean)
     */
    public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
        Ann ann = new Ann(name, visitable);
        anns.add(ann);
        return new V3AnnAdapter(ann);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitCode()
     */
    public DexCodeVisitor visitCode() {
        return new V3CodeAdapter(methodNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitEnd()
     */
    @SuppressWarnings("unchecked")
    public void visitEnd() {
        build();
        MethodNode methodNode = this.methodNode;

        try {
            if (methodNode.instructions.size() > 2) {
                List<? extends MethodTransformer> trs = Arrays.asList(new B(), new C(method));
                for (MethodTransformer tr : trs) {
                    // TraceMethodVisitor tmv = new TraceMethodVisitor();
                    // methodNode.instructions.accept(tmv);
                    // StringBuilder sb=new StringBuilder();
                    // int i=0;
                    // for(Object o:tmv.text){
                    // sb.append(i++).append(o);
                    // }
                    // System.out.println(sb);
                    tr.transform(methodNode);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error transform method:" + this.method, e);
        }

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

    /*
     * (non-Javadoc)
     * 
     * @see pxb.android.dex2jar.visitors.DexMethodVisitor#visitParamesterAnnotation (int)
     */
    public DexAnnotationAble visitParamesterAnnotation(int index) {
        final List<Ann> panns = paramAnns[index];
        return new DexAnnotationAble() {
            public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
                Ann ann = new Ann(name, visitable);
                panns.add(ann);
                return new V3AnnAdapter(ann);
            }
        };
    }

}
