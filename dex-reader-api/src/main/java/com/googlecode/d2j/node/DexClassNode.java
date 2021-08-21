package com.googlecode.d2j.node;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bob Pan
 */
public class DexClassNode extends DexClassVisitor {

    public int access;

    public List<DexAnnotationNode> anns;

    public String className;

    public List<DexFieldNode> fields;

    public String[] interfaceNames;

    public List<DexMethodNode> methods;

    public String source;

    public String superClass;

    public DexClassNode(DexClassVisitor v, int access, String className, String superClass, String[] interfaceNames) {
        super(v);
        this.access = access;
        this.className = className;
        this.superClass = superClass;
        this.interfaceNames = interfaceNames;
    }

    public DexClassNode(int access, String className, String superClass, String[] interfaceNames) {
        super();
        this.access = access;
        this.className = className;
        this.superClass = superClass;
        this.interfaceNames = interfaceNames;
    }

    public void accept(DexClassVisitor dcv) {
        if (anns != null) {
            for (DexAnnotationNode ann : anns) {
                ann.accept(dcv);
            }
        }
        if (methods != null) {
            for (DexMethodNode m : methods) {
                m.accept(dcv);
            }
        }
        if (fields != null) {
            for (DexFieldNode f : fields) {
                f.accept(dcv);
            }
        }
        if (source != null) {
            dcv.visitSource(source);
        }
    }

    public void accept(DexFileVisitor dfv) {
        DexClassVisitor dcv = dfv.visit(access, className, superClass, interfaceNames);
        if (dcv != null) {
            accept(dcv);
            dcv.visitEnd();
        }
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
        if (name.equals("Lkotlin/Metadata;")) {
            return null; // clean kotlin metadata
        }
        if (anns == null) {
            anns = new ArrayList<>(5);
        }
        DexAnnotationNode annotation = new DexAnnotationNode(name, visibility);
        anns.add(annotation);
        return annotation;
    }

    @Override
    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        DexFieldNode fieldNode = new DexFieldNode(accessFlags, field, value);
        fields.add(fieldNode);
        return fieldNode;
    }

    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        if (methods == null) {
            methods = new ArrayList<>();
        }
        DexMethodNode methodNode = new DexMethodNode(accessFlags, method);
        methods.add(methodNode);
        return methodNode;
    }

    @Override
    public void visitSource(String file) {
        this.source = file;
    }

}
