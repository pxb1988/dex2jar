package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.ACC_PUBLIC;
import static com.googlecode.dex2jar.DexOpcodes.ACC_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_CONST_STRING;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_VIRTUAL;
import static com.googlecode.dex2jar.DexOpcodes.OP_MONITOR_EXIT;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_EXCEPTION;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN_VOID;
import static com.googlecode.dex2jar.DexOpcodes.OP_THROW;
import static com.googlecode.dex2jar.DexOpcodes.TYPE_OBJECT;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.DexOpcodes;
import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.v3.V3;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class OptSyncTest {
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
     * 
     * @param cv
     */
    public static void a(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "a", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        int v0 = 0;
        int v1 = 1;
        DexLabel try_start = new DexLabel();
        DexLabel try_end = new DexLabel();
        DexLabel catch_a = new DexLabel();

        code.visitTryCatch(try_start, try_end, new DexLabel[] { catch_a }, new String[] { null });
        code.visitArguments(2, new int[] {});
        code.visitFieldStmt(DexOpcodes.OP_SGET, v0, new Field("Ljava/lang/System;", "out", "Ljava/io/PrintStream;"),
                DexOpcodes.TYPE_OBJECT);
        code.visitLabel(try_start);
        code.visitMonitorStmt(DexOpcodes.OP_MONITOR_ENTER, v0);
        code.visitConstStmt(OP_CONST_STRING, v1, "haha", TYPE_OBJECT);
        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { v0, v1 }, new Method("Ljava/io/PrintString;", "println",
                new String[] { "Ljava/lang/String;" }, "V"));
        code.visitLabel(try_end);
        code.visitMonitorStmt(OP_MONITOR_EXIT, v0);
        code.visitReturnStmt(OP_RETURN_VOID);
        code.visitLabel(catch_a);
        code.visitMoveStmt(OP_MOVE_EXCEPTION, v1, TYPE_OBJECT);
        code.visitMonitorStmt(OP_MONITOR_EXIT, v0);
        code.visitReturnStmt(OP_THROW, v1, TYPE_OBJECT);
        code.visitEnd();
        mv.visitEnd();
    }

    // FIXME this test case shows a special scenario which case the class verify fail
    // @Test
    public void test() throws IllegalArgumentException, IllegalAccessException, AnalyzerException {
        TestDexClassV cv = new TestDexClassV("Lt", V3.OPTIMIZE_SYNCHRONIZED | V3.TOPOLOGICAL_SORT);
        a(cv);
        ClassReader cr = new ClassReader(cv.toByteArray());
        TestUtils.verify(cr);
    }
}
