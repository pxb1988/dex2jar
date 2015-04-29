/*
 * dex2jar - Tools to work with android .dex and java .class files
 * Copyright (c) 2009-2012 Panxiaobo
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

import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexMethodNode;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.PrintWriter;
import java.io.StringWriter;

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
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(Opcodes.ATHROW);
    }
}
