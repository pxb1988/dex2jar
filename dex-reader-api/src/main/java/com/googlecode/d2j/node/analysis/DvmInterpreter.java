package com.googlecode.d2j.node.analysis;

import com.googlecode.d2j.node.insn.DexStmtNode;

import java.util.List;

public abstract class DvmInterpreter<V> {
    
    /**
     * CONST*
     * SGET*
     * NEW
     *
     *
     */
    public abstract V newOperation(DexStmtNode insn) ;

    /**
     * MOVE*
     */
    public abstract V copyOperation(DexStmtNode insn, V value) ;

    /**
     * NEG*
     * *_TO_*
     * IF_*Z
     * *SWITCH
     * IGET*
     * NEW_ARRAY
     * MONITOR_*
     * CHECK_CAST
     * INSTANCEOF

     */
    public abstract V unaryOperation(DexStmtNode insn, V value);

    /**
     * AGET*
     * IPUT*
     *
     */
    public abstract V binaryOperation(DexStmtNode insn, V value1, V value2);

    /**
     * APUT
     */
    public abstract V ternaryOperation(DexStmtNode insn, V value1,
                                       V value2, V value3) ;

    /**
     * INVOKE*
     * MULTIANEWARRAY
     * FilledNewArrayStmt
     */
    public abstract V naryOperation(DexStmtNode insn,
                                    List<? extends V> values) ;

    /**
     * RETURN*
     */
    public abstract void returnOperation(DexStmtNode insn, V value);
}
