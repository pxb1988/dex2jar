package org.objectweb.asm;

import java.lang.reflect.Field;
import org.objectweb.asm.tree.MethodNode;

public final class AsmBridge {

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
        MethodWriter firstMethodWriter;
        MethodWriter lastMethodWriter;

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

    private AsmBridge() {
        throw new UnsupportedOperationException();
    }

}
