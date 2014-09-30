package com.googlecode.dex2jar.test;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.googlecode.d2j.DexConstants.*;
import static com.googlecode.d2j.reader.Op.*;

@RunWith(DexTranslatorRunner.class)
public class I121Test {

    @Test
    public static void i121(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "a", new String[] {
                "Ljava/lang/String;", "Ljava/lang/String;" }, "Ljava/lang/String;"));
        DexCodeVisitor code = mv.visitCode();
        int p0 = 2;
        int p1 = 3;
        int v0 = 0;
        int v1 = 1;

        DexLabel cond_7 = new DexLabel();
        DexLabel try_start_2 = new DexLabel();
        DexLabel try_end_9 = new DexLabel();
        DexLabel catch_a = new DexLabel();
        DexLabel goto_2 = new DexLabel();

        code.visitTryCatch(try_start_2, try_end_9, new DexLabel[] { catch_a },
                new String[] { "Ljava/io/UnsupportedEncodingException;" });

        code.visitRegister(4);

        code.visitJumpStmt(IF_EQZ, p1, -1, cond_7);

        code.visitLabel(goto_2);
        code.visitLabel(try_start_2);

        code.visitMethodStmt(INVOKE_STATIC, new int[] { p0, p1 }, new Method("Ljava/net/URLEncoder;", "encode",
                new String[] { "Ljava/lang/String;", "Ljava/lang/String;" }, "Ljava/lang/String;"));
        code.visitStmt1R(MOVE_RESULT_OBJECT, v0);
        code.visitStmt1R(RETURN_OBJECT, v0);
        code.visitLabel(cond_7);
        code.visitConstStmt(CONST_STRING, p1, "ISO-8859-1");

        code.visitLabel(try_end_9);
        code.visitJumpStmt(GOTO, -1, -1, goto_2);
        code.visitLabel(catch_a);
        code.visitStmt1R(MOVE_EXCEPTION, v0);
        code.visitTypeStmt(NEW_INSTANCE, v1, -1, "Ljava/lang/IllegalArgumentException;");
        code.visitMethodStmt(INVOKE_DIRECT, new int[] { v1, v0 }, new Method("Ljava/lang/IllegalArgumentException;",
                "<init>", new String[] { "Ljava/lang/Throwable;" }, "V"));
        code.visitStmt1R(THROW, v1);
        code.visitEnd();
        mv.visitEnd();
    }
}
