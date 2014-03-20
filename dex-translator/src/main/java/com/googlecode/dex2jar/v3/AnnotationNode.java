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
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

import com.googlecode.dex2jar.DexType;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexAnnotationAble;
import com.googlecode.dex2jar.visitors.DexAnnotationVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class AnnotationNode implements DexAnnotationVisitor {
    public static class Item {
        public String name;

        public Object value;

        /**
         * @param name
         * @param value
         */
        public Item(String name, Object value) {
            super();
            this.name = name;
            this.value = value;
        }
    }

    public String type;
    public boolean visible;
    protected List<Item> items = new ArrayList<Item>(5);

    @SuppressWarnings("unchecked")
    public static void accept(String name, Object v, AnnotationVisitor av) {
        if (v instanceof AnnotationNode) {
            AnnotationNode a = (AnnotationNode) v;
            AnnotationVisitor av1 = av.visitAnnotation(name, a.type);
            accept(a.items, av1);
            av1.visitEnd();
        } else if (v instanceof Field) {
            Field e = (Field) v;
            av.visitEnum(name, e.getOwner(), e.getName());
        } else if (v instanceof List) {
            List<Object> list = (List<Object>) v;
            AnnotationVisitor av1 = av.visitArray(name);
            for (Object i : list) {
                accept(null, i, av1);
            }
            av1.visitEnd();
        } else if (v instanceof Method) {
            // Method method = (Method) v;
            // AnnotationVisitor av1 = av.visitAnnotation(item.name, "Lcom.googlecode.Method;");
            // av1.visit("owner", method.getOwner());
            // av1.visit("name", method.getName());
            // av1.visit("desc", method.getType().getDesc());
            // av1.visitEnd();
            // av.visit(name, v);
            System.err.println("WARN: ignored method annotation value");
        } else if (v instanceof DexType) {
            // fix issue 222 the type is translated to string in annotation
            av.visit(name, Type.getType(((DexType) v).desc));
        } else {
            if (v == null) {
                System.err.println("WARN: ignored null annotation value");
            } else {
                av.visit(name, v);
            }
        }
    }

    private static void accept(List<Item> items, AnnotationVisitor av) {
        for (Item item : items) {
            accept(item.name, item.value, av);
        }
    }

    @SuppressWarnings("unchecked")
    public static void accept(String name, Object v, DexAnnotationVisitor av) {
        if (v instanceof AnnotationNode) {
            AnnotationNode a = (AnnotationNode) v;
            DexAnnotationVisitor av1 = av.visitAnnotation(name, a.type);
            accept(a.items, av1);
            av1.visitEnd();
        } else if (v instanceof Field) {
            Field e = (Field) v;
            av.visitEnum(name, e.getOwner(), e.getName());
        } else if (v instanceof List) {
            List<Object> list = (List<Object>) v;
            DexAnnotationVisitor av1 = av.visitArray(name);
            for (Object i : list) {
                accept(null, i, av1);
            }
            av1.visitEnd();
        } else if (v instanceof Field) {
            Field e = (Field) v;
            av.visitEnum(name, e.getOwner(), e.getName());
        } else if (v instanceof Method) {
            // Method method = (Method) v;
            // AnnotationVisitor av1 = av.visitAnnotation(item.name, "Lcom.googlecode.Method;");
            // av1.visit("owner", method.getOwner());
            // av1.visit("name", method.getName());
            // av1.visit("desc", method.getType().getDesc());
            // av1.visitEnd();
            av.visit(name, v);
        } else {
            av.visit(name, v);
        }
    }

    private static void accept(List<Item> items, DexAnnotationVisitor av) {
        for (Item item : items) {
            accept(item.name, item.value, av);
        }
    }

    public AnnotationNode() {
        super();
    }

    public AnnotationNode(String type, boolean visible) {
        super();
        this.type = type;
        this.visible = visible;
    }

    public void accept(DexAnnotationAble a) {
        DexAnnotationVisitor av = a.visitAnnotation(type, visible);
        accept(items, av);
        av.visitEnd();
    }

    public void accept(DexAnnotationVisitor av) {
        accept(items, av);
    }

    public void accept(FieldVisitor fv) {
        AnnotationVisitor av = fv.visitAnnotation(type, visible);
        accept(items, av);
        av.visitEnd();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visit(java.lang.String, java.lang.Object)
     */
    @Override
    public void visit(String name, Object value) {
        items.add(new Item(name, value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitAnnotation(java .lang.String, java.lang.String)
     */
    @Override
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {
        AnnotationNode ann = new AnnotationNode(desc, true);
        items.add(new Item(name, ann));
        return ann;
    }

    private static class ArrayV implements DexAnnotationVisitor {
        List<Object> list;

        public ArrayV(List<Object> list2) {
            this.list = list2;
        }

        @Override
        public void visit(String name, Object value) {
            list.add(value);
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            list.add(new Field(desc, value, null));
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(String name, String desc) {
            AnnotationNode node = new AnnotationNode(desc, true);
            list.add(node);
            return node;
        }

        @Override
        public DexAnnotationVisitor visitArray(String name) {
            List<Object> list = new ArrayList<Object>(5);
            list.add(list);
            return new ArrayV(list);
        }

        @Override
        public void visitEnd() {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitArray(java.lang .String)
     */
    @Override
    public DexAnnotationVisitor visitArray(String name) {
        List<Object> list = new ArrayList<Object>(5);
        items.add(new Item(name, list));
        return new ArrayV(list);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationVisitor#visitEnum(java.lang .String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void visitEnum(String name, String desc, String value) {
        items.add(new Item(name, new Field(desc, value, null)));
    }

    public void accept(ClassVisitor cv) {
        AnnotationVisitor av = cv.visitAnnotation(type, visible);
        accept(items, av);
        av.visitEnd();

    }

}
