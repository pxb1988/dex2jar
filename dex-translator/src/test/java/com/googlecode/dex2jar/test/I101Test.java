package com.googlecode.dex2jar.test;

import static com.googlecode.dex2jar.DexOpcodes.ACC_PUBLIC;
import static com.googlecode.dex2jar.DexOpcodes.ACC_STATIC;
import static com.googlecode.dex2jar.DexOpcodes.OP_CONST_STRING;
import static com.googlecode.dex2jar.DexOpcodes.OP_INVOKE_VIRTUAL;
import static com.googlecode.dex2jar.DexOpcodes.OP_MOVE_EXCEPTION;
import static com.googlecode.dex2jar.DexOpcodes.OP_RETURN_VOID;

import org.junit.Test;
import org.objectweb.asm.ClassReader;

import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.v3.V3;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class I101Test {

    public static void a(DexClassVisitor cv) {
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_STATIC, new Method("La;", "b", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitArguments(2, new int[] {});
        DexLabel L0 = new DexLabel();
        DexLabel L1 = new DexLabel();
        DexLabel L2 = new DexLabel();
        code.visitTryCatch(L0, L1, L2, "Lsome/Exception;");

        code.visitLabel(L0);
        code.visitConstStmt(OP_CONST_STRING, 0, "abc", 2);
        code.visitLabel(L1);

        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 0 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitReturnStmt(OP_RETURN_VOID);
        code.visitLabel(L2);
        code.visitMoveStmt(OP_MOVE_EXCEPTION, 1, 2);
        code.visitMethodStmt(OP_INVOKE_VIRTUAL, new int[] { 1 }, new Method("Ljava/lang/String;", "toString",
                new String[] {}, "Ljava/lang/String;"));
        code.visitReturnStmt(OP_RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();
    }

    @Test
    public void test() throws Exception {
        TestDexClassV cv = new TestDexClassV("Lt", V3.OPTIMIZE_SYNCHRONIZED | V3.TOPOLOGICAL_SORT);
        a(cv);
        byte[] data = cv.toByteArray();
        ClassReader cr = new ClassReader(data);
        TestUtils.verify(cr);
        // FIXME java.lang.ClassFormatError: Illegal exception table range in class file Lt
        // CL cl = new CL();
        // cl.xxxDefine("Lt", data);
    }

    static class CL extends ClassLoader {
        public Class<?> xxxDefine(String type, byte[] data) {
            return super.defineClass(type, data, 0, data.length);
        }
    }
}
