package com.googlecode.dex2jar.v3;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.Transformer;

public class EndRemover implements Transformer {

    @Override
    public void transform(IrMethod irMethod) {
        for (Stmt st = irMethod.stmts.getFirst(); st != null; st = st.getNext()) {
            if (st.st == ST.GOTO) {
                JumpStmt js = (JumpStmt) st;
                Stmt to = js.target.getNext();
                switch (to.st) {
                case RETURN: {
                    Stmt nst = Stmts.nReturn(((UnopStmt) to).op.value);
                    irMethod.stmts.replace(st, nst);
                    st = nst;
                }
                    break;
                case RETURN_VOID: {
                    Stmt nst = Stmts.nReturnVoid();
                    irMethod.stmts.replace(st, nst);
                    st = nst;
                }
                    break;
                case THROW: {
                    Stmt nst = Stmts.nThrow(((UnopStmt) to).op.value);
                    irMethod.stmts.replace(st, nst);
                    st = nst;
                }
                    break;
                }
            }
        }
    }

}
