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
package org.objectweb.asm;

import java.lang.reflect.Field;
import org.objectweb.asm.tree.MethodNode;

public class AsmBridge {
    public static MethodVisitor searchMethodWriter(MethodVisitor methodVisitor) {
        while (methodVisitor != null && !(methodVisitor instanceof MethodWriter)) {
            methodVisitor = methodVisitor.mv;
        }
        return methodVisitor;
    }

    public static int sizeOfMethodWriter(MethodVisitor methodVisitor) {
        MethodWriter mw = (MethodWriter) methodVisitor;
        return mw.computeMethodInfoSize();
    }

    private static void removeMethodWriter(MethodWriter methodWriter) {
        SymbolTable symbolTable;
        ClassWriter classWriter;
        MethodWriter firstMethodWriter, lastMethodWriter;

        // Get the SymbolTable for accessing ClassWriter
        try {
            Field stField = methodWriter.getClass().getDeclaredField("symbolTable");
            stField.setAccessible(true);
            symbolTable = (SymbolTable) stField.get(methodWriter);

            // Get ClassWriter object from methodWriter's SymbolTable
            classWriter = symbolTable.classWriter;

            Field fmField = classWriter.getClass().getDeclaredField("firstMethod");
            fmField.setAccessible(true);
            firstMethodWriter = (MethodWriter) fmField.get(classWriter);

            Field lmField = classWriter.getClass().getDeclaredField("lastMethod");
            lmField.setAccessible(true);
            lastMethodWriter = (MethodWriter) lmField.get(classWriter);

            // mv must be the last element
            if (firstMethodWriter == methodWriter) {
                fmField.set(classWriter, null);
                if (lastMethodWriter == methodWriter) {
                    lmField.set(classWriter, null);
                }
            } else {
                while (firstMethodWriter != null) {
                    if (firstMethodWriter.mv == methodWriter) {
                        firstMethodWriter.mv = methodWriter.mv;
                        if (lastMethodWriter == methodWriter) {
                            lmField.set(classWriter, firstMethodWriter);
                        }
                        break;
                    } else {
                        firstMethodWriter = (MethodWriter) firstMethodWriter.mv;
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException exc) {
            exc.printStackTrace();
        }
    }

    public static void replaceMethodWriter(MethodVisitor methodVisitor, MethodNode methodNode) {
        MethodWriter methodWriter = (MethodWriter) methodVisitor;
        SymbolTable symbolTable;
        ClassWriter classWriter;

        // Get the SymbolTable for accessing ClassWriter
        try {
            Field stField = methodWriter.getClass().getDeclaredField("symbolTable");
            stField.setAccessible(true);
            symbolTable = (SymbolTable) stField.get(methodWriter);

            // Get ClassWriter object from methodWriter's SymbolTable
            classWriter = symbolTable.classWriter;

            methodNode.accept(classWriter);
            removeMethodWriter(methodWriter);
        } catch (IllegalAccessException | NoSuchFieldException exc) {
            exc.printStackTrace();
        }
    }
}
