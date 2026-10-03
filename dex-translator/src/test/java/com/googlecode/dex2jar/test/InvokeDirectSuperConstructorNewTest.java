package com.googlecode.dex2jar.test;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static com.googlecode.d2j.DexConstants.ACC_CONSTRUCTOR;
import static com.googlecode.d2j.DexConstants.ACC_PUBLIC;
import static com.googlecode.d2j.reader.Op.INVOKE_DIRECT;
import static com.googlecode.d2j.reader.Op.NEW_INSTANCE;
import static com.googlecode.d2j.reader.Op.RETURN_VOID;

public class InvokeDirectSuperConstructorNewTest {

    @Test
    public void newInstanceTypeSurvivesSuperConstructorCall() throws Exception {
        DexClassNode clz = new DexClassNode(ACC_PUBLIC, "LChild;", "Ljava/lang/Object;", null);
        DexMethodVisitor mv = clz.visitMethod(ACC_PUBLIC | ACC_CONSTRUCTOR,
                new Method("LChild;", "<init>", new String[] {}, "V"));
        DexCodeVisitor code = mv.visitCode();
        code.visitRegister(2);
        code.visitTypeStmt(NEW_INSTANCE, 0, -1, "LChild;");
        code.visitMethodStmt(INVOKE_DIRECT, new int[] { 0 },
                new Method("LBase;", "<init>", new String[] {}, "V"));
        code.visitStmt0R(RETURN_VOID);
        code.visitEnd();
        mv.visitEnd();

        byte[] data = TestUtils.translateAndCheck(clz);

        ClassNode cn = new ClassNode();
        new ClassReader(data).accept(cn, 0);
        MethodNode init = null;
        for (Object method : cn.methods) {
            MethodNode mn = (MethodNode) method;
            if ("<init>".equals(mn.name)) {
                init = mn;
                break;
            }
        }
        Assert.assertNotNull(init);

        TypeInsnNode newInsn = null;
        MethodInsnNode initCall = null;
        for (int i = 0; i < init.instructions.size(); i++) {
            if (init.instructions.get(i).getOpcode() == Opcodes.NEW) {
                newInsn = (TypeInsnNode) init.instructions.get(i);
            } else if (init.instructions.get(i).getOpcode() == Opcodes.INVOKESPECIAL) {
                initCall = (MethodInsnNode) init.instructions.get(i);
            }
        }

        Assert.assertNotNull(newInsn);
        Assert.assertNotNull(initCall);
        Assert.assertEquals("Child", newInsn.desc);
        Assert.assertEquals("Base", initCall.owner);
    }
}
