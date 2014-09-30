/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2014 Panxiaobo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.d2j.dex;

import org.objectweb.asm.AsmBridge;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import com.googlecode.d2j.DexException;
import com.googlecode.d2j.node.DexMethodNode;

public class ExDex2Asm extends Dex2Asm {
    final protected DexExceptionHandler exceptionHandler;

    public ExDex2Asm(DexExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void convertCode(DexMethodNode methodNode, MethodVisitor mv) {
        if (!AsmBridge.isMethodWriter(mv)) {
            throw new RuntimeException("We use a MethodWriter tricky here!");
        }
        MethodNode mn = new MethodNode(Opcodes.ASM4, methodNode.access, methodNode.method.getName(),
                methodNode.method.getDesc(), null, null);
        try {
            super.convertCode(methodNode, mn);
        } catch (Exception ex) {
            if (exceptionHandler == null) {
                throw new DexException(ex, "fail convert code for %s", methodNode.method);
            } else {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                exceptionHandler.handleMethodTranslateException(methodNode.method, methodNode, mn, ex);
            }
        }
        // code convert ok, copy to MethodWriter and check for Size
        mn.accept(mv);
        try {
            AsmBridge.sizeOfMethodWriter(mv);
        } catch (Exception ex) {
            mn.instructions.clear();
            mn.tryCatchBlocks.clear();
            exceptionHandler.handleMethodTranslateException(methodNode.method, methodNode, mn, ex);
            AsmBridge.replaceMethodWriter(mv, mn);
        }
    }
}
