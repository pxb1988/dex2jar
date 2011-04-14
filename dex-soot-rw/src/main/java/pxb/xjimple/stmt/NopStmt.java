package pxb.xjimple.stmt;

import java.util.Map;

public class NopStmt extends Stmt {

    public NopStmt() {
        super(ST.NOP);
    }

    @Override
    public Stmt clone(Map<LabelStmt, LabelStmt> map) {
        return new NopStmt();
    }

    public String toString() {
        return "NOP";
    }
}
