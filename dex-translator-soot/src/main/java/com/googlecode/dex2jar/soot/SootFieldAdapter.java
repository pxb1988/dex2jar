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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import com.googlecode.dex2jar.Annotation;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Annotation.Item;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;


/**
 * @author Panxiaobo [pxb1988@gmail.com]
 * @version $Id$
 */
public class SootFieldAdapter implements DexFieldVisitor {
    protected List<Annotation> anns = new ArrayList<Annotation>();
    protected boolean build = false;
    protected ClassVisitor cv;
    protected Field field;
    protected FieldVisitor fv;
    protected Object value;

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
            FieldVisitor fv = cv.visitField(field.getAccessFlags(), field.getName(), field.getType(), signature, value);
            if (fv != null) {
                for (Annotation ann : anns) {
                    AnnotationVisitor av = fv.visitAnnotation(ann.type, ann.visible);
                    SootAnnAdapter.accept(ann.items, av);
                    av.visitEnd();
                }
            }
            this.fv = fv;
            build = true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFieldVisitor#visitAnnotation(java.lang .String, boolean)
     */
    public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
        Annotation ann = new Annotation(name, visitable);
        anns.add(ann);
        return new SootAnnAdapter(ann);
    }

    /**
     * @param cv
     * @param field
     */
    public SootFieldAdapter(ClassVisitor cv, Field field, Object value) {
        super();
        this.cv = cv;
        this.field = field;
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexFieldVisitor#visitEnd()
     */
    public void visitEnd() {
        build();
        if (fv != null)
            fv.visitEnd();
    }

}
