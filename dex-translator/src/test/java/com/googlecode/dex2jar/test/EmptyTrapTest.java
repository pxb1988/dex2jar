package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.OP_CONST_STRING;
import static com.googlecode.dex2jar.DexOpcodes.OP_GOTO;
import static com.googlecode.dex2jar.DexOpcodes.OP_IF_EQZ;
import static com.googlecode.dex2jar.DexOpcodes.OP_IF_NEZ;
import static com.googlecode.dex2jar.DexOpcodes.OP_IGET;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_DIRECT;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_VIRTUAL;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_EXCEPTION;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_RESULT;
import static com.googlecode.dex2jar.DexOpcodes.OP_NEW_INSTANCE;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.v3.V3;
import com.googlecode.dex2jar.v3.V3MethodAdapter;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;
import com.googlecode.dex2jar.visitors.EmptyVisitor;

public class EmptyTrapTest implements Opcodes {

    @Test
    public void test1() throws Exception {
        try {
            final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cw.visit(V1_6, ACC_PUBLIC, "JSResponseTest", null, "java/lang/Object", null);
            m005_toJSONString(new EmptyVisitor() {
                @Override
                public DexMethodVisitor visitMethod(int accessFlags, Method method) {
                    return new V3MethodAdapter(accessFlags, method, null, V3.OPTIMIZE_SYNCHRONIZED
                            | V3.TOPOLOGICAL_SORT) {

                        @Override
                        public void visitEnd() {
                            super.visitEnd();
                            methodNode.accept(cw);
                        }
                    };
                }
            });
            ClassReader cr = new ClassReader(cw.toByteArray());
            TestUtils.verify(cr);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void m005_toJSONString(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(0, new Method("LJSResponseTest;", "toJSONString", new String[] {},
                "Ljava/lang/String;"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitArguments(6, new int[] { 5 });
                DexLabel L0 = new DexLabel();
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                code.visitTryCatch(L0, L1, L2, "Lorg/json/JSONException;");
                DexLabel L3 = new DexLabel();
                DexLabel L4 = new DexLabel();
                code.visitTryCatch(L3, L4, L2, "Lorg/json/JSONException;");
                DexLabel L5 = new DexLabel();
                DexLabel L6 = new DexLabel();
                code.visitTryCatch(L5, L6, L2, "Lorg/json/JSONException;");
                DexLabel L7 = new DexLabel();
                code.visitLineNumber(50, L7);
                code.visitLineNumber(50, L0);
                DexLabel L8 = new DexLabel();
                code.visitLineNumber(51, L8);
                DexLabel L9 = new DexLabel();
                code.visitLineNumber(67, L9);
                DexLabel L10 = new DexLabel();
                code.visitLineNumber(52, L10);
                DexLabel L11 = new DexLabel();
                code.visitLineNumber(53, L11);
                DexLabel L12 = new DexLabel();
                code.visitLineNumber(54, L12);
                DexLabel L13 = new DexLabel();
                code.visitLineNumber(55, L13);
                DexLabel L14 = new DexLabel();
                code.visitLineNumber(56, L14);
                DexLabel L15 = new DexLabel();
                code.visitLineNumber(57, L15);
                code.visitLineNumber(67, L1);
                code.visitLineNumber(58, L3);
                DexLabel L16 = new DexLabel();
                code.visitLineNumber(59, L16);
                code.visitLineNumber(62, L2);
                code.visitLocalVariable("object", "Lorg/json/JSONObject;", null, L11, L2, 1);
                DexLabel L17 = new DexLabel();
                code.visitLineNumber(64, L17);
                DexLabel L18 = new DexLabel();
                code.visitLineNumber(65, L18);
                code.visitLineNumber(61, L5);
                code.visitLocalVariable("jsonException", "Lorg/json/JSONException;", null, L17, L5, 0);
                DexLabel L19 = new DexLabel();
                code.visitLocalVariable("object", "Lorg/json/JSONObject;", null, L5, L19, 1);
                code.visitLocalVariable("this", "LJSResponseTest;", null, L7, L19, 5);
                code.visitLabel(L7);
                code.visitConstStmt(OP_CONST_STRING, 2, "response", 2);
                code.visitConstStmt(OP_CONST_STRING, 4, "", 2);
                code.visitLabel(L0);
                code.visitFieldStmt(OP_IGET, 2, 5, new Field("LJSResponseTest;", "className", "Ljava/lang/String;"), 2);
                code.visitJumpStmt(OP_IF_EQZ, 2, L8);
                code.visitFieldStmt(OP_IGET, 2, 5, new Field("LJSResponseTest;", "methodName", "Ljava/lang/String;"), 2);
                code.visitJumpStmt(OP_IF_NEZ, 2, L10);
                code.visitLabel(L8);
                code.visitConstStmt(OP_CONST_STRING, 2, "", 2);
                code.visitMoveStmt(OP_MOVE, 2, 4, 2);
                code.visitLabel(L9);
                code.visitReturnStmt(OP_RETURN, 2, 2);
                code.visitLabel(L10);
                code.visitClassStmt(OP_NEW_INSTANCE, 1, "Lorg/json/JSONObject;");
                code.visitMethodStmt(OP_INVOKE_DIRECT, new int[] { 1 }, new Method("Lorg/json/JSONObject;", "<init>",
                        new String[] {}, "V"));
                code.visitLabel(L11);
                code.visitConstStmt(OP_CONST_STRING, 2, "class", 2);
                code.visitFieldStmt(OP_IGET, 3, 5, new Field("LJSResponseTest;", "className", "Ljava/lang/String;"), 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;",
                        "put", new String[] { "Ljava/lang/String;", "Ljava/lang/Object;" }, "Lorg/json/JSONObject;"));
                code.visitLabel(L12);
                code.visitConstStmt(OP_CONST_STRING, 2, "call", 2);
                code.visitFieldStmt(OP_IGET, 3, 5, new Field("LJSResponseTest;", "methodName", "Ljava/lang/String;"), 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;",
                        "put", new String[] { "Ljava/lang/String;", "Ljava/lang/Object;" }, "Lorg/json/JSONObject;"));
                code.visitLabel(L13);
                code.visitConstStmt(OP_CONST_STRING, 2, "result", 2);
                code.visitFieldStmt(OP_IGET, 3, 5, new Field("LJSResponseTest;", "result", "I"), 0);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;",
                        "put", new String[] { "Ljava/lang/String;", "I" }, "Lorg/json/JSONObject;"));
                code.visitLabel(L14);
                code.visitFieldStmt(OP_IGET, 2, 5, new Field("LJSResponseTest;", "response", "Ljava/lang/Object;"), 2);
                code.visitJumpStmt(OP_IF_EQZ, 2, L3);
                code.visitLabel(L15);
                code.visitConstStmt(OP_CONST_STRING, 2, "response", 2);
                code.visitFieldStmt(OP_IGET, 3, 5, new Field("LJSResponseTest;", "response", "Ljava/lang/Object;"), 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;",
                        "put", new String[] { "Ljava/lang/String;", "Ljava/lang/Object;" }, "Lorg/json/JSONObject;"));
                code.visitLabel(L1);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1 }, new Method("Lorg/json/JSONObject;",
                        "toString", new String[] {}, "Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 2, 2);
                code.visitJumpStmt(OP_GOTO, L9);
                code.visitLabel(L3);
                code.visitFieldStmt(OP_IGET, 2, 5, new Field("LJSResponseTest;", "dataResponse", "[B"), 2);
                code.visitJumpStmt(OP_IF_EQZ, 2, L5);
                code.visitLabel(L16);
                code.visitConstStmt(OP_CONST_STRING, 2, "response", 2);
                code.visitFieldStmt(OP_IGET, 3, 5, new Field("LJSResponseTest;", "dataResponse", "[B"), 2);
                code.visitMethodStmt(OP_INVOKE_STATIC, new int[] { 3 }, new Method("LBase64;", "encode",
                        new String[] { "[B" }, "Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 3, 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;",
                        "put", new String[] { "Ljava/lang/String;", "Ljava/lang/Object;" }, "Lorg/json/JSONObject;"));
                code.visitLabel(L4);
                code.visitJumpStmt(OP_GOTO, L1);
                code.visitLabel(L2);
                code.visitMoveStmt(OP_MOVE_EXCEPTION, 2, 2);
                code.visitMoveStmt(OP_MOVE, 0, 2, 2);
                code.visitLabel(L17);
                code.visitConstStmt(OP_CONST_STRING, 2, "MillennialMediaSDK", 2);
                code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 0 }, new Method("Lorg/json/JSONException;",
                        "getMessage", new String[] {}, "Ljava/lang/String;"));
                code.visitMoveStmt(OP_MOVE_RESULT, 3, 2);
                code.visitMethodStmt(OP_INVOKE_STATIC, new int[] { 2, 3 }, new Method("Landroid/util/Log;", "e",
                        new String[] { "Ljava/lang/String;", "Ljava/lang/String;" }, "I"));
                code.visitLabel(L18);
                code.visitConstStmt(OP_CONST_STRING, 2, "", 2);
                code.visitMoveStmt(OP_MOVE, 2, 4, 2);
                code.visitJumpStmt(OP_GOTO, L9);
                code.visitLabel(L5);
                code.visitConstStmt(OP_CONST_STRING, 2, "", 2);
                code.visitLabel(L6);
                code.visitMoveStmt(OP_MOVE, 2, 4, 2);
                code.visitJumpStmt(OP_GOTO, L9);
                code.visitLabel(L19);
                code.visitEnd();
            }
            mv.visitEnd();
        }
    }
}
