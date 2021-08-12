package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

public final class Stmts {

    private Stmts() {
        throw new UnsupportedOperationException();
    }

    public static AssignStmt nAssign(Value left, Value right) {
        return new AssignStmt(ST.ASSIGN, left, right);
    }

    public static AssignStmt nFillArrayData(Value left, Value arrayData) {
        return new AssignStmt(ST.FILL_ARRAY_DATA, left, arrayData);
    }

    public static GotoStmt nGoto(LabelStmt target) {
        return new GotoStmt(target);
    }

    public static AssignStmt nIdentity(Value local, Value identityRef) {
        return new AssignStmt(ST.IDENTITY, local, identityRef);
    }

    public static IfStmt nIf(Value a, LabelStmt target) {
        return new IfStmt(ST.IF, a, target);
    }

    public static LabelStmt nLabel() {
        return new LabelStmt();
    }

    public static UnopStmt nLock(Value op) {
        return new UnopStmt(ST.LOCK, op);
    }

    public static LookupSwitchStmt nLookupSwitch(Value key, int[] lookupValues, LabelStmt[] targets, LabelStmt target) {
        return new LookupSwitchStmt(key, lookupValues, targets, target);
    }

    public static NopStmt nNop() {
        return new NopStmt();
    }

    public static UnopStmt nReturn(Value op) {
        return new UnopStmt(ST.RETURN, op);
    }

    public static ReturnVoidStmt nReturnVoid() {
        return new ReturnVoidStmt();
    }

    public static TableSwitchStmt nTableSwitch(Value key, int lowIndex, LabelStmt[] targets,
                                               LabelStmt target) {
        return new TableSwitchStmt(key, lowIndex, targets, target);
    }

    public static UnopStmt nThrow(Value op) {
        return new UnopStmt(ST.THROW, op);
    }

    public static UnopStmt nUnLock(Value op) {
        return new UnopStmt(ST.UNLOCK, op);
    }

    public static VoidInvokeStmt nVoidInvoke(Value op) {
        return new VoidInvokeStmt(op);
    }

}
