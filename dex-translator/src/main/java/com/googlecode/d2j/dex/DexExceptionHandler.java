package com.googlecode.d2j.dex;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexMethodNode;
import org.objectweb.asm.MethodVisitor;

public interface DexExceptionHandler {

    void handleFileException(Exception e);

    void handleMethodTranslateException(Method method, DexMethodNode methodNode, MethodVisitor mv, Exception e);

}
