package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.ACC_PUBLIC;
import static com.googlecode.dex2jar.DexOpcodes.ACC_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_CONST_STRING;
import static com.googlecode.dex2jar.DexOpcodes.OP_GOTO;
import static com.googlecode.dex2jar.DexOpcodes.OP_IF_EQZ;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_DIRECT;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_EXCEPTION;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_RESULT;
import static com.googlecode.dex2jar.DexOpcodes.OP_NEW_INSTANCE;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN;
import static com.googlecode.dex2jar.DexOpcodes.OP_THROW;
import static com.googlecode.dex2jar.DexOpcodes.TYPE_INT;
import static com.googlecode.dex2jar.DexOpcodes.TYPE_OBJECT;

import org.junit.Test;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class I121Test {

    public static void a(DexClassVisitor cv) {
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

        code.visitArguments(3, new int[] { p0, p1 });

        code.visitJumpStmt(OP_IF_EQZ, p1, cond_7);

        code.visitLabel(goto_2);
        code.visitLabel(try_start_2);

        code.visitMethodStmt(OP_INVOKE_STATIC, new int[] { p0, p1 }, new Method("Ljava/net/URLEncoder;", "encode",
                new String[] { "Ljava/lang/String;", "Ljava/lang/String;" }, "Ljava/lang/String;"));
        code.visitMoveStmt(OP_MOVE_RESULT, v0, TYPE_OBJECT);
        code.visitReturnStmt(OP_RETURN, v0, TYPE_OBJECT);
        code.visitLabel(cond_7);
        code.visitConstStmt(OP_CONST_STRING, p1, "ISO-8859-1", TYPE_OBJECT);

        code.visitLabel(try_end_9);
        code.visitJumpStmt(OP_GOTO, goto_2);
        code.visitLabel(catch_a);
        code.visitMoveStmt(OP_MOVE_EXCEPTION, v0, TYPE_OBJECT);
        code.visitClassStmt(OP_NEW_INSTANCE, v1, "Ljava/lang/IllegalArgumentException;");
        code.visitMethodStmt(OP_INVOKE_DIRECT, new int[] { v1, v0 }, new Method("Ljava/lang/IllegalArgumentException;",
                "<init>", new String[] { "Ljava/lang/Throwable;" }, "V"));
        code.visitReturnStmt(OP_THROW, v1, TYPE_INT);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public void test() throws Exception {
        TestUtils.testDexASMifier(getClass(), "a");
    }
}
