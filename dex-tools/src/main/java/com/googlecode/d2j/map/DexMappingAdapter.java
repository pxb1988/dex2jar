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
package com.googlecode.d2j.map;

import com.googlecode.d2j.*;
import com.googlecode.d2j.node.DexAnnotationNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.util.Mapper;
import com.googlecode.d2j.util.Types;
import com.googlecode.d2j.visitors.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureWriter;

public class DexMappingAdapter extends DexFileVisitor {
    protected Mapper mapper;

    public DexMappingAdapter(Mapper mapper, DexFileVisitor visitor) {
        super(visitor);
        this.mapper = mapper;
    }

    static String mapClassName(Mapper map, String oldDesc) {
        if (oldDesc == null) {
            return null;
        }
        int count = 0;
        while (count < oldDesc.length() && oldDesc.charAt(count) == '[') {
            count++;
        }
        if (count + 1 == oldDesc.length()) {// simple type
            return oldDesc;
        }
        if (count > 0) {
            String n = map.mapClassName(oldDesc.substring(count));
            if (n == null) {
                return oldDesc;
            }
            return oldDesc.substring(0, count) + n;
        } else {
            String n = map.mapClassName(oldDesc);
            if (n == null) {
                return oldDesc;
            }
            return n;
        }
    }

    static String mapAnnotationElementName(Mapper map, String owner, String name) {
        String n = map.mapMethodName(owner, name, null, null);
        if (n == null) {
            return name;
        }
        return n;
    }

    static String[] mapClassNames(Mapper map, String[] oldDescs) {
        if (oldDescs != null && oldDescs.length > 0) {
            String copy[] = new String[oldDescs.length];
            for (int i = 0; i < oldDescs.length; i++) {
                copy[i] = mapClassName(map, oldDescs[i]);
            }
            return copy;
        } else {
            return oldDescs;
        }
    }

    static String mapFieldName(Mapper map, String owner, String name, String type) {
        String n = map.mapFieldName(owner, name, type);
        if (n == null) {
            return name;
        }
        return n;
    }

    static Field mapField(Mapper map, Field f) {
        String n = map.mapFieldName(f.getOwner(), f.getName(), f.getType());
        if (n == null) {
            n = f.getName();
        }
        return new Field(mapClassName(map, f.getOwner()), n, mapClassName(map, f.getType()));
    }

    static Field mapFieldNameAndOwner(Mapper map, Field f) {
        String n = map.mapFieldName(f.getOwner(), f.getName(), f.getType());
        if (n == null) {
            n = f.getName();
        }
        return new Field(mapFieldOwner(map, f.getOwner(), f.getName(), f.getType()), n, mapClassName(map, f.getType()));
    }

    private static String mapFieldOwner(Mapper map, String owner, String name, String type) {
        String n = map.mapFieldOwner(owner, name, type);
        if (n == null) {
            return owner;
        }
        return n;
    }

    private static Method mapMethod(Mapper map, Method m) {
        String n = map.mapMethodName(m.getOwner(), m.getName(), m.getParameterTypes(), m.getReturnType());
        if (n == null) {
            n = m.getName();
        }
        return new Method(mapClassName(map, m.getOwner()), n, mapClassNames(map, m.getParameterTypes()), mapClassName(
                map, m.getReturnType()));
    }

    private static Method mapMethodNameAndOwner(Mapper map, Method m) {
        String n = map.mapMethodName(m.getOwner(), m.getName(), m.getParameterTypes(), m.getReturnType());
        if (n == null) {
            n = m.getName();
        }
        String owner = map.mapMethodOwner(m.getOwner(), m.getName(), m.getParameterTypes(), m.getReturnType());
        if (owner == null) {
            owner = m.getOwner();
        }
        return new Method(owner, n, mapClassNames(map, m.getParameterTypes()), mapClassName(map, m.getReturnType()));
    }

