package pxb.xjimple.stmt;

import java.util.Map;

public class ReturnVoidStmt extends Stmt {

    public ReturnVoidStmt() {
        super(ST.RETURN_VOID);
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        return new ReturnVoidStmt();
    }

}
