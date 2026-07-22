package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

/**
 * Represent a RETURN_VOID statement
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 * @see ST#RETURN_VOID
 */
public class ReturnVoidStmt extends E0Stmt {

    public ReturnVoidStmt() {
        super(ST.RETURN_VOID);
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        return new ReturnVoidStmt();
    }

    @Override
    public String toString() {
        return "return";
    }

}
