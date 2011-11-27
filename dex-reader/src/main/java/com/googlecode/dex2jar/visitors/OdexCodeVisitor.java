package com.googlecode.dex2jar.visitors;

import com.googlecode.dex2jar.Field;
import com.googlecode.dex2jar.Method;

public interface OdexCodeVisitor extends DexCodeVisitor {

    int VerificatioError_None = 0;
    int VerificatioError_Generic = 1;
    int VerificatioError_NoClass = 2;
    int VerificatioError_NoField = 3;
    int VerificatioError_NoMethod = 4;
    int VerificatioError_AccessClass = 5;
    int VerificatioError_AccessField = 6;
    int VerificatioError_AccessMethod = 7;
    int VerificatioError_ClassChange = 8;
    int VerificatioError_Instantiation = 9;

    /**
     * 
     * @param opcode
     * @param cause
     *            {@link #VerificatioError_None}
     * @param ref
     */
    void visitReturnStmt(int opcode, int cause, Object ref);

    /**
     * quick or inline
     * 
     * @param opcode
     * @param args
     * @param a
     *            vtaboff,inline
     */
    void visitMethodStmt(int opcode, int[] args, int a);

    /**
     * s-quick
     * 
     * @param opcode
     * @param fromOrToReg
     * @param field
     */
    void visitFieldStmt(int opcode, int fromOrToReg, int fieldoff);

    /**
     * i-quick
     * 
     * @param opcode
     * @param fromOrToReg
     * @param objReg
     * @param field
     */
    void visitFieldStmt(int opcode, int fromOrToReg, int objReg, int fieldoff);

}
