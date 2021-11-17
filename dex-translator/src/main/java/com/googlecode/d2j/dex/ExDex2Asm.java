package com.googlecode.d2j.dex;

import com.googlecode.d2j.DexException;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.dex2jar.tools.Constants;
import org.objectweb.asm.AsmBridge;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

public class ExDex2Asm extends Dex2Asm {

    protected final DexExceptionHandler exceptionHandler;

    public ExDex2Asm(DexExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void convertCode(DexMethodNode methodNode, MethodVisitor mv, ClzCtx clzCtx) {
        MethodVisitor mw = AsmBridge.searchMethodWriter(mv);
        MethodNode mn = new MethodNode(Constants.ASM_VERSION, methodNode.access, methodNode.method.getName(),
                methodNode.method.getDesc(), null, null);
        try {
            super.convertCode(methodNode, mn, clzCtx);
        } catch (Exception ex) {
            if (exceptionHandler == null) {
                new DexException(ex, "Failed to convert code for %s", methodNode.method)
                        .printStackTrace();
            } else {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                exceptionHandler.handleMethodTranslateException(methodNode.method, methodNode, mn, ex);
            }
        }
        // code convert ok, copy to MethodWriter and check for Size
        mn.accept(mv);
        if (mw != null) {
            try {
                AsmBridge.sizeOfMethodWriter(mw);
            } catch (Exception ex) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                if (exceptionHandler == null) {
                    new DexException(ex, "Failed to convert code for %s", methodNode.method)
                            .printStackTrace();
                } else {
                    exceptionHandler.handleMethodTranslateException(methodNode.method, methodNode, mn, ex);
                }
                AsmBridge.replaceMethodWriter(mw, mn);
            }
        }
    }

}