    static Object mapObject(Mapper map, Object value) {
        if (value instanceof DexType) {
            value = new DexType(mapClassName(map, ((DexType) value).desc));
        } else if (value instanceof Field) {
            Field f = (Field) value;
            value = mapField(map, f);
        } else if (value instanceof Method) {
            Method m = (Method) value;
            value = mapMethod(map, m);
        }
        return value;
    }

    @Override
    public DexClassVisitor visit(int access_flags, final String className, String superClass, String[] interfaceNames) {
        DexClassVisitor dcv = super.visit(access_flags, mapClassName(mapper, className),
                mapClassName(mapper, superClass), mapClassNames(mapper, interfaceNames));
        if (dcv != null) {
            return new MappingCV(mapper, className, dcv);
        }
        return dcv;
    }

    public static class MappingAV extends DexAnnotationVisitor {
        protected Mapper mapper;
        protected String oldAnnotationClassName;

        public MappingAV(String oldAnnotationClassName, Mapper mapper, DexAnnotationVisitor visitor) {
            super(visitor);
            this.mapper = mapper;
            this.oldAnnotationClassName = oldAnnotationClassName;
        }

        @Override
        public void visit(String name, Object value) {
            if (name != null && oldAnnotationClassName != null) {
                name = mapAnnotationElementName(mapper, oldAnnotationClassName, name);
            }
            super.visit(name, mapObject(mapper, value));
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(String name, String desc) {
            if (name != null && oldAnnotationClassName != null) {
                name = mapAnnotationElementName(mapper, oldAnnotationClassName, name);
            }
            DexAnnotationVisitor dav = super.visitAnnotation(name, mapClassName(mapper, desc));
            if (dav != null) {
                return new MappingAV(desc, mapper, dav);
            }
            return dav;
        }

        @Override
        public DexAnnotationVisitor visitArray(String name) {
            if (name != null && oldAnnotationClassName != null) {
                name = mapAnnotationElementName(mapper, oldAnnotationClassName, name);
            }
            DexAnnotationVisitor dav = super.visitArray(name);
            if (dav != null) {
                return new MappingAV(null, mapper, dav);
            }
            return dav;
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            String fn = mapFieldName(mapper, desc, value, desc);
            if (name != null && oldAnnotationClassName != null) {
                name = mapAnnotationElementName(mapper, oldAnnotationClassName, name);
            }
            super.visitEnum(name, mapClassName(mapper, desc), fn);
        }
    }

    public static class MappingCode extends DexCodeVisitor {
        protected Mapper mapper;

        public MappingCode(Mapper mapper, DexCodeVisitor dcv) {
            super(dcv);
            this.mapper = mapper;
        }

        @Override
        public DexDebugVisitor visitDebug() {
            DexDebugVisitor v = super.visitDebug();
            if (v != null) {
                return new DexDebugVisitor() {
                    @Override
                    public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
                        // FIXME map signature
                        super.visitStartLocal(reg, label, name, mapClassName(mapper, type), null);
                    }
                };
            }
            return v;
        }

        @Override
        public void visitConstStmt(Op op, int ra, Object value) {
            super.visitConstStmt(op, ra, mapObject(mapper, value));
        }

        @Override
        public void visitFieldStmt(Op op, int a, int b, Field field) {
            super.visitFieldStmt(op, a, b, mapFieldNameAndOwner(mapper, field));
        }

        @Override
        public void visitFilledNewArrayStmt(Op op, int[] args, String type) {
            super.visitFilledNewArrayStmt(op, args, mapClassName(mapper, type));
        }

        @Override
        public void visitMethodStmt(Op op, int[] args, Method method) {
            switch (op) {
            case INVOKE_DIRECT:
            case INVOKE_DIRECT_RANGE:
            case INVOKE_STATIC:
            case INVOKE_STATIC_RANGE:
            case INVOKE_INTERFACE:
            case INVOKE_INTERFACE_RANGE:
                super.visitMethodStmt(op, args, mapMethodNameAndOwner(mapper, method));
                break;
            default:
                super.visitMethodStmt(op, args, mapMethod(mapper, method));
                break;
            }
        }

