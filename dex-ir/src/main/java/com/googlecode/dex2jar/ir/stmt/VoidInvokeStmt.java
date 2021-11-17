package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

/**
 * Represent a void-expr: the expr result is ignored.
 * possible op type: AbstractInvokeExpr, FieldExpr, or others
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 8da5a5faa6bd $
 * @see ST#VOID_INVOKE
 */
public class VoidInvokeStmt extends E1Stmt {

    public VoidInvokeStmt(Value op) {
        super(ST.VOID_INVOKE, op);
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        return new VoidInvokeStmt(op.clone(mapper));
    }

    @Override
    public String toString() {
        return "void " + op;
    }

}
