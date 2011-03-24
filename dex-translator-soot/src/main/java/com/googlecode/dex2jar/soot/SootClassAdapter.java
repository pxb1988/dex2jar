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
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.PackManager;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.tagkit.DoubleConstantValueTag;
import soot.tagkit.FloatConstantValueTag;
import soot.tagkit.InnerClassTag;
import soot.tagkit.IntegerConstantValueTag;
import soot.tagkit.LongConstantValueTag;
import soot.tagkit.SignatureTag;

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
public class SootClassAdapter implements DexClassVisitor {

    private static final Logger log = LoggerFactory.getLogger(SootClassAdapter.class);
    protected Map<String, Integer> accessFlagsMap;
    protected List<Annotation> anns = new ArrayList<Annotation>();
    protected String file;
    protected SootClass sootClass;

    public SootClassAdapter(Map<String, Integer> accessFlagsMap, SootClass sootClass) {
        super();
        this.accessFlagsMap = accessFlagsMap;
        this.sootClass = sootClass;
    }

    // FIXME
    protected void build() {
        for (Iterator<Annotation> it = anns.iterator(); it.hasNext();) {
            Annotation ann = it.next();
            it.remove();
            if ("Ldalvik/annotation/Signature;".equals(ann.type)) {
                for (Item item : ann.items) {
                    if (item.name.equals("value")) {
                        Annotation values = (Annotation) item.value;
                        StringBuilder sb = new StringBuilder();
                        for (Item i : values.items) {
                            sb.append(i.value.toString());
                        }
                        sootClass.addTag(new SignatureTag(sb.toString()));
                    }
                }
                continue;
            } else if (ann.type.equals("Ldalvik/annotation/MemberClasses;")) {
                for (Item i : ann.items) {
                    if (i.name.equals("value")) {
                        for (Item j : ((Annotation) i.value).items) {
                            String name = j.value.toString();
                            Integer access = accessFlagsMap.get(name);
                            int d = name.lastIndexOf('$');
                            String innerName = name.substring(d + 1, name.length() - 1);
                            // TODO 设置默认内部类修饰符
                            sootClass.addTag(new InnerClassTag(Type.getType(name).getClassName(), sootClass.getName(),
                                    innerName, access == null ? 0 : access));
                        }
                    }
                }
                continue;
            } else if (ann.type.equals("Ldalvik/annotation/EnclosingClass;")) {
                for (Item i : ann.items) {
                    if (i.name.equals("value")) {
                        Type t = (Type) i.value;
                        String className = sootClass.getName();
                        int d = className.lastIndexOf('$');
                        String innerName = className.substring(d + 1, className.length() - 1);
                        // FIXME
                        // cv.visitInnerClass(className, t.toString(), innerName, access_flags);
                    }
                }
                continue;
            } else if (ann.type.equals("Ldalvik/annotation/InnerClass;")) {
                continue;
            } else if (ann.type.equals("Ldalvik/annotation/EnclosingMethod;")) {
                continue;
            }
            // AnnotationVisitor av = cv.visitAnnotation(ann.type, ann.visible);
            // SootAnnAdapter.accept(ann.items, av);
            // av.visitEnd();

        }
    }

    public AnnotationVisitor visitAnnotation(String name, boolean visitable) {
        Annotation ann = new Annotation(name, visitable);
        anns.add(ann);
        return new SootAnnAdapter(ann);
    }

    public void visitEnd() {
        build();
        try {
            PackManager.v().writeClass(sootClass);
        } catch (Exception e) {
            log.error("Error while wrting class", e);
        }
    }

    public DexFieldVisitor visitField(Field field, Object value) {
        build();
        SootField f = new SootField(field.getName(), RefType.v(Type.getType(field.getType()).getClassName()),
                field.getAccessFlags());
        sootClass.addField(f);
        if (value != null) {
            switch (Type.getType(field.getType()).getSort()) {

            case Type.BOOLEAN:
                f.addTag(new IntegerConstantValueTag(((Boolean) value) ? 1 : 0));
                break;
            case Type.BYTE:
                f.addTag(new IntegerConstantValueTag(((Byte) value)));
                break;
            case Type.CHAR:
                f.addTag(new IntegerConstantValueTag(((Character) value)));
                break;
            case Type.DOUBLE:
                f.addTag(new DoubleConstantValueTag(((Double) value)));
                break;
            case Type.FLOAT:
                f.addTag(new FloatConstantValueTag(((Float) value)));
                break;
            case Type.INT:
                f.addTag(new IntegerConstantValueTag(((Integer) value)));
                break;
            case Type.LONG:
                f.addTag(new LongConstantValueTag(((Long) value)));
                break;
            case Type.SHORT:
                f.addTag(new IntegerConstantValueTag(((Short) value)));
                break;
            case Type.ARRAY:
            case Type.OBJECT:
            case Type.VOID:
                // FIXME
            }
        }
        return null;
    }

    public DexMethodVisitor visitMethod(Method method) {
        build();
        return new SootMethodAdapter(method, sootClass);
    }

    public void visitSource(String file) {
        this.file = file;
    }

}