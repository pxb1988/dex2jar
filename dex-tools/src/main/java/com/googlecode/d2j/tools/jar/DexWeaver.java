/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2015 Panxiaobo
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
package com.googlecode.d2j.tools.jar;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexCodeNode;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * only implement sub set of InvocationWeaver
 * 2. Replace method A to another method B, parameter of B must be MethodInvocation
 * 3. Replace Methods Implementations
 */
public class DexWeaver extends BaseWeaver {
    interface CB {
        String getKey(Method mtd);
    }

    public String buildInvocationClz(DexFileVisitor dfv) {
        String typeName = getCurrentInvocationName();
        String typeNameDesc = "L" + typeName + ";";
        DexClassVisitor dcv = dfv.visit(DexConstants.ACC_PUBLIC, typeNameDesc, "Ljava/lang/Object;", new String[]{
                invocationInterfaceDesc});

        dcv.visitField(DexConstants.ACC_PRIVATE | DexConstants.ACC_FINAL,
                new Field(typeNameDesc, "thiz", "Ljava/lang/Object;"), null).visitEnd();
        dcv.visitField(DexConstants.ACC_PRIVATE | DexConstants.ACC_FINAL, new Field(typeNameDesc, "args", "[Ljava/lang/Object;"), null)
                .visitEnd();
        dcv.visitField(DexConstants.ACC_PRIVATE | DexConstants.ACC_FINAL, new Field(typeNameDesc, "idx", "I"), null)
                .visitEnd();


        {
            DexMethodVisitor mv = dcv
                    .visitMethod(DexConstants.ACC_PUBLIC | DexConstants.ACC_CONSTRUCTOR, new Method(typeNameDesc, "<init>", new String[]{
                            "Ljava/lang/Object;", "[Ljava/lang/Object;", "I"}, "V"));
            DexCodeVisitor codeVisitor = mv.visitCode();
            codeVisitor.visitRegister(4);
            codeVisitor.visitFieldStmt(Op.IPUT_OBJECT, 1, 0, new Field(typeNameDesc, "thiz", "Ljava/lang/Object;"));
            codeVisitor.visitFieldStmt(Op.IPUT_OBJECT, 2, 0, new Field(typeNameDesc, "args", "[Ljava/lang/Object;"));
            codeVisitor.visitFieldStmt(Op.IPUT, 3, 0, new Field(typeNameDesc, "idx", "I"));
            codeVisitor.visitStmt0R(Op.RETURN_VOID);
            codeVisitor.visitEnd();
            mv.visitEnd();
        }
        {
            genSwitchMethod(dcv, typeNameDesc, "getMethodOwner", new CB() {
                @Override
                public String getKey(Method mtd) {
                    return toInternal(mtd.getOwner());
                }
            });
            genSwitchMethod(dcv, typeNameDesc, "getMethodName", new CB() {
                @Override
                public String getKey(Method mtd) {
                    return mtd.getName();
                }
            });
            genSwitchMethod(dcv, typeNameDesc, "getMethodDesc", new CB() {
                @Override
                public String getKey(Method mtd) {
                    return mtd.getDesc();
                }
            });
        }

        {
            DexMethodVisitor mv = dcv
                    .visitMethod(DexConstants.ACC_PUBLIC, new Method(typeNameDesc, "getArguments", new String[0], "[Ljava/lang/Object;"));
            DexCodeVisitor code = mv.visitCode();
            code.visitRegister(2);
            code.visitFieldStmt(Op.IGET, 0, 1, new Field(typeNameDesc, "args", "[Ljava/lang/Object;"));
            code.visitStmt1R(Op.RETURN_OBJECT, 0);

            code.visitEnd();
            mv.visitEnd();
        }

        {
            DexMethodVisitor mv = dcv
                    .visitMethod(DexConstants.ACC_PUBLIC, new Method(typeNameDesc, "getThis", new String[0], "Ljava/lang/Object;"));
            DexCodeVisitor code = mv.visitCode();
            code.visitRegister(2);
            code.visitFieldStmt(Op.IGET, 0, 1, new Field(typeNameDesc, "thiz", "Ljava/lang/Object;"));
            code.visitStmt1R(Op.RETURN_OBJECT, 0);
            code.visitEnd();
            mv.visitEnd();
        }
        {
            DexMethodVisitor mv = dcv
                    .visitMethod(DexConstants.ACC_PUBLIC, new Method(typeNameDesc, "proceed", new String[0], "Ljava/lang/Object;"));
            DexCodeVisitor code = mv.visitCode();
            code.visitRegister(4);


            code.visitFieldStmt(Op.IGET, 0, 3, new Field(typeNameDesc, "thiz", "Ljava/lang/Object;"));
            code.visitFieldStmt(Op.IGET, 1, 3, new Field(typeNameDesc, "args", "[Ljava/lang/Object;"));
            code.visitFieldStmt(Op.IGET, 2, 3, new Field(typeNameDesc, "idx", "I"));

            DexLabel labels[] = new DexLabel[callbacks.size()];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new DexLabel();
            }
            code.visitPackedSwitchStmt(Op.PACKED_SWITCH, 2, 0, labels);
            code.visitTypeStmt(Op.NEW_INSTANCE, 0, 0, "Ljava/lang/RuntimeException;");
            code.visitConstStmt(Op.CONST_STRING, 1, "invalid idx");
            code.visitMethodStmt(Op.INVOKE_DIRECT, new int[]{0,
                    1}, new Method("Ljava/lang/RuntimeException;", "<init>", new String[]{"Ljava/lang/String;"}, "V"));
            code.visitStmt1R(Op.THROW, 0);

            for (int i = 0; i < labels.length; i++) {
                code.visitLabel(labels[i]);
                Callback callback = callbacks.get(i);
                Method mCallback = (Method) callback.callback;
                if (callback.isStatic) {
                    code.visitMethodStmt(Op.INVOKE_STATIC, new int[]{1}, mCallback);
                } else if (callback.isSpecial) {
                    code.visitTypeStmt(Op.CHECK_CAST, 0, -1, mCallback.getOwner());
                    code.visitMethodStmt(Op.INVOKE_VIRTUAL, new int[]{0, 1}, mCallback);
                } else {
                    code.visitMethodStmt(Op.INVOKE_STATIC, new int[]{0, 1}, mCallback);
                }
                code.visitStmt1R(Op.MOVE_RESULT_OBJECT, 0);
                code.visitStmt1R(Op.RETURN_OBJECT, 0);
            }

            code.visitEnd();
            mv.visitEnd();
        }

