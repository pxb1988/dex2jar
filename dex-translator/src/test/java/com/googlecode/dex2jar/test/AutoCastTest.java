package com.googlecode.dex2jar.test;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.OdexOpcodes;
import com.googlecode.dex2jar.visitors.DexClassVisitor;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.DexFieldVisitor;
import com.googlecode.dex2jar.visitors.DexMethodVisitor;

public class AutoCastTest implements OdexOpcodes {
    /**
     * generate code, it works fine on JVM, but fails on Dalvik VM
     * 
     * <pre>
     * class a {
     *     private static short theField;
     * 
     *     public a() {
     *         theField = 0xffFFffFF + theField;// the 0xffFFffFF is not casted
     *     }
     * }
     * </pre>
     * 
     * @param cv
     */
    public static void strict(DexClassVisitor cv) {
        Field f = new Field("La;", "theField", "S");
        DexMethodVisitor mv = cv.visitMethod(ACC_PUBLIC, new Method("La;", "<init>", new String[] {}, "V"));
        if (mv != null) {
            DexCodeVisitor code = mv.visitCode();
            if (code != null) {
                code.visitArguments(3, new int[] { 2 });
                code.visitMethodStmt(OP_INVOKE_SUPER, new int[] { 2 }, new Method("Ljava/lang/Object;", "<init>",
                        new String[] {}, "V"));
                code.visitFieldStmt(OP_SGET, 0, f, TYPE_SHORT);
                code.visitConstStmt(OP_CONST, 1, 0xffFFffFF, TYPE_SINGLE);
                code.visitBinopStmt(OP_ADD, 0, 0, 1, TYPE_INT);
                code.visitFieldStmt(OP_SPUT, 0, f, TYPE_SHORT);
                code.visitReturnStmt(OP_RETURN_VOID);
                code.visitEnd();
            }
            mv.visitEnd();
        }
        DexFieldVisitor fv = cv.visitField(ACC_PRIVATE | ACC_STATIC, f, 0);
        if (fv != null) {
            fv.visitEnd();
        }
    }

    @Test
    public void test() throws Exception {
        byte[] data = TestUtils.testDexASMifier(getClass(), "strict", "a");
        Class<?> clz = TestUtils.defineClass("a", data);
        Object c = clz.newInstance();
        Assert.assertNotNull(c);
        java.lang.reflect.Field f = clz.getDeclaredField("theField");
        f.setAccessible(true);
        Short r = (Short) f.get(null);
        Assert.assertEquals(-1, r.intValue());

        // check for I2S instruction
        ClassReader cr = new ClassReader(data);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        boolean find = false;
        for (Object m : cn.methods) {
            MethodNode method = (MethodNode) m;
            for (AbstractInsnNode p = method.instructions.getFirst(); p != null; p = p.getNext()) {
                if (p.getOpcode() == Opcodes.I2S) {
                    find = true;
                    break;
                }
            }
        }
        Assert.assertTrue("we need an I2S instruction", find);

    }
}
