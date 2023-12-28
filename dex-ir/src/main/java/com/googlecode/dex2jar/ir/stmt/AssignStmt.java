package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.E2Stmt;

/**
 * Represent an Assign statement
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 8da5a5faa6bd $
 * @see ST#ASSIGN
 * @see ST#IDENTITY
 * @see ST#FILL_ARRAY_DATA
 */
public class AssignStmt extends E2Stmt {

    public AssignStmt(ST type, Value left, Value right) {
        super(type, left, right);
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        return new AssignStmt(st, op1.clone(mapper), op2.clone(mapper));
    }

    @Override
    public String toString() {
        switch (st) {
        case ASSIGN:
            return op1 + " = " + op2;
        case LOCAL_START:
        case IDENTITY:
            return op1 + " := " + op2;
        case FILL_ARRAY_DATA:
            return op1 + " <- " + op2;
        default:
        }
        return super.toString();
    }

}
