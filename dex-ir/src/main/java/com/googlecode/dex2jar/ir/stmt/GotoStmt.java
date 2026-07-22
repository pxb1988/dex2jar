package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

/**
 * Represent a GOTO statement
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @see ST#GOTO
 */
public class GotoStmt extends E0Stmt implements JumpStmt {

    public LabelStmt target;

    public LabelStmt getTarget() {
        return target;
    }

    public void setTarget(LabelStmt target) {
        this.target = target;
    }

    public GotoStmt(LabelStmt target) {
        super(ST.GOTO);
        this.target = target;
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        LabelStmt nTarget = mapper.map(target);
        return new GotoStmt(nTarget);
    }

    @Override
    public String toString() {
        return "GOTO " + target.getDisplayName();
    }

}
