package pxb.xjimple.stmt;

import java.util.Map;

import pxb.xjimple.Value;
import pxb.xjimple.ValueBox;

public class AssignStmt extends Stmt {

    public ValueBox left;
    public ValueBox right;

    public AssignStmt(ST type, Value left, Value right) {
        super(type);
        this.left = new ValueBox(left);
        this.right = new ValueBox(right);
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        return new AssignStmt(st, left.value, right.value);
    }

    public String toString() {
        switch (st) {
        case ASSIGN:
            return left + " = " + right;
        case IDENTITY:
            return left + " := " + right;
        }
        return super.toString();
    }

}
