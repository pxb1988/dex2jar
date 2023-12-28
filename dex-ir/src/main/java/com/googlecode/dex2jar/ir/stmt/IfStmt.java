package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

/**
 * Represent a IF statement
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev: 9fd8005bbaa4 $
 * @see ST#IF
 */
public class IfStmt extends E1Stmt implements JumpStmt {

    public LabelStmt target;

    public LabelStmt getTarget() {
        return target;
    }

    public void setTarget(LabelStmt target) {
        this.target = target;
    }

    public IfStmt(ST type, Value condition, LabelStmt target) {
        super(type, condition);
        this.target = target;
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        LabelStmt nTarget = mapper.map(target);
        return new IfStmt(st, op.clone(mapper), nTarget);
    }

    @Override
    public String toString() {
        return "if " + op + " GOTO " + target.getDisplayName();
    }

}
