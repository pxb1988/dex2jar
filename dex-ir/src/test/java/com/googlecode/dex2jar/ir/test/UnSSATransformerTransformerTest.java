package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmt.ST;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.UnSSATransformer;
import org.junit.jupiter.api.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeStatic;
import static com.googlecode.dex2jar.ir.expr.Exprs.nPhi;
import static com.googlecode.dex2jar.ir.expr.Exprs.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.niAdd;
import static com.googlecode.dex2jar.ir.expr.Exprs.niGt;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nGoto;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIf;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nLock;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturnVoid;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nVoidInvoke;
import static org.junit.jupiter.api.Assertions.*;

public class UnSSATransformerTransformerTest extends BaseTransformerTest<UnSSATransformer> {

    @Test
    public void test00Base() {
        initMethod(true, "V");
        Local a = addLocal("a");
        Local b = addLocal("b");
        Local phi = addLocal("p");
        LabelStmt L1 = newLabel();
        Stmt s1 = addStmt(nAssign(a, nString("123")));
        addStmt(nIf(niGt(nInt(100), nInt(0)), L1));
        Stmt s2 = addStmt(nAssign(b, nString("456")));
        addStmt(L1);
        attachPhi(L1, nAssign(phi, nPhi(a, b)));
        addStmt(nReturn(phi));
        transform();
        assertEquals(ST.ASSIGN, s1.getNext().st, "insert assign after s1");
        assertEquals(ST.ASSIGN, s2.getNext().st, "insert assign after s1");
        // assertEquals(0, b._ls_index, "local should index to 0");
    }

    @Test
    public void test01SSAProblem() {
        initMethod(true, "I");
        Local a = addLocal("a");
        Local b = addLocal("b");
        Local phi = addLocal("p");
        LabelStmt L0 = newLabel();
        addStmt(nAssign(a, nInt(2)));
        addStmt(L0);
        attachPhi(L0, nAssign(phi, nPhi(a, b)));
        Stmt stmt = addStmt(nAssign(b, niAdd(phi, nInt(0))));
        addStmt(nIf(niGt(nInt(100), nInt(0)), L0));
        addStmt(nReturn(phi));
        transform();
        assertNotSame(stmt.getPre(), L0, "a new local should introduced to solve the problem");
    }

    @Test
    public void test02_3branches() {
        initMethod(true, "I");
        Local a = addLocal("a");
        Local b = addLocal("b");
        Local c = addLocal("c");
        Local d = addLocal("d");
        Local phi = addLocal("p");
        LabelStmt L0 = newLabel();
        LabelStmt L1 = newLabel();
        addStmt(nAssign(a, nInt(2)));
        addStmt(nIf(niGt(nInt(100), nInt(0)), L1));
        addStmt(nAssign(b, nInt(3)));
        addStmt(nIf(niGt(nInt(100), nInt(0)), L0));
        addStmt(nAssign(c, nInt(4)));
        addStmt(nLock(c));
        addStmt(nGoto(L1));
        addStmt(L0);
        addStmt(nAssign(d, nInt(5)));
        addStmt(nLock(d));
        addStmt(L1);
        attachPhi(L1, nAssign(phi, nPhi(a, b)));
        addStmt(nReturn(phi));
        transform();
    }

    @Test
    public void test04OneInPhi() {
        initMethod(true, "V");
        Local a = addLocal("a");
        Local b = addLocal("b");
        Local phi = addLocal("p");
        LabelStmt L1 = newLabel();
        Stmt s1 = addStmt(nAssign(a, nString("123")));
        Stmt j = addStmt(nIf(niGt(nInt(100), nInt(0)), L1));
        Stmt s2 = addStmt(nAssign(b, nString("456")));
        addStmt(L1);
        attachPhi(L1, nAssign(phi, nPhi(a)));
        addStmt(nReturn(phi));
        transform();
        assertNotSame(j.getPre(), s1, "p=a should inserted");

    }

    @Test
    public void test05OneInPhiLoop() {
        initMethod(true, "V");
        Local a = addLocal("a");
        Local b = addLocal("b");
        Local phi = addLocal("p");

        Stmt s1 = addStmt(nAssign(a, nString("123")));
        LabelStmt L1 = newLabel();
        addStmt(L1);
        attachPhi(L1, nAssign(phi, nPhi(a)));
        addStmt(nVoidInvoke(nInvokeStatic(new Value[]{phi}, "LAAA;", "bMethod",
                new String[]{"Ljava/lang/String;"}, "V")));
        addStmt(nAssign(b, nString("456")));

        // phi is still live here
        Stmt s2 = addStmt(nVoidInvoke(nInvokeStatic(new Value[]{b}, "LBBB;", "cMethod",
                new String[]{"Ljava/lang/String;"}, "V")));
        addStmt(nIf(niGt(nInt(100), nInt(0)), L1));
        addStmt(nReturnVoid());
        transform();

        assertNotSame(s1.getPre(), L1, "p=a should inserted");

    }

    @Test
    public void test06TwoJump() {
        initMethod(true, "V");
        Local a1 = addLocal("a1");
        Local a2 = addLocal("a2");
        Local a = addLocal("a");

        Local b1 = addLocal("b1");
        Local b2 = addLocal("b2");
        Local b = addLocal("b");

        addStmt(nAssign(a1, nString("123")));
        addStmt(nAssign(b1, nString("123")));
        LabelStmt L1 = newLabel();
        addStmt(L1);
        attachPhi(L1, nAssign(a, nPhi(a1, a2)));
        attachPhi(L1, nAssign(b, nPhi(b1, b2)));

        addStmt(nAssign(a2, nString("456")));

        addStmt(nIf(niGt(nInt(100), nInt(0)), L1));
        addStmt(nAssign(b2, nString("456")));
        addStmt(nIf(niGt(nInt(100), nInt(0)), L1));

        addStmt(nReturnVoid());
        transform();
        // assertTrue(ls1._ls_index != ls2._ls_index, "must assign different index");

    }

    @Test
    public void test07PhiInHandler() {
        initMethod(true, "I");
        Local a1 = addLocal("a1");
        Local a2 = addLocal("a2");
        Local a = addLocal("a");
        Local ex = addLocal("ex");
        addStmt(Stmts.nAssign(a1, nInt(1)));
        LabelStmt L0 = newLabel();
        LabelStmt L2 = newLabel();
        LabelStmt L3 = newLabel();
        addStmt(L0);
        addStmt(Stmts.nVoidInvoke(Exprs.nInvokeStatic(new Value[0], "La;", "m", new String[0], "V")));
        addStmt(Stmts.nAssign(a2, nInt(2)));
        addStmt(Stmts.nVoidInvoke(Exprs.nInvokeStatic(new Value[0], "La;", "m", new String[0], "V")));
        addStmt(L2);
        addStmt(Stmts.nReturn(a2));
        addStmt(L3);
        Stmt ref = addStmt(Stmts.nIdentity(ex, Exprs.nExceptionRef("Ljava/lang/Exception;")));
        attachPhi(L3, Stmts.nAssign(a, nPhi(a1, a2)));
        addStmt(Stmts.nVoidInvoke(Exprs.nInvokeStatic(new Value[]{a1}, "La;", "m", new String[]{"I"}, "V")));
        addStmt(Stmts.nReturn(a));
        method.traps.add(new Trap(L0, L2, new LabelStmt[]{L3}, new String[]{"Ljava/lang/Exception"}));
        transform();
        assertSame(L3.getNext(), ref, "the fix assign should insert after x=@ExceptionRef");
    }

}
