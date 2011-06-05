package pxb.xjimple.stmt;

import java.util.Map;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class UnopStmt extends Stmt {

    public ValueBox op;

    public UnopStmt(ST type, Value op) {
        super(type);
        this.op = new ValueBox(op);
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        return new UnopStmt(st, op.value);
    }

    public String toString() {
        switch (super.st) {
        case LOCK:
            return "lock " + op;
        case UNLOCK:
            return "unlock " + op;
        case THROW:
            return "throw " + op;
        case RETURN:
            return "return " + op;
        }
        return super.toString();
    }

}
