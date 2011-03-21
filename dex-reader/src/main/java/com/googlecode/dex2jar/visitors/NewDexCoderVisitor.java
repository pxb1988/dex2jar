package com.googlecode.dex2jar.visitors;

import org.objectweb.asm.Label;

import com.googlecode.dex2jar.Field;

public interface NewDexCoderVisitor {

    /**
     * OP_GOTO
     * 
     * @param opGoto
     * @param label
     */
    void visitJumpStmt(int opGoto, Label label);

    /**
     * {@link #OP_RETURN_VOID}
     * 
     * @param opcode
     */
    void visitReturnStmt(int opcode);

    /**
     * OP_CONST
     * 
     * @param opConst
     * @param a
     * @param b
     */
    void visitConstStmt(int opConst, int toReg, Object value);

    void visitMoveStmt(int opConst, int toReg);

    void visitReturnStmt(int opConst, int reg);

    void visitMonitorStmt(int opcode, int reg);

    void visitMoveStmt(int opcode, int toReg, int fromReg);

    void visitUnopStmt(int opcode, int toReg, int fromReg);

    void visitBinopStmt(int opcode, int toReg, int r1, int r2);

    void visitClassStmt(int opCheckCast, int saveTo, String type);

    void visitFieldStmt(int opcode, int fromOrToReg, Field field);

    /**
     * IFZ
     * 
     * @param opConst
     * @param reg
     * @param label
     */
    void visitJumpStmt(int opConst, int reg, Label label);

    void visitLookupSwitchStmt(int opcode, int aA, Label label, int[] cases, Label[] labels);

}
