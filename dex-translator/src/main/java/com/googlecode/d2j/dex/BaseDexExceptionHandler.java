package com.googlecode.d2j.dex;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexMethodNode;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BaseDexExceptionHandler implements DexExceptionHandler {

    @Override
    public void handleFileException(Exception e) {
        e.printStackTrace(System.err);
    }

    @Override
    public void handleMethodTranslateException(Method method, DexMethodNode methodNode, MethodVisitor mv, Exception e) {
        // replace the generated code with
        // 'return new RuntimeException("D2jFail translate: xxxxxxxxxxxxx");'
        StringWriter s = new StringWriter();
        s.append("d2j fail translate: ");
        e.printStackTrace(new PrintWriter(s));
        String msg = s.toString();
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(msg);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V",
                false);
        mv.visitInsn(Opcodes.ATHROW);
    }

}
