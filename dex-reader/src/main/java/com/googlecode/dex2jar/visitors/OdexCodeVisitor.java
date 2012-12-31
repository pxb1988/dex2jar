package com.googlecode.dex2jar.visitors;

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
     * OP_THROW_VERIFICATION_ERROR
     * 
     * @param opcode
     * @param cause
     *            {@link #VerificatioError_None}
     * @param ref
     */
    void visitReturnStmt(int opcode, int cause, Object ref);

    /**
     * OP_EXECUTE_INLINE
     * 
     * OP_INVOKE_SUPER_QUICK
     * 
     * OP_INVOKE_VIRTUAL_QUICK
     * 
     * 
     * @param opcode
     * @param args
     * @param a
     *            vtaboff,inline
     */
    void visitMethodStmt(int opcode, int[] args, int a);

    /**
     * OP_IGET_QUICK
     * 
     * OP_IPUT_QUICK
     * 
     * @param opcode
     * @param fromOrToReg
     * @param objReg
     * @param field
     * @param xt
     */
    void visitFieldStmt(int opcode, int fromOrToReg, int objReg, int fieldoff, int xt);

}
