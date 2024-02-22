package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

/**
 * Represent a NOP statement
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 * @see ST#NOP
 */
public class NopStmt extends E0Stmt {

    public NopStmt() {
        super(ST.NOP);
    }

    @Override
    public Stmt clone(LabelAndLocalMapper mapper) {
        return new NopStmt();
    }

    @Override
    public String toString() {
        return "NOP";
    }

}
