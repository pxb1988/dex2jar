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
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;

/**
 * 
 * @author Bob Pan
 * 
 */
public class DexAnnotationNode extends DexAnnotationVisitor {
    private abstract static class AV extends DexAnnotationVisitor {
        List<Object> objs = new ArrayList<Object>();

        @Override
        public void visit(String name, Object value) {
            objs.add(value);
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(String name, String desc) {
            DexAnnotationNode annotation = new DexAnnotationNode(desc, Visibility.RUNTIME);
            objs.add(annotation);
            return annotation;
        }

        @Override
        public DexAnnotationVisitor visitArray(String name) {
            return new AV() {

                @Override
                public void visitEnd() {
                    AV.this.objs.add(this.objs.toArray());
                    super.visitEnd();
                }

            };
        }

        @Override
        public void visitEnd() {
            objs = null;
            super.visitEnd();
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            objs.add(new Field(null, value, desc));
        }
    }

    public static class Item {
        public String name;

        public Object value;

        public Item(String name, Object value) {
            super();
            this.name = name;
            this.value = value;
        }
    }

    public static void acceptAnnotationItem(DexAnnotationVisitor dav, String name, Object o) {
        if (o instanceof Object[]) {
            DexAnnotationVisitor arrayVisitor = dav.visitArray(name);
            if (arrayVisitor != null) {
                Object[] array = (Object[]) o;
                for (Object e : array) {
                    acceptAnnotationItem(arrayVisitor, null, e);
                }
                arrayVisitor.visitEnd();
            }
        } else if (o instanceof DexAnnotationNode) {
            DexAnnotationNode ann = (DexAnnotationNode) o;
            DexAnnotationVisitor av = dav.visitAnnotation(name, ann.type);
            if (av != null) {
                for (DexAnnotationNode.Item item : ann.items) {
                    acceptAnnotationItem(av, item.name, item.value);
                }
                av.visitEnd();
            }
        } else if (o instanceof Field) {
            Field f = (Field) o;
            dav.visitEnum(name, f.getType(), f.getName());
        } else {
            dav.visit(name, o);
        }
    }

    public List<Item> items = new ArrayList<Item>(5);

    public String type;
    public Visibility visibility;

    public DexAnnotationNode(String type, Visibility visibility) {
        super();
        this.type = type;
        this.visibility = visibility;
    }

    public void accept(DexAnnotationAble av) {
        DexAnnotationVisitor av1 = av.visitAnnotation(type, visibility);
        if (av != null) {
            for (Item item : items) {
                acceptAnnotationItem(av1, item.name, item.value);
            }
            av1.visitEnd();
        }
    }

    @Override
    public void visit(String name, Object value) {
        items.add(new Item(name, value));
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {
        DexAnnotationNode annotation = new DexAnnotationNode(desc, Visibility.RUNTIME);
        items.add(new Item(name, annotation));
        return annotation;
    }

    @Override
    public DexAnnotationVisitor visitArray(final String name) {
        return new AV() {

            @Override
            public void visitEnd() {
                items.add(new Item(name, objs.toArray()));
                super.visitEnd();
            }
        };
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        items.add(new Item(name, new Field(desc, value, desc)));
    }
}