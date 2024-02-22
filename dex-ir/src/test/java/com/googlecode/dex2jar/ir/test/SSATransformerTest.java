package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.RemoveLocalFromSSA;
import com.googlecode.dex2jar.ir.ts.SSATransformer;
import org.junit.jupiter.api.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nExceptionRef;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNull;
import static com.googlecode.dex2jar.ir.expr.Exprs.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.niGt;
import static com.googlecode.dex2jar.ir.expr.Exprs.njGt;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nGoto;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIdentity;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIf;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nLabel;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static org.junit.jupiter.api.Assertions.*;

public class SSATransformerTest extends BaseTransformerTest<SSATransformer> {

    /**
     * base test
     */
    @Test
    public void test00Base() {

        Local b = addLocal("b");

        AssignStmt st1 = addStmt(nAssign(b, nString("123")));
        AssignStmt st2 = addStmt(nAssign(b, nNull()));
        UnopStmt st3 = addStmt(nReturn(b));

        transform();

        assertSame(st2.op1, st3.op);
        assertNotSame(st1.op1, st2.op1, "st1 and st1 must be cut");
        assertEquals(2, method.locals.size());
    }

    /**
     * Test for huge local and stmt size
     */
    @Test
    public void test01HugeLocalStmt() {
        for (int i = 0; i < 2000; i++) {
            Local b = addLocal("a");
            addStmt(nAssign(b, nString("123")));
        }
        Local b = addLocal("a");
        for (int i = 0; i < 20000; i++) {
            addStmt(nAssign(b, nString("123")));
        }
        addStmt(nReturn(b));
        transform();
    }

    /**
     * test for Phi insert
     */
    @Test
    public void test05PhiInGoto() {

        Local b = addLocal("a");

        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();
        addStmt(nIf(niGt(nInt(100), nInt(0)), L1));
        addStmt(nAssign(b, nString("123")));
        addStmt(nGoto(L2));
        addStmt(L1);
        addStmt(nAssign(b, nNull()));
        addStmt(L2);
        addStmt(nReturn(b));

        transform();
        assertEquals(3, method.locals.size());// phi inserted
        assertPhiStmt(L2);
    }

    @Test
    public void test06PhiInTrap() {
        String exType = "Ljava/lang/Exception;";
        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();
        LabelStmt L3 = newLabel();
        LabelStmt L4 = newLabel();
        method.traps.add(new Trap(L1, L2, new LabelStmt[]{L3}, new String[]{exType}));

        Local b = addLocal("a");
        Local ex = addLocal("ex");

        addStmt(L1);
        addStmt(nAssign(b, nString("123")));
        addStmt(Stmts.nLock(b));
        addStmt(L2);
        addStmt(nGoto(L4));
        addStmt(L3);
        addStmt(nIdentity(ex, nExceptionRef(exType)));
        addStmt(nAssign(b, nNull()));
        addStmt(L4);
        addStmt(nReturn(b));

        transform();
        assertEquals(4, method.locals.size());// phi inserted
        assertPhiStmt(L4);
    }

    public void transform() {
        super.transform();
        new RemoveLocalFromSSA().transform(method);
    }

    @Test
    public void test07MergeAtFirst() {

        LabelStmt L0 = newLabel();
        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();
        LabelStmt L3 = newLabel();

        Local b = addLocal("b");
        Local c = addLocal("c");

        addStmt(Stmts.nIf(Exprs.niEq(nInt(1), nInt(2)), L0));
        addStmt(nAssign(b, nInt(3)));
        addStmt(nGoto(L1));
        addStmt(L0);
        addStmt(nAssign(b, nInt(4)));
        addStmt(L1);
        addStmt(Stmts.nIf(Exprs.niEq(nInt(1), nInt(2)), L2));
        addStmt(Stmts.nReturnVoid());
        addStmt(L2);
        addStmt(Stmts.nIf(Exprs.niEq(nInt(1), nInt(2)), L3));
        addStmt(Stmts.nAssign(c,
                Exprs.nInvokeStatic(new Value[]{b}, "Ljava/lang/String;", "someMethod", new String[]{"I"}, "V")));
        addStmt(Stmts.nReturnVoid());
        addStmt(L3);

        addStmt(Stmts.nReturnVoid());

        transform();

        assertEquals(4, method.locals.size());
        assertPhiStmt(L1);

    }

