package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.OP_CONST;
import static com.googlecode.dex2jar.DexOpcodes.OP_GOTO;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_INTERFACE;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_VIRTUAL;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_EXCEPTION;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_RESULT;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN;
import static com.googlecode.dex2jar.DexOpcodes.OP_THROW;
import static com.googlecode.dex2jar.DexOpcodes.TYPE_OBJECT;
import static com.googlecode.dex2jar.DexOpcodes.TYPE_SINGLE;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class I160Test implements Opcodes {
    @Test
    public void test() throws Exception {
        TestUtils.testDexASMifier(getClass(), "i160");
    }

    public static void i160(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC, new Method("Landroid/net/VpnService;", "protect",
                new String[] { "I" }, "Z"));
        DexCodeVisitor code = mv.visitCode();
        code.visitArguments(6, new int[] { 4, 5 });
        DexLabel L0 = new DexLabel();
        DexLabel L1 = new DexLabel();
        DexLabel L2 = new DexLabel();
        DexLabel L3 = new DexLabel();
        code.visitTryCatch(L0, L1, new DexLabel[] { L2, L3 }, new String[] { "Ljava/lang/Exception;", null });
        DexLabel L4 = new DexLabel();
        DexLabel L5 = new DexLabel();
        DexLabel L6 = new DexLabel();
        code.visitTryCatch(L4, L5, new DexLabel[] { L6 }, new String[] { "Ljava/lang/Exception;" });
        DexLabel L7 = new DexLabel();
        DexLabel L8 = new DexLabel();
        DexLabel L9 = new DexLabel();
        code.visitTryCatch(L7, L8, new DexLabel[] { L9 }, new String[] { "Ljava/lang/Exception;" });
        code.visitConstStmt(OP_CONST, 0, Integer.valueOf(0), TYPE_SINGLE); // int: 0x00000000 float:0,000000
        code.visitLabel(L0);
        code.visitMethodStmt(OP_INVOKE_STATIC, new int[] { 5 }, new Method("Landroid/os/ParcelFileDescriptor;",
                "fromFd", new String[] { "I" }, "Landroid/os/ParcelFileDescriptor;"));
        code.visitMoveStmt(OP_MOVE_RESULT, 0, TYPE_OBJECT);
        code.visitMethodStmt(OP_INVOKE_STATIC, new int[] {}, new Method("Landroid/net/VpnService;", "getService",
                new String[] {}, "Landroid/net/IConnectivityManager;"));
        code.visitMoveStmt(OP_MOVE_RESULT, 2, TYPE_OBJECT);
        code.visitMethodStmt(OP_INVOKE_INTERFACE, new int[] { 2, 0 }, new Method("Landroid/net/IConnectivityManager;",
                "protectVpn", new String[] { "Landroid/os/ParcelFileDescriptor;" }, "Z"));
        code.visitLabel(L1);
        code.visitMoveStmt(OP_MOVE_RESULT, 2, TYPE_SINGLE);
        code.visitLabel(L4);
        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 0 }, new Method("Landroid/os/ParcelFileDescriptor;",
                "close", new String[] {}, "V"));
        DexLabel L10 = new DexLabel();
        code.visitLabel(L10);
        code.visitReturnStmt(OP_RETURN, 2, TYPE_SINGLE);
        code.visitLabel(L2);
        code.visitMoveStmt(OP_MOVE_EXCEPTION, 1, TYPE_OBJECT);
        code.visitConstStmt(OP_CONST, 2, Integer.valueOf(0), TYPE_SINGLE); // int: 0x00000000 float:0,000000
        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 0 }, new Method("Landroid/os/ParcelFileDescriptor;",
                "close", new String[] {}, "V"));
        code.visitLabel(L5);
        code.visitJumpStmt(OP_GOTO, L10);
        code.visitLabel(L6);
        code.visitMoveStmt(OP_MOVE_EXCEPTION, 3, TYPE_OBJECT);
        code.visitJumpStmt(OP_GOTO, L10);
        code.visitLabel(L3);
        code.visitMoveStmt(OP_MOVE_EXCEPTION, 2, TYPE_OBJECT);
        code.visitLabel(L7);
        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 0 }, new Method("Landroid/os/ParcelFileDescriptor;",
                "close", new String[] {}, "V"));
        code.visitLabel(L8);
        code.visitReturnStmt(OP_THROW, 2, TYPE_OBJECT);
        code.visitLabel(L9);
        code.visitMoveStmt(OP_MOVE_EXCEPTION, 3, TYPE_OBJECT);
        code.visitJumpStmt(OP_GOTO, L8);
        code.visitEnd();
        mv.visitEnd();
    }
}
