/*
 *  dex2jar - A tool for converting Android's .dex format to Java's .class format
 *  Copyright (c) 2009-2011 Panxiaobo
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package com.googlecode.dex2jar.soot;

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
public class SootAnnAdapter implements AnnotationVisitor {

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
    public SootAnnAdapter(Annotation ann) {
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
        return new SootAnnAdapter(ann);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang .String)
     */
    public AnnotationVisitor visitArray(String name) {
        Annotation ann = new Annotation(null, true);
        this.ann.items.add(new Item(name, ann));
        return new SootAnnAdapter(ann);
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
