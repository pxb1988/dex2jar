package com.googlecode.dex2jar.test;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.googlecode.d2j.DexConstants.ACC_PUBLIC;
import static com.googlecode.d2j.DexConstants.ACC_STATIC;
import static com.googlecode.d2j.reader.Op.CONST_STRING;
import static com.googlecode.d2j.reader.Op.INVOKE_VIRTUAL;
import static com.googlecode.d2j.reader.Op.MONITOR_ENTER;
import static com.googlecode.d2j.reader.Op.MONITOR_EXIT;
import static com.googlecode.d2j.reader.Op.MOVE_EXCEPTION;
import static com.googlecode.d2j.reader.Op.RETURN_VOID;
import static com.googlecode.d2j.reader.Op.SGET_OBJECT;
import static com.googlecode.d2j.reader.Op.THROW;

@ExtendWith(DexTranslatorRunner.class)
public class OptSyncTest {

    public void a() {
        synchronized (System.out) {
            System.out.println();
        }
    }

    public void b() {
        PrintStream a = System.out;
        synchronized (a) {
            System.out.println();
        }
    }

    public void c() {
        Object a = null;
        synchronized (a) {
            System.out.println();
        }
    }

    /**
     * Generate the following code
     *
     * <pre>
     * public static void a() {
     * a0 = System.out
     * L0:
     * lock a0 <= a0 is inside a try-catch
     * a1="haha"
     * a0.println(a1)
     * L1:
     * unlock a0
     * return
     * L2:
     * a1 := @Exception
     * unlock a0
     * throw a1
     * =============
     * .catch L0 - L1 > L2 // all
     * }
     * </pre>
     */
    @Test
    public void test(DexClassNode cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "a", new String[]{}, "V"));
        DexCodeVisitor code = mv.visitCode();
        int v0 = 0;
        int v1 = 1;
        DexLabel try_start = new DexLabel();
        DexLabel try_end = new DexLabel();
        DexLabel catch_a = new DexLabel();

        code.visitTryCatch(try_start, try_end, new DexLabel[]{catch_a}, new String[]{null});
        code.visitRegister(2);
        code.visitFieldStmt(SGET_OBJECT, v0, -1, new Field("Ljava/lang/System;", "out", "Ljava/io/PrintStream;"));
        code.visitLabel(try_start);
        code.visitStmt1R(MONITOR_ENTER, v0);
        code.visitConstStmt(CONST_STRING, v1, "haha");
        code.visitMethodStmt(INVOKE_VIRTUAL, new int[]{v0, v1}, new Method("Ljava/io/PrintString;", "println",
                new String[]{"Ljava/lang/String;"}, "V"));
        code.visitLabel(try_end);
        code.visitStmt1R(MONITOR_EXIT, v0);
        code.visitStmt0R(RETURN_VOID);
        code.visitLabel(catch_a);
        code.visitStmt1R(MOVE_EXCEPTION, v1);
        code.visitStmt1R(MONITOR_EXIT, v0);
        code.visitStmt1R(THROW, v1);
        code.visitEnd();
        mv.visitEnd();
        cv.visitEnd();
    }

}
