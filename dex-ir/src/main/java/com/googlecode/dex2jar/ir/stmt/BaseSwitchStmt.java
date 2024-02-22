package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt.E1Stmt;

/**
 * Parent class of {@link LookupSwitchStmt} and {@link TableSwitchStmt}
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 */
public abstract class BaseSwitchStmt extends E1Stmt {

    public BaseSwitchStmt(ST type, Value op) {
        super(type, op);
    }

    public LabelStmt[] targets;

    public LabelStmt defaultTarget;

}
