package pxb.xjimple.stmt;

import org.objectweb.asm.Label;

import pxb.xjimple.Value;
import pxb.xjimple.stmt.Stmt.ST;

public final class Stmts {

    public static AssignStmt nAssign(Value left, Value right) {
        return new AssignStmt(ST.ASSIGN, left, right);
    }

    public static JumpStmt nGoto(LabelStmt target) {
        return new JumpStmt(ST.GOTO, target);
    }

    public static LabelStmt nLabel(Label label) {
        return new LabelStmt(label);
    }

    public static LabelStmt nLabel() {
        return new LabelStmt(new Label());
    }

    public static AssignStmt nIdentity(Value local, Value identityRef) {
        return new AssignStmt(ST.IDENTITY, local, identityRef);
    }

    public static JumpStmt nIf(Value a, LabelStmt target) {
        return new JumpStmt(ST.IF, a, target);
    }

    public static LookupSwitchStmt nLookupSwitch(Value key, Value[] lookupValues, LabelStmt[] targets, LabelStmt target) {
        return new LookupSwitchStmt(key, lookupValues, targets, target);
    }

    public static NopStmt nNop() {
        return new NopStmt();
    }

    public static TableSwitchStmt nTableSwitch(Value key, int lowIndex, int highIndex, LabelStmt[] targets,
            LabelStmt target) {
        return new TableSwitchStmt(key, lowIndex, highIndex, targets, target);
    }

    private Stmts() {
    }
}