        dcv.visitEnd();
        return typeName;
    }

    private void genSwitchMethod(DexClassVisitor dcv, String typeNameDesc, String methodName, CB callback) {
        DexMethodVisitor dmv = dcv
                .visitMethod(DexConstants.ACC_PUBLIC, new Method(typeNameDesc, methodName, new String[0], "Ljava/lang/String;"));
        DexCodeVisitor code = dmv.visitCode();
        code.visitRegister(3);
        code.visitFieldStmt(Op.IGET, 0, 2, new Field(typeNameDesc, "idx", "I"));

        DexLabel labels[] = new DexLabel[callbacks.size()];

        Map<String, DexLabel> strMap = new TreeMap<>();
        for (int i = 0; i < labels.length; i++) {
            Callback cb = callbacks.get(i);
            String key = callback.getKey((Method) cb.target);
            DexLabel label = strMap.get(key);
            if (label == null) {
                label = new DexLabel();
                strMap.put(key, label);
            }
            labels[i] = label;
        }
        code.visitPackedSwitchStmt(Op.PACKED_SWITCH, 0, 0, labels);
        code.visitTypeStmt(Op.NEW_INSTANCE, 0, 0, "Ljava/lang/RuntimeException;");
        code.visitConstStmt(Op.CONST_STRING, 1, "invalid idx");
        code.visitMethodStmt(Op.INVOKE_DIRECT, new int[]{0,
                1}, new Method("Ljava/lang/RuntimeException;", "<init>", new String[]{"Ljava/lang/String;"}, "V"));
        code.visitStmt1R(Op.THROW, 0);

        for (Map.Entry<String, DexLabel> e : strMap.entrySet()) {
            code.visitLabel(e.getValue());
            code.visitConstStmt(Op.CONST_STRING, 0, e.getKey());
            code.visitStmt1R(Op.RETURN_OBJECT, 0);
        }
        code.visitEnd();
        dmv.visitEnd();
    }

    public DexFileVisitor wrap(DexFileVisitor dcv) {
        return dcv == null ? null : new DexFileVisitor(dcv) {
            @Override
            public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
                return wrap(className, super.visit(access_flags, className, superClass, interfaceNames));
            }
        };
    }

    public DexClassVisitor wrap(final String classNameDesc, final DexClassVisitor dcv) {

        return dcv == null ? null : new DexClassVisitor(dcv) {
            Map<MtdInfo, Method> cache = new HashMap<>();

            @Override
            public DexMethodVisitor visitMethod(final int accessFlags, Method method) {
                final DexMethodVisitor dmv = superVisitDexMethod(accessFlags, method);
                final MtdInfo mapTo = findDefinedTargetMethod(method.getOwner(), method.getName(), method.getDesc());
                if (mapTo != null) {
                    final Method t = new Method(
                            method.getOwner(), buildMethodAName(method.getName()), method.getParameterTypes(), method
                            .getReturnType()
                    );
                    final Method src = method;
                    return new DexMethodNode(accessFlags, method) {
                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            DexCodeNode code = this.codeNode;
                            this.codeNode = null;
                            accept(dmv);
                            Op opcode;
                            if (Modifier.isStatic(access)) {
                                opcode = Op.INVOKE_STATIC_RANGE;
                            } else {
                                opcode = Op.INVOKE_VIRTUAL_RANGE;
                            }
                            generateMtdACode(opcode, t, mapTo, dmv, src);

                            int newAccess = (access & ~(DexConstants.ACC_PRIVATE | DexConstants.ACC_PROTECTED)) | DexConstants.ACC_PUBLIC; // make sure public
                            code.accept(wrap(superVisitDexMethod(newAccess, t), dcv));
                        }
                    };
                } else {
                    return wrap(dmv, dcv);
                }
            }

            private DexMethodVisitor wrap(DexMethodVisitor dmv, final DexClassVisitor classVisitor) {
                return dmv == null ? null : new DexMethodVisitor(dmv) {
                    @Override
                    public DexCodeVisitor visitCode() {
                        return wrap(super.visitCode(), classVisitor);
                    }
                };
            }

            private DexCodeVisitor wrap(DexCodeVisitor dcv, final DexClassVisitor classVisitor) {
                return dcv == null ? null : new DexCodeVisitor(dcv) {
                    @Override
                    public void visitMethodStmt(Op op, int[] args, Method method) {
                        MtdInfo mapTo = findTargetMethod(method.getOwner(), method.getName(), method.getDesc());
                        if (mapTo != null) {

                            Method methodA = cache.get(buildKey(method.getOwner(), method.getName(), method.getDesc()));
                            if (methodA == null) {
                                if (isStatic(op)) {
                                    methodA = new Method(classNameDesc, buildMethodAName(method.getName()), method
                                            .getParameterTypes(), method.getReturnType());
                                } else {
                                    methodA = new Method(classNameDesc, buildMethodAName(method.getName()), join(method
                                            .getOwner(), method.getParameterTypes()), method.getReturnType());
                                }
                                DexMethodVisitor dmv = classVisitor
                                        .visitMethod(DexConstants.ACC_PRIVATE | DexConstants.ACC_STATIC, methodA);
                                generateMtdACode(op, method, mapTo, dmv, method);
                                dmv.visitEnd();
                                cache.put(buildKey(method.getOwner(), method.getName(), method.getDesc()), methodA);
                            }
                            super.visitMethodStmt(isRange(op) ? Op.INVOKE_STATIC_RANGE : Op.INVOKE_STATIC, args, methodA);
                        } else {
                            super.visitMethodStmt(op, args, method);
                        }
                    }
                };
            }

            private void generateMtdACode(Op opcode, Method t, MtdInfo mapTo, DexMethodVisitor dmv, Method src) {

                DexCodeVisitor dcv = dmv.visitCode();
                int countArge = countArgs(t);
                boolean haveThis = haveThis(opcode);

                int registers = 4 + (haveThis ? 1 : 0) + countArge;
                dcv.visitRegister(registers);
                int argStart = 4;
                if (haveThis) {
                    dcv.visitStmt2R(Op.MOVE_OBJECT, 0, argStart);
                    argStart++;
                } else {
                    dcv.visitConstStmt(Op.CONST_4, 0, 0);
                }
                if (t.getParameterTypes().length == 0) {
                    dcv.visitConstStmt(Op.CONST_4, 1, 0);
                } else {
                    dcv.visitConstStmt(Op.CONST, 1, t.getParameterTypes().length);
                    dcv.visitTypeStmt(Op.NEW_ARRAY, 1, 1, "[Ljava/lang/Object;");
                    for (int i = 0; i < t.getParameterTypes().length; i++) {
                        char type = t.getParameterTypes()[i].charAt(0);
                        dcv.visitConstStmt(Op.CONST, 2, i);
                        box(type, argStart, 3, dcv);
                        dcv.visitStmt3R(Op.APUT_OBJECT, 3, 1, 2);
                        if (type == 'J' || type == 'D') {
                            argStart += 2;
                        } else {
                            argStart += 1;
                        }
                    }
                }
                int nextIdx = callbacks.size();
                dcv.visitConstStmt(Op.CONST, 2, nextIdx);
                String miTypeDesc = "L" + getCurrentInvocationName() + ";";
                dcv.visitTypeStmt(Op.NEW_INSTANCE, 3, 0, miTypeDesc);
                dcv.visitMethodStmt(Op.INVOKE_DIRECT, new int[]{3, 0, 1,
                        2}, new Method(miTypeDesc, "<init>", new String[]{
                        "Ljava/lang/Object;", "[Ljava/lang/Object;", "I"
                }, "V"));
                Method call = build(mapTo);
                dcv.visitMethodStmt(Op.INVOKE_STATIC, new int[]{3}, call);
                if (!"V".equals(t.getReturnType())) {
                    switch (call.getReturnType().charAt(0)) {
                        case '[':
                        case 'L':
                            dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, 0);
                            break;
                        case 'J':
                        case 'D':
                            dcv.visitStmt1R(Op.MOVE_RESULT_WIDE, 0);
                            break;
                        default:
                            dcv.visitStmt1R(Op.MOVE_RESULT, 0);
                            break;
                    }
                    unbox(t.getReturnType(), 0, dcv);
                    switch (t.getReturnType().charAt(0)) {
                        case '[':
                        case 'L':
                            dcv.visitStmt1R(Op.RETURN_OBJECT, 0);
                            break;
                        case 'J':
                        case 'D':
                            dcv.visitStmt1R(Op.RETURN_WIDE, 0);
                            break;
                        default:
                            dcv.visitStmt1R(Op.RETURN, 0);
                            break;
                    }
                } else {
                    dcv.visitStmt0R(Op.RETURN_VOID);
                }

                Callback cb = new Callback();
                cb.idx = nextIdx;
                cb.callback = newMethodCallback(opcode, t);
                cb.target = src;
                cb.isSpecial = isSuper(opcode);
                cb.isStatic = isStatic(opcode);
                callbacks.add(cb);
            }

            private Method newMethodCallback(Op opcode, Method t) {
                boolean isStatic = !haveThis(opcode);
                boolean isSuper = isSuper(opcode);
                Method m;
                if (isSuper || isStatic) {
                    m = new Method(t.getOwner(), buildCallbackMethodName(t.getName()), new String[]{
                            "[Ljava/lang/Object;"
                    }, "Ljava/lang/Object;");
                } else {
                    m = new Method(t.getOwner(), buildCallbackMethodName(t.getName()), new String[]{
                            "Ljava/lang/Object;", "[Ljava/lang/Object;"
                    }, "Ljava/lang/Object;");
                }

                DexMethodVisitor dmv = superVisitDexMethod(
                        DexConstants.ACC_PUBLIC | (isSuper ? 0 : DexConstants.ACC_STATIC), m);
                DexCodeVisitor dcv = dmv.visitCode();
                int totalRegs;
                int argStart;
                if (isStatic) {
                    totalRegs = 1 + countArgs(t) + 1;
                    argStart = totalRegs - 1;
                } else {
                    totalRegs = 1 + countArgs(t) + 2;
                    argStart = totalRegs - 2;
                }
                dcv.visitRegister(totalRegs);
                int args[] = new int[countArgs(t) + (isStatic ? 0 : 1)];
                int args_index = 0;
                int i = 1;
                if (!isStatic) {
                    if (i != argStart) {
                        dcv.visitStmt2R(Op.MOVE_OBJECT, i, argStart);
                    }
                    if(!isSuper) {
                        dcv.visitTypeStmt(Op.CHECK_CAST, i, -1, t.getOwner());
                    }
                    args[args_index++] = i;
                    i++;
                    argStart++;
                }

                String[] parameterTypes = t.getParameterTypes();
                for (int i1 = 0; i1 < parameterTypes.length; i1++) {
                    String argType = parameterTypes[i1];
                    dcv.visitConstStmt(Op.CONST, 0, i1);
                    dcv.visitStmt3R(Op.AGET_OBJECT, i, argStart, 0);
                    unbox(argType, i, dcv);
                    args[args_index++] = i;
                    if (argType.charAt(0) == 'J' || argType.charAt(0) == 'D') {
                        args[args_index++] = i + 1;
                        i += 2;
                    } else {
                        i += 1;
                    }
                }

                dcv.visitMethodStmt(opcode, args, t);
                if ("V".equals(t.getReturnType())) {
                    dcv.visitConstStmt(Op.CONST, 0, 0);
                } else {
                    switch (t.getReturnType().charAt(0)) {
                        case '[':
                        case 'L':
                            dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, 0);
                            break;
                        case 'J':
                        case 'D':
                            dcv.visitStmt1R(Op.MOVE_RESULT_WIDE, 0);
                            break;
                        default:
                            dcv.visitStmt1R(Op.MOVE_RESULT, 0);
                            break;
                    }
                    box(t.getReturnType().charAt(0), 0, 0, dcv);
                }
                dcv.visitStmt1R(Op.RETURN_OBJECT, 0);

                return m;
            }

            private DexMethodVisitor superVisitDexMethod(int accessFlags, Method method) {
                return super.visitMethod(accessFlags, method);
            }
        };
    }

    private String[] join(String a, String[] b) {
        String joined[] = new String[b.length + 1];
        joined[0] = a;
        System.arraycopy(b, 0, joined, 1, b.length);
        return joined;
    }

    private boolean isStatic(Op op) {
        return op == Op.INVOKE_STATIC || op == Op.INVOKE_STATIC_RANGE;
    }

    private boolean isRange(Op op) {
        switch (op) {
            case INVOKE_STATIC_RANGE:
            case INVOKE_DIRECT_RANGE:
            case INVOKE_INTERFACE_RANGE:
            case INVOKE_SUPER_RANGE:
            case INVOKE_VIRTUAL_RANGE:
                return true;
            default:
                return false;
        }
    }

    private void unbox(String argType, int i, DexCodeVisitor dcv) {
        switch (argType.charAt(0)) {
            case '[':
            case 'L':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, argType);
                break;
            case 'Z':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, "Ljava/lang/Boolean;");
                dcv.visitMethodStmt(Op.INVOKE_VIRTUAL_RANGE, new int[]{
                        i}, new Method("Ljava/lang/Boolean;", "booleanValue", new String[]{}, "Z"));
                dcv.visitStmt1R(Op.MOVE_RESULT, i);
                break;
            case 'B':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, "Ljava/lang/Byte;");
                dcv.visitMethodStmt(Op.INVOKE_VIRTUAL_RANGE, new int[]{
                        i}, new Method("Ljava/lang/Byte;", "byteValue", new String[]{}, "B"));
                dcv.visitStmt1R(Op.MOVE_RESULT, i);
                break;
            case 'S':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, "Ljava/lang/Short;");
                dcv.visitMethodStmt(Op.INVOKE_VIRTUAL_RANGE, new int[]{
                        i}, new Method("Ljava/lang/Short;", "shortValue", new String[]{}, "S"));
                dcv.visitStmt1R(Op.MOVE_RESULT, i);
                break;
            case 'C':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, "Ljava/lang/Character;");
                dcv.visitMethodStmt(Op.INVOKE_VIRTUAL_RANGE, new int[]{
                        i}, new Method("Ljava/lang/Character;", "charValue", new String[]{}, "C"));
                dcv.visitStmt1R(Op.MOVE_RESULT, i);
                break;
            case 'I':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, "Ljava/lang/Integer;");
                dcv.visitMethodStmt(Op.INVOKE_VIRTUAL_RANGE, new int[]{
                        i}, new Method("Ljava/lang/Integer;", "intValue", new String[]{}, "I"));
                dcv.visitStmt1R(Op.MOVE_RESULT, i);
                break;
            case 'F':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, "Ljava/lang/Float;");
                dcv.visitMethodStmt(Op.INVOKE_VIRTUAL_RANGE, new int[]{
                        i}, new Method("Ljava/lang/Float;", "floatValue", new String[]{}, "F"));
                dcv.visitStmt1R(Op.MOVE_RESULT, i);
                break;
            case 'D':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, "Ljava/lang/Double;");
                dcv.visitMethodStmt(Op.INVOKE_VIRTUAL_RANGE, new int[]{
                        i}, new Method("Ljava/lang/Double;", "doubleValue", new String[]{}, "D"));
                dcv.visitStmt1R(Op.MOVE_RESULT_WIDE, i);
                break;
            case 'J':
                dcv.visitTypeStmt(Op.CHECK_CAST, i, i, "Ljava/lang/Long;");
                dcv.visitMethodStmt(Op.INVOKE_VIRTUAL_RANGE, new int[]{
                        i}, new Method("Ljava/lang/Long;", "longValue", new String[]{}, "J"));
                dcv.visitStmt1R(Op.MOVE_RESULT_WIDE, i);
                break;
        }
    }

    private boolean isSuper(Op opcode) {
        return opcode == Op.INVOKE_SUPER || opcode == Op.INVOKE_SUPER_RANGE;
    }

    private Method build(MtdInfo mapTo) {
        Type[] ts = Type.getArgumentTypes(mapTo.desc);
        String ss[] = new String[ts.length];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = ts[i].getDescriptor();
        }
        return new Method(mapTo.owner, mapTo.name, ss, Type.getReturnType(mapTo.desc).getDescriptor());
    }

    private void box(char type, int from, int to, DexCodeVisitor dcv) {
        switch (type) {
            case 'L':
            case '[':
                dcv.visitStmt2R(Op.MOVE_OBJECT, from, to);
                break;
            case 'Z':
                dcv.visitMethodStmt(Op.INVOKE_STATIC_RANGE, new int[]{
                        from}, new Method("Ljava/lang/Boolean;", "valueOf", new String[]{"Z"}, "Ljava/lang/Boolean;"));
                dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, to);
                break;
            case 'B':
                dcv.visitMethodStmt(Op.INVOKE_STATIC_RANGE, new int[]{
                        from}, new Method("Ljava/lang/Byte;", "valueOf", new String[]{"B"}, "Ljava/lang/Byte;"));
                dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, to);
                break;
            case 'S':
                dcv.visitMethodStmt(Op.INVOKE_STATIC_RANGE, new int[]{
                        from}, new Method("Ljava/lang/Short;", "valueOf", new String[]{"S"}, "Ljava/lang/Short;"));
                dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, to);
                break;
            case 'C':
                dcv.visitMethodStmt(Op.INVOKE_STATIC_RANGE, new int[]{
                        from}, new Method("Ljava/lang/Character;", "valueOf", new String[]{
                        "C"}, "Ljava/lang/Character;"));
                dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, to);
                break;
            case 'I':
                dcv.visitMethodStmt(Op.INVOKE_STATIC_RANGE, new int[]{
                        from}, new Method("Ljava/lang/Integer;", "valueOf", new String[]{"I"}, "Ljava/lang/Integer;"));
                dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, to);
                break;
            case 'F':
                dcv.visitMethodStmt(Op.INVOKE_STATIC_RANGE, new int[]{
                        from}, new Method("Ljava/lang/Float;", "valueOf", new String[]{"F"}, "Ljava/lang/Float;"));
                dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, to);
                break;
            case 'D':
                dcv.visitMethodStmt(Op.INVOKE_STATIC_RANGE, new int[]{
                        from, from + 1}, new Method("Ljava/lang/Double;", "valueOf", new String[]{
                        "D"}, "Ljava/lang/Double;"));
                dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, to);
                break;
            case 'J':
                dcv.visitMethodStmt(Op.INVOKE_STATIC_RANGE, new int[]{
                        from, from + 1}, new Method("Ljava/lang/Long;", "valueOf", new String[]{
                        "J"}, "Ljava/lang/Long;"));
                dcv.visitStmt1R(Op.MOVE_RESULT_OBJECT, to);
                break;
        }
    }

    private boolean haveThis(Op opcode) {
        return opcode != Op.INVOKE_STATIC && opcode != Op.INVOKE_STATIC_RANGE;
    }

    static int countArgs(Method t) {
        int i = 0;
        for (String arg : t.getParameterTypes()) {
            char type = arg.charAt(0);
            if (type == 'J' || type == 'D') {
                i += 2;
            } else {
                i += 1;
            }
        }
        return i;
    }


}
