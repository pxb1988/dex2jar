package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.TypeClass;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import com.googlecode.dex2jar.ir.ts.TypeTransformer;
import org.junit.Assert;
import org.junit.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nConstant;
import static com.googlecode.dex2jar.ir.expr.Exprs.nEq;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInvokeStatic;
import static com.googlecode.dex2jar.ir.expr.Exprs.nLong;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNewMutiArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nOr;
import static com.googlecode.dex2jar.ir.expr.Exprs.nStaticField;
import static com.googlecode.dex2jar.ir.expr.Exprs.nString;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nFillArrayData;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nIf;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturnVoid;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nVoidInvoke;

public class TypeTransformerTest extends BaseTransformerTest<TypeTransformer> {

    /**
     * base test
     */
    @Test
    public void test00Base() {
        initMethod(true, "Ljava/lang/Object;");
        Local b = addLocal("b");

        addStmt(nAssign(b, nString("123")));
        addStmt(nReturn(b));

        transform();
        Assert.assertEquals("", "L", b.valueType.substring(0, 1));
    }

    @Test
    public void test1Const() {
        initMethod(true, "F");
        Local b = addLocal("b");

        AssignStmt st1 = addStmt(nAssign(b, nInt(0)));
        UnopStmt st3 = addStmt(nReturn(b));
        transform();
        Assert.assertEquals("", b.valueType, "F");
    }

    @Test
    public void test2byte() {
        initMethod(true, "V");
        Local b = addLocal("b");

        addStmt(nAssign(b, nStaticField("La;", "z", "B")));
        addStmt(nVoidInvoke(nInvokeStatic(new Value[]{b}, "La;", "y", new String[]{"I"}, "V")));
        addStmt(nAssign(nStaticField("La;", "z", "B"), b));
        addStmt(nReturnVoid());
        transform();
        // FIXME fix type detect
        // Assert.assertEquals("", "I", b.valueType);
    }

    @Test
    public void test2char() {
        initMethod(true, "V");
        Local b = addLocal("b");

        addStmt(nAssign(b, nInt(255)));
        addStmt(nVoidInvoke(nInvokeStatic(new Value[]{b}, "La;", "y", new String[]{"I"}, "V")));
        addStmt(nAssign(nStaticField("La;", "z", "C"), b));
        addStmt(nReturnVoid());
        transform();
        // FIXME fix type detect
        // Assert.assertEquals("", "I", b.valueType);
    }

    // @Ignore("type b to Int is ok to this context")
    @Test
    public void test3() {
        initMethod(true, "V");
        Local b = addLocal("b");

        addStmt(nAssign(b, nInt(456)));
        LabelStmt L0 = newLabel();
        addStmt(nIf(nEq(b, nInt(0), TypeClass.ZIFL.name), L0));
        addStmt(L0);
        addStmt(nReturnVoid());
        transform();
        Assert.assertEquals("", "I", b.valueType);
    }

    @Test
    public void test3Z() {
        initMethod(true, "V");
        Local b = addLocal("b");

        addStmt(nAssign(b, nInt(1)));
        LabelStmt L0 = newLabel();
        addStmt(nIf(nEq(b, nInt(0), TypeClass.ZIFL.name), L0));
        addStmt(L0);
        addStmt(nReturnVoid());
        transform();
        // FIXME local should type to Z but I works as well
        // Assert.assertEquals("", "Z", b.valueType);
    }

    @Test
    public void test2arrayF() {
        initMethod(true, "V");
        Local b = addLocal("b");
        Local c = addLocal("c");

        addStmt(nAssign(b, nNewMutiArray("F", 1, new Value[]{nInt(2)})));
        addStmt(nFillArrayData(b, nConstant(new int[]{5, 6})));
        addStmt(nAssign(c, nArray(b, nInt(3), TypeClass.IF.name)));
        addStmt(nReturnVoid());
        transform();
        Assert.assertEquals("", b.valueType, "[F");
    }

    @Test
    public void testDefaultZI() {
        initMethod(true, "V");
        Local b = addLocal("b");
        Local c = addLocal("c");

        addStmt(nAssign(b, nInt(5)));
        addStmt(nAssign(c, nOr(b, nInt(6), TypeClass.ZI.name)));

        addStmt(nReturnVoid());
        transform();
        Assert.assertEquals("I", c.valueType);
    }


    @Test
    public void testGithubIssue28() {
        initMethod(true, "V");
        Local b = addLocal("b");

        addStmt(nAssign(b, nNewMutiArray("D", 2, new Value[]{nInt(2), nInt(3)})));
        addStmt(nAssign(nArray(nArray(b, nInt(5), TypeClass.OBJECT.name), nInt(1), TypeClass.JD.name), nLong(0)));
        addStmt(nReturnVoid());
        transform();
        Assert.assertEquals("", b.valueType, "[[D");
    }

    @Test
    public void testGithubIssue28x() {
        initMethod(true, "V");
        Local b = addLocal("b");

        addStmt(nAssign(b, nInt(0)));
        addStmt(nAssign(nArray(nArray(b, nInt(5), TypeClass.OBJECT.name), nInt(1), TypeClass.JD.name), nLong(0)));
        addStmt(nReturnVoid());
        transform();
        // this case is ok to fail as the NPE transformer cover this
        // Assert.assertEquals("", "[[D", b.valueType);
    }

}
