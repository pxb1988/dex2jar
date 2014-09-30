package com.googlecode.dex2jar.test;

import static com.googlecode.d2j.reader.Op.*;

import com.googlecode.d2j.visitors.DexDebugVisitor;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

@RunWith(DexTranslatorRunner.class)
public class EmptyTrapTest {

    @Test
    public static void m005_toJSONString(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(0, new Method("LJSResponseTest;", "toJSONString", new String[] {},
                "Ljava/lang/String;"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitRegister(6);

                DexLabel L8 = new DexLabel();
                DexLabel L9 = new DexLabel();
                DexLabel L10 = new DexLabel();

                DexLabel L0 = new DexLabel();
                DexLabel L1 = new DexLabel();
                DexLabel L2 = new DexLabel();
                code.visitTryCatch(L0, L1, new DexLabel[] { L2 }, new String[] { "Lorg/json/JSONException;" });
                DexLabel L3 = new DexLabel();
                DexLabel L4 = new DexLabel();
                code.visitTryCatch(L3, L4, new DexLabel[] { L2 }, new String[] { "Lorg/json/JSONException;" });
                DexLabel L5 = new DexLabel();
                DexLabel L6 = new DexLabel();
                code.visitTryCatch(L5, L6, new DexLabel[] { L2 }, new String[] { "Lorg/json/JSONException;" });

                code.visitConstStmt(CONST_STRING, 2, "response");
                code.visitConstStmt(CONST_STRING, 4, "");
                code.visitLabel(L0);
                code.visitFieldStmt(IGET, 2, 5, new Field("LJSResponseTest;", "className", "Ljava/lang/String;"));
                code.visitJumpStmt(IF_EQZ, 2, -1, L8);
                code.visitFieldStmt(IGET, 2, 5, new Field("LJSResponseTest;", "methodName", "Ljava/lang/String;"));
                code.visitJumpStmt(IF_NEZ, 2, -1, L10);
                code.visitLabel(L8);
                code.visitConstStmt(CONST_STRING, 2, "");
                code.visitStmt2R(MOVE, 2, 4);
                code.visitLabel(L9);
                code.visitStmt1R(RETURN_OBJECT, 2);
                code.visitLabel(L10);
                code.visitTypeStmt(NEW_INSTANCE, 1, -1, "Lorg/json/JSONObject;");
                code.visitMethodStmt(INVOKE_DIRECT, new int[] { 1 }, new Method("Lorg/json/JSONObject;", "<init>",
                        new String[] {}, "V"));

                code.visitConstStmt(CONST_STRING, 2, "class");
                code.visitFieldStmt(IGET, 3, 5, new Field("LJSResponseTest;", "className", "Ljava/lang/String;"));
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;", "put",
                        new String[] { "Ljava/lang/String;", "Ljava/lang/Object;" }, "Lorg/json/JSONObject;"));

                code.visitConstStmt(CONST_STRING, 2, "call");
                code.visitFieldStmt(IGET, 3, 5, new Field("LJSResponseTest;", "methodName", "Ljava/lang/String;"));
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;", "put",
                        new String[] { "Ljava/lang/String;", "Ljava/lang/Object;" }, "Lorg/json/JSONObject;"));

                code.visitConstStmt(CONST_STRING, 2, "result");
                code.visitFieldStmt(IGET, 3, 5, new Field("LJSResponseTest;", "result", "I"));
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;", "put",
                        new String[] { "Ljava/lang/String;", "I" }, "Lorg/json/JSONObject;"));

                code.visitFieldStmt(IGET, 2, 5, new Field("LJSResponseTest;", "response", "Ljava/lang/Object;"));
                code.visitJumpStmt(IF_EQZ, 2, -1, L3);

                code.visitConstStmt(CONST_STRING, 2, "response");
                code.visitFieldStmt(IGET, 3, 5, new Field("LJSResponseTest;", "response", "Ljava/lang/Object;"));
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;", "put",
                        new String[] { "Ljava/lang/String;", "Ljava/lang/Object;" }, "Lorg/json/JSONObject;"));
                code.visitLabel(L1);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1 }, new Method("Lorg/json/JSONObject;", "toString",
                        new String[] {}, "Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT_OBJECT, 2);
                code.visitJumpStmt(GOTO, -1, -1, L9);
                code.visitLabel(L3);
                code.visitFieldStmt(IGET, 2, 5, new Field("LJSResponseTest;", "dataResponse", "[B"));
                code.visitJumpStmt(IF_EQZ, 2, -1, L5);

                code.visitConstStmt(CONST_STRING, 2, "response");
                code.visitFieldStmt(IGET, 3, 5, new Field("LJSResponseTest;", "dataResponse", "[B"));
                code.visitMethodStmt(INVOKE_STATIC, new int[] { 3 }, new Method("LBase64;", "encode",
                        new String[] { "[B" }, "Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT, 3);
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 1, 2, 3 }, new Method("Lorg/json/JSONObject;", "put",
                        new String[] { "Ljava/lang/String;", "Ljava/lang/Object;" }, "Lorg/json/JSONObject;"));
                code.visitLabel(L4);
                code.visitJumpStmt(GOTO, -1, -1, L1);
                code.visitLabel(L2);
                code.visitStmt1R(MOVE_EXCEPTION, 2);
                code.visitStmt2R(MOVE, 0, 2);

                code.visitConstStmt(CONST_STRING, 2, "MillennialMediaSDK");
                code.visitMethodStmt(INVOKE_VIRTUAL, new int[] { 0 }, new Method("Lorg/json/JSONException;",
                        "getMessage", new String[] {}, "Ljava/lang/String;"));
                code.visitStmt1R(MOVE_RESULT, 3);
                code.visitMethodStmt(INVOKE_STATIC, new int[] { 2, 3 }, new Method("Landroid/util/Log;", "e",
                        new String[] { "Ljava/lang/String;", "Ljava/lang/String;" }, "I"));

                code.visitConstStmt(CONST_STRING, 2, "");
                code.visitStmt2R(MOVE, 2, 4);
                code.visitJumpStmt(GOTO, -1, -1, L9);
                code.visitLabel(L5);
                code.visitConstStmt(CONST_STRING, 2, "");
                code.visitLabel(L6);
                code.visitStmt2R(MOVE, 2, 4);
                code.visitJumpStmt(GOTO, -1, -1, L9);

                code.visitEnd();
            }
            mv.visitEnd();
        }
    }
}