    @Test
    public void test03NotInsertPhiInLoop() {

        Local b = addLocal("a");

        addStmt(nAssign(b, nString("123")));
        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();
        addStmt(L1);
        addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L2));
        addStmt(Stmts.nNop());
        addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L1));
        addStmt(L2);
        addStmt(nReturn(b));

        transform();
        assertEquals(1, method.locals.size());
    }

    @Test
    public void test04NotInsertPhiLoop2() {

        Local b = addLocal("a");

        addStmt(nAssign(b, nString("123")));
        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();
        addStmt(L1);
        addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L2));
        addStmt(Stmts.nNop());
        addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L1));
        addStmt(Stmts.nNop());
        addStmt(L2);
        addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L1));
        addStmt(Stmts.nNop());

        addStmt(nReturn(b));

        transform();
        assertEquals(1, method.locals.size(), "no phi should add");
    }

    @Test
    public void test02NotInsertPhi() {

        Local b = addLocal("a");
        addStmt(nAssign(b, nString("123")));
        LabelStmt L1 = newLabel();
        addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L1));
        addStmt(Stmts.nNop());
        addStmt(L1);
        addStmt(nReturn(b));

        transform();
        assertEquals(1, method.locals.size());
    }

    /**
     * test for
     *
     * <pre>
     * if (xxx) {
     *     a = 1;
     * } else {
     *     a = 2;
     * }// phi here
     *
     * if (xxx) {
     *     if (xxx) {
     *         a = 3;
     *     }// phi here
     *     return a;
     * } else {
     *     if (xxx) {
     *         a = 4;
     *     } // phi here
     *     return a;
     * }
     * </pre>
     */
    @Test
    public void test08() {

        Local a = addLocal("a");
        LabelStmt L0 = newLabel();
        LabelStmt L1 = newLabel();
        LabelStmt L2 = newLabel();
        LabelStmt L3 = newLabel();
        LabelStmt L4 = newLabel();
        LabelStmt L5 = newLabel();
        {// if-else
            addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L0));
            {
                addStmt(nAssign(a, nString("1")));
            }
            addStmt(Stmts.nGoto(L1));
            addStmt(L0);
            {
                addStmt(nAssign(a, nString("2")));
            }
            addStmt(L1);// expect a phi here
        }

        {// if-else
            addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L2));
            {
                { // if
                    addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L4));
                    addStmt(nAssign(a, nString("3")));
                    addStmt(L4);
                }
                addStmt(nReturn(a));
            }
            addStmt(Stmts.nGoto(L3));
            addStmt(L2);
            {
                { // if
                    addStmt(Stmts.nIf(Exprs.niEq(nInt(0), nInt(9)), L5));
                    addStmt(nAssign(a, nString("4")));
                    addStmt(L5);
                }
                addStmt(nReturn(a));
            }
            addStmt(L3);
        }
        transform();

        assertPhiStmt(L1);
        assertPhiStmt(L4);
        assertPhiStmt(L5);
        assertEquals(7, method.locals.size());// 4 assign + 3 phi
    }

    private void assertPhiStmt(LabelStmt label) {
        assertNotNull(label.phis);
        assertTrue(!label.phis.isEmpty());
    }

    /**
     * for
     *
     * <pre>
     *     a=12;
     *     b=34;
     *     c=a;
     *     if c1=0 goto L1:
     *     c=b;
     *     L1:
     *     return c;
     *
     * </pre>
     */
    @Test
    public void test11NotDeleteAssignWherePhiIsConfused() {
        Local a = addLocal("a");
        Local b = addLocal("b");
        Local c = addLocal("c");
        LabelStmt L1 = nLabel();
        addStmt(nAssign(a, nString("12")));
        addStmt(nAssign(b, nString("34")));
        addStmt(nAssign(c, a));
        Stmt jmp = addStmt(nIf(njGt(c, nInt(0)), L1));
        addStmt(nAssign(c, b));
        addStmt(L1);
        addStmt(Stmts.nReturn(c));
        transform();

        assertNotSame(jmp.getNext(), L1, "the c=b should not deleted");
    }

}
