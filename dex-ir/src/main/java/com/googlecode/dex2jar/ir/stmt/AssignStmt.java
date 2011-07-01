package com.googlecode.dex2jar.ir.stmt;

import java.util.Map;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.ValueBox;

public class AssignStmt extends Stmt {

    public ValueBox left;
    public ValueBox right;

    public AssignStmt(ST type, ValueBox left, ValueBox right) {
        super(type);
        this.left = left;
        this.right = right;
    }

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
