package com.googlecode.d2j.node;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob Pan
 */
public class DexAnnotationNode extends DexAnnotationVisitor {

    private abstract static class AV extends DexAnnotationVisitor {

        List<Object> objs = new ArrayList<>();

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

    public List<Item> items = new ArrayList<>(5);

    public String type;

    public Visibility visibility;

    public DexAnnotationNode(String type, Visibility visibility) {
        super();
        this.type = type;
        this.visibility = visibility;
    }

    public void accept(DexAnnotationAble av) {
        DexAnnotationVisitor av1 = av.visitAnnotation(type, visibility);
        if (av1 != null) {
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