        @Override
        public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handler, String[] type) {
            super.visitTryCatch(start, end, handler, mapClassNames(mapper, type));
        }

        @Override
        public void visitTypeStmt(Op op, int a, int b, String type) {
            super.visitTypeStmt(op, a, b, mapClassName(mapper, type));
        }
    }

    public static class MappingCV extends DexClassVisitor {
        protected String clzName;
        protected Mapper mapper;

        public MappingCV(Mapper mapper, String clzName, DexClassVisitor dcv) {
            super(dcv);
            this.mapper = mapper;
            this.clzName = clzName;
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
            final DexAnnotationVisitor dav = super.visitAnnotation(mapClassName(mapper, name), visibility);
            if (dav != null) {
                switch (name) {
                case DexConstants.ANNOTATION_SIGNATURE_TYPE:
                    return new DexAnnotationNode(name, visibility) {
                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            Item p = super.items.get(0);
                            Object[] newVs = remapSignature(mapper, (Object[]) p.value, false);
                            dav.visit(p.name, newVs);
                            dav.visitEnd();
                        }
                    };
                case DexConstants.ANNOTATION_INNER_CLASS_TYPE:
                    return new DexAnnotationVisitor(dav) {
                        @Override
                        public void visit(String name, Object value) {
                            if (name.equals("name")) {
                                String simpleName = (String) value;
                                if (simpleName != null) {
                                    if (clzName.endsWith("$" + simpleName + ";")) {
                                        String nNameDesc = mapClassName(mapper, clzName);
                                        String containd = mapClassName(mapper,
                                                clzName.substring(0, clzName.length() - 2 - simpleName.length()) + ";");

                                        String internalNameWitherOwner = nNameDesc.substring(1, nNameDesc.length() - 1);
                                        String internalNameOwner = containd.substring(1, containd.length() - 1);

                                        if (internalNameWitherOwner.startsWith(internalNameOwner + "$")) {
                                            value = internalNameWitherOwner.substring(1 + internalNameOwner.length());
                                        } else {
                                            value = null;
                                        }
                                    } else {
                                        value = null;
                                    }
                                }
                            }
                            super.visit(name, value);
                        }
                    };
                case DexConstants.ANNOTATION_ENCLOSING_CLASS_TYPE:
                    return new DexAnnotationVisitor(dav) {
                        @Override
                        public void visit(String name, Object value) {
                            if (name.equals("value")) {
                                super.visit(name, new DexType(mapClassName(mapper, ((DexType) value).desc)));
                            } else {
                                super.visit(name, value);
                            }
                        }
                    };
                case DexConstants.ANNOTATION_ENCLOSING_METHOD_TYPE: {
                    return new DexAnnotationVisitor(dav) {
                        @Override
                        public void visit(String name, Object value) {
                            if (name.equals("value")) {
                                Method m = (Method) value;
                                super.visit(name, mapMethod(mapper, m));
                            } else {
                                super.visit(name, value);
                            }
                        }
                    };
                }
                default:
                    return new MappingAV(name, mapper, dav);
                }
            }
            return dav;
        }

        @Override
        public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
            DexFieldVisitor dfv = super.visitField(accessFlags, mapField(mapper, field), mapObject(mapper, value));
            if (dfv != null) {
                return new MappingFV(mapper, dfv);
            }
            return null;
        }

        @Override
        public DexMethodVisitor visitMethod(int accessFlags, Method method) {
            DexMethodVisitor dmv = super.visitMethod(accessFlags, mapMethod(mapper, method));
            if (dmv != null) {
                return new MappingMV(mapper, dmv);
            }
            return dmv;
        }
    }

    public static class MappingFV extends DexFieldVisitor {
        protected Mapper mapper;

        public MappingFV(Mapper mapper, DexFieldVisitor dfv) {
            super(dfv);
            this.mapper = mapper;
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
            final DexAnnotationVisitor dav = super.visitAnnotation(mapClassName(mapper, name), visibility);
            if (dav != null) {
                if (DexConstants.ANNOTATION_SIGNATURE_TYPE.equals(name)) {
                    return new DexAnnotationNode(name, visibility) {
                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            Item p = super.items.get(0);
                            Object[] newVs = remapSignature(mapper, (Object[]) p.value, false);
                            dav.visit(p.name, newVs);
                            dav.visitEnd();
                        }
                    };
                }
                return new MappingAV(name, mapper, dav);
            }
            return dav;
        }
    }

    public static class MappingMV extends DexMethodVisitor {
        protected Mapper mapper;

        public MappingMV(Mapper mapper, DexMethodVisitor dmv) {
            super(dmv);
            this.mapper = mapper;
        }

        @Override
        public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
            final DexAnnotationVisitor dav = super.visitAnnotation(mapClassName(mapper, name), visibility);
            if (dav != null) {
                if (DexConstants.ANNOTATION_SIGNATURE_TYPE.equals(name)) {
                    return new DexAnnotationNode(name, visibility) {
                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            Item p = super.items.get(0);
                            Object[] newVs = remapSignature(mapper, (Object[]) p.value, false);
                            dav.visit(p.name, newVs);
                            dav.visitEnd();
                        }
                    };
                }
                return new MappingAV(name, mapper, dav);
            }
            return dav;
        }

        @Override
        public DexCodeVisitor visitCode() {
            DexCodeVisitor dcv = super.visitCode();
            if (dcv != null) {
                return new MappingCode(mapper, dcv);
            }
            return dcv;
        }

        @Override
        public DexAnnotationAble visitParameterAnnotation(int index) {
            final DexAnnotationAble a = super.visitParameterAnnotation(index);
            if (a != null) {
                return new DexAnnotationAble() {
                    @Override
                    public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
                        DexAnnotationVisitor dav = a.visitAnnotation(mapClassName(mapper, name), visibility);
                        if (dav != null) {
                            return new MappingAV(name, mapper, dav);
                        }
                        return dav;
                    }
                };
            }
            return a;
        }

    }

    static Object[] remapSignature(final Mapper mapper, Object vs[], boolean isType) {
        StringBuilder sb = new StringBuilder();
        for (Object v0 : vs) {
            sb.append(v0);
        }
        SignatureWriter w = new SignatureWriter() {
            String clzName;

            @Override
            public void visitClassType(String name) {
                super.visitClassType(mapClassName(mapper, name));
                clzName = name;
            }

            @Override
            public void visitInnerClassType(String simpleName) {
                String value = simpleName;
                if (simpleName != null) {
                    if (clzName.endsWith("$" + simpleName + ";")) {
                        String nNameDesc = mapClassName(mapper, clzName);
                        String containd = mapClassName(mapper,
                                clzName.substring(0, clzName.length() - 2 - simpleName.length()) + ";");

                        String internalNameWitherOwner = nNameDesc.substring(1, nNameDesc.length() - 1);
                        String internalNameOwner = containd.substring(1, containd.length() - 1);

                        if (internalNameWitherOwner.startsWith(internalNameOwner + "$")) {
                            value = internalNameWitherOwner.substring(1 + internalNameOwner.length());
                        } else {
                            value = null;
                        }
                    } else {
                        value = null;
                    }
                }
                super.visitInnerClassType(value);
            }
        };
        if (isType) {
            new SignatureReader(sb.toString()).acceptType(w);
        } else {
            new SignatureReader(sb.toString()).accept(w);
        }
        String newSignature = w.toString();
        return Types.buildDexStyleSignature(newSignature);
    }
}
