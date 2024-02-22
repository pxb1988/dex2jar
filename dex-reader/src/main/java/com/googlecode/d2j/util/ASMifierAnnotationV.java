package com.googlecode.d2j.util;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class ASMifierAnnotationV extends DexAnnotationVisitor implements DexConstants {

    ArrayOut out;

    int i = 0;

    public ASMifierAnnotationV(String objName, ArrayOut out, String name, Visibility visibility) {
        this.out = out;
        out.s("if(%s!=null){", objName);
        out.push();

        out.s("DexAnnotationVisitor av%02d = %s.visitAnnotation(%s, Visibility.%s);", i, objName, Escape.v(name),
                visibility.name());
        out.s("if(av%02d != null) {", i);
        out.push();
    }

    @Override
    public void visit(String name, Object value) {
        out.s("av%02d.visit(%s, %s);", i, Escape.v(name), Escape.v(value));
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        out.s("av%02d.visitEnum(%s, %s, %s);", i, Escape.v(name), Escape.v(desc), Escape.v(value));
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, String desc) {
        out.s("{");
        out.push();
        int old = i;
        int n = ++i;
        out.s("DexAnnotationVisitor av%02d = av%02d.visitAnnotation(%s, %s);", n, old, Escape.v(name), Escape.v(desc));
        out.s("if(av%02d != null) {", i);
        out.push();
        return this;
    }

    @Override
    public DexAnnotationVisitor visitArray(String name) {
        out.s("{");
        out.push();
        int old = i;
        int n = ++i;
        out.s("DexAnnotationVisitor av%02d = av%02d.visitArray(%s);", n, old, Escape.v(name));
        out.s("if(av%02d != null) {", i);
        out.push();
        return this;
    }

    @Override
    public void visitEnd() {
        out.s("av%02d.visitEnd();", i);
        i--;
        out.pop();
        out.s("}");
        out.pop();
        out.s("}");
    }

}
