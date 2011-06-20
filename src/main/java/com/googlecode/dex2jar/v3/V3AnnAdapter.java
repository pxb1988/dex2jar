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

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.Annotation.Item;


/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class V3AnnAdapter implements AnnotationVisitor {

    protected Annotation ann;

    public static void accept(List<Item> items, AnnotationVisitor av) {
        if (av == null)
            return;
        for (Item item : items) {
            Object v = item.value;
            if (v instanceof Annotation) {
                Annotation a = (Annotation) v;
                if (a.type != null) {
                    AnnotationVisitor av1 = av.visitAnnotation(item.name, a.type);
                    accept(a.items, av1);
                    av1.visitEnd();
                } else {// array
                    AnnotationVisitor av1 = av.visitArray(item.name);
                    accept(a.items, av1);
                    av1.visitEnd();
                }
            } else if (v instanceof Field) {
                Field e = (Field) v;
                av.visitEnum(item.name, e.getType(), e.getName());
            } else if (v instanceof Method) {
                // Method method = (Method) v;
                // AnnotationVisitor av1 = av.visitAnnotation(item.name, "Lcom.googlecode.Method;");
                // av1.visit("owner", method.getOwner());
                // av1.visit("name", method.getName());
                // av1.visit("desc", method.getType().getDesc());
                // av1.visitEnd();
                av.visit(item.name, v);
            } else {
                av.visit(item.name, v);
            }
        }
    }

    /**
     * @param ann
     */
    public V3AnnAdapter(Annotation ann) {
        super();
        this.ann = ann;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visit(java.lang.String, java.lang.Object)
     */
    public void visit(String name, Object value) {
        ann.items.add(new Item(name, value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitAnnotation(java .lang.String, java.lang.String)
     */
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        Annotation ann = new Annotation(desc, true);
        this.ann.items.add(new Item(name, ann));
        return new V3AnnAdapter(ann);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang .String)
     */
    public AnnotationVisitor visitArray(String name) {
        Annotation ann = new Annotation(null, true);
        this.ann.items.add(new Item(name, ann));
        return new V3AnnAdapter(ann);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitEnd()
     */
    public void visitEnd() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitEnum(java.lang .String, java.lang.String,
     * java.lang.String)
     */
    public void visitEnum(String name, String desc, String value) {
    }

}
