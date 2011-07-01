package pxb.jimple.test;

import org.junit.Test;
import org.objectweb.asm.Type;

import pxb.xjimple.Constant;
import pxb.xjimple.Local;
import pxb.xjimple.expr.Exprs;
import pxb.xjimple.stmt.LabelStmt;
import pxb.xjimple.stmt.StmtList;
import pxb.xjimple.stmt.Stmts;

public class StmtListTest {
    @Test
    public void toStringTest() {

        StmtList list = new StmtList();
        Local a = Exprs.nLocal("this", Type.getType("Ljava/lang/String;"));
        Local b = Exprs.nLocal("b", Type.INT_TYPE);
        Local c = Exprs.nLocal("c", Type.LONG_TYPE);
        Local d = Exprs.nLocal("d", Type.FLOAT_TYPE);
        LabelStmt L1 = Stmts.nLabel();

        list.add(Stmts.nIdentity(a, Exprs.nThisRef(Type.getType("La/Some;"))));
        list.add(Stmts.nIdentity(b, Exprs.nParameterRef(Type.INT_TYPE, 0)));
        list.add(Stmts.nIdentity(c, Exprs.nParameterRef(Type.LONG_TYPE, 1)));
        list.add(Stmts.nIdentity(d, Exprs.nParameterRef(Type.FLOAT_TYPE, 2)));
        list.add(Stmts.nIf(Exprs.nGt(b, Constant.nInt(0)), L1));
        list.add(Stmts.nAssign(c, Exprs.nCast(d, Type.LONG_TYPE)));
        list.add(L1);
        list.toString();
    }
}
