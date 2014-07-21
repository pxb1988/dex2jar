package com.googlecode.dex2jar.test;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.Assert;
import org.junit.Test;

import static com.googlecode.d2j.reader.Op.*;

public class AutoCastTest implements DexConstants {

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
                code.visitRegister(3);

                code.visitMethodStmt(INVOKE_SUPER, new int[] { 2 }, new Method("Ljava/lang/Object;", "<init>",
                        new String[] {}, "V"));
                code.visitFieldStmt(SGET_BOOLEAN, 0, -1, f);
                code.visitConstStmt(CONST, 1, 0xffFFffFF);
                code.visitStmt3R(ADD_INT, 0, 0, 1);
                code.visitFieldStmt(SPUT_SHORT, 0, -1, f);
                code.visitStmt0R(RETURN_VOID);
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

        // it's already ok to run on JVM and able to convert to dex,
        // // check for I2S instruction
        // ClassReader cr = new ClassReader(data);
        // ClassNode cn = new ClassNode();
        // cr.accept(cn, 0);
        // boolean find = false;
        // for (Object m : cn.methods) {
        // MethodNode method = (MethodNode) m;
        // for (AbstractInsnNode p = method.instructions.getFirst(); p != null; p = p.getNext()) {
        // if (p.getOpcode() == Opcodes.I2S) {
        // find = true;
        // break;
        // }
        // }
        // }
        // Assert.assertTrue("we need an I2S instruction", find);

    }
}
