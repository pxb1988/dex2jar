package com.googlecode.dex2jar.ir.stmt;

import org.objectweb.asm.Label;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;

public final class Stmts {

    public static AssignStmt nAssign(Value left, Value right) {
        return new AssignStmt(ST.ASSIGN, left, right);
    }

    public static JumpStmt nGoto(LabelStmt target) {
        return new JumpStmt(ST.GOTO, target);
    }

    public static AssignStmt nIdentity(Value local, Value identityRef) {
        return new AssignStmt(ST.IDENTITY, local, identityRef);
    }

    public static JumpStmt nIf(Value a, LabelStmt target) {
        return new JumpStmt(ST.IF, a, target);
    }
    public static LabelStmt nLabel() {
        return new LabelStmt(new Label());
    }
    public static LabelStmt nLabel(Label label) {
        return new LabelStmt(label);
    }

    public static UnopStmt nLock(Value op) {
        return new UnopStmt(ST.LOCK, op);
    }

    public static LookupSwitchStmt nLookupSwitch(Value key, Value[] lookupValues, LabelStmt[] targets, LabelStmt target) {
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

    public static TableSwitchStmt nTableSwitch(Value key, int lowIndex, int highIndex, LabelStmt[] targets,
            LabelStmt target) {
        return new TableSwitchStmt(key, lowIndex, highIndex, targets, target);
    }

    public static UnopStmt nThrow(Value op) {
        return new UnopStmt(ST.THROW, op);
    }

    public static UnopStmt nUnLock(Value op) {
        return new UnopStmt(ST.UNLOCK, op);
    }

    private Stmts() {
    }
}
