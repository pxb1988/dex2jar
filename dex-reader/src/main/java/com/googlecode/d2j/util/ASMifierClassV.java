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
package com.googlecode.d2j.util;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class ASMifierClassV extends DexClassVisitor {
    protected ArrayOut out = new ArrayOut();
    private List<ArrayOut> methodOuts = new ArrayList<ArrayOut>();
    private List<ArrayOut> fieldOuts = new ArrayList<ArrayOut>();

    int fCount = 0;
    int mCount = 0;

    public ASMifierClassV(String pkgName, String javaClassName, int access_flags, String className, String superClass,
            String[] interfaceNames) {
        super();
        out.s("package %s;", pkgName);
        out.s("import com.googlecode.d2j.*;");
        out.s("import com.googlecode.d2j.visitors.*;");
        out.s("import static com.googlecode.d2j.DexConstants.*;");
        out.s("import static com.googlecode.d2j.reader.Op.*;");
        out.s("public class %s {", javaClassName);
        out.push();
        out.s("public static void accept(DexFileVisitor v) {");
        out.push();
        out.s("DexClassVisitor cv=v.visit(%s,%s,%s,%s);", Escape.classAcc(access_flags), Escape.v(className),
                Escape.v(superClass), Escape.v(interfaceNames));
        out.s("if(cv!=null) {");
        out.push();
        out.s("accept(cv);");
        out.s("cv.visitEnd();");
        out.pop();
        out.s("}");
        out.pop();
        out.s("}");
        out.s("public static void accept(DexClassVisitor cv) {");
        out.push();
    }

    @Override
    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
        return new ASMifierAnnotationV("cv", out, name, visibility);
    }

    @Override
    public void visitSource(String file) {
        out.s("cv.visitSource(\"%s\");", Utf8Utils.escapeString(file));
    }

    @Override
    public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
        String fieldName = String.format("f%03d_%s", fCount++, field.getName());
        out.s("%s(cv);", fieldName);

        final ArrayOut f = new ArrayOut();
        fieldOuts.add(f);
        f.s("public static void %s(DexClassVisitor cv) {", fieldName);
        f.push();
        f.s("DexFieldVisitor fv=cv.visitField(%s, %s, %s);", Escape.fieldAcc(accessFlags), Escape.v(field),
                Escape.v(value));
        f.s("if(fv != null) {");
        f.push();
        return new DexFieldVisitor() {

            @Override
            public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
                return new ASMifierAnnotationV("fv", f, name, visibility);
            }

            @Override
            public void visitEnd() {
                f.s("fv.visitEnd();");
                f.pop();
                f.s("}");
                f.pop();
                f.s("}");
            }
        };
    }

    @Override
    public DexMethodVisitor visitMethod(int accessFlags, Method method) {
        String methodName = String.format("m%03d_%s", mCount++, method.getName().replace('<', '_').replace('>', '_'));
        out.s("%s(cv);", methodName);

        final ArrayOut m = new ArrayOut();
        methodOuts.add(m);
        m.s("public static void %s(DexClassVisitor cv) {", methodName);
        m.push();
        m.s("DexMethodVisitor mv=cv.visitMethod(%s, %s);", Escape.methodAcc(accessFlags), Escape.v(method));
        m.s("if(mv != null) {");
        m.push();

        return new DexMethodVisitor() {

            @Override
            public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
                return new ASMifierAnnotationV("mv", m, name, visibility);
            }

            @Override
            public DexAnnotationAble visitParameterAnnotation(final int index) {
                m.s("DexAnnotationAble pv%02d = mv.visitParameterAnnotation(%s);", index, index);
                return new DexAnnotationAble() {

                    @Override
                    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
                        return new ASMifierAnnotationV(String.format("pv%02d", index), m, name, visibility);
                    }
                };
            }

            @Override
            public DexCodeVisitor visitCode() {
                m.s("DexCodeVisitor code=mv.visitCode();");
                m.s("if(code != null) {");
                m.push();
                return new ASMifierCodeV(m) {

                    @Override
                    public void visitEnd() {
                        super.visitEnd();

                        m.pop();
                        m.s("}");
                    }
                };
            }

            @Override
            public void visitEnd() {
                m.s("mv.visitEnd();");
                m.pop();
                m.s("}");
                m.pop();
                m.s("}");
            }

        };
    }

    @Override
    public void visitEnd() {
        out.pop();
        out.s("}");
        for (ArrayOut o : fieldOuts) {
            out.array.addAll(o.array);
            for (int i : o.is) {
                out.is.add(out.i + i);
            }
        }
        fieldOuts = null;
        for (ArrayOut o : methodOuts) {
            out.array.addAll(o.array);
            for (int i : o.is) {
                out.is.add(out.i + i);
            }
        }
        methodOuts = null;
        out.pop();
        out.s("}");
    }

}
