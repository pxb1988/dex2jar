package com.googlecode.dex2jar.ir.test;

import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.ts.AggTransformer;
import com.googlecode.dex2jar.ir.ts.SSATransformer;
import org.junit.jupiter.api.Test;

import static com.googlecode.dex2jar.ir.expr.Exprs.nArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nInt;
import static com.googlecode.dex2jar.ir.expr.Exprs.nNewIntArray;
import static com.googlecode.dex2jar.ir.expr.Exprs.nString;
import static com.googlecode.dex2jar.ir.expr.Exprs.niAdd;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nAssign;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturn;
import static com.googlecode.dex2jar.ir.stmt.Stmts.nReturnVoid;
import static org.junit.jupiter.api.Assertions.*;

public class AggTransformerTest extends BaseTransformerTest<AggTransformer> {

    @Test
    public void t001() {
        Local a = addLocal("a");
        addStmt(nAssign(a, nNewIntArray(nInt(5))));
        addStmt(nReturn(a));
        transform();
        assertEquals(1, stmts.getSize(), "only `return new int[5]` should left.");
        assertEquals(0, locals.size(), "no local should left");
    }

    @Test
    public void t002() {

        Local a = addLocal("a");
        Local b = addLocal("b");
        Local c = addLocal("c");

        addStmt(nAssign(a, nNewIntArray(nInt(5))));
        addStmt(nAssign(b, nInt(2)));
        addStmt(nAssign(c, nArray(a, b, "I")));
        addStmt(nReturn(c));
        transform();
        assertEquals(1, stmts.getSize());
        assertEquals(0, locals.size());
    }

    @Test
    public void test04() {
        Local array = addLocal("array");
        Local index = addLocal("index");
        Local value = addLocal("value");

        addStmt(nAssign(array, nNewIntArray(nInt(5))));
        addStmt(nAssign(index, niAdd(nInt(1999), nInt(3))));
        addStmt(nAssign(value, niAdd(index, nInt(4))));
        addStmt(nAssign(nArray(array, index, "I"), value));
        addStmt(nReturnVoid());

        transform();

        assertTrue(method.locals.size() >= 2);
    }

    @Test
    public void test05() {
        String sbType = "Ljava/lang/StringBuilder;";
        String sType = "Ljava/lang/String;";

        Local b = addLocal("b");
        Local ex = addLocal("ex");
        Local c = addLocal("c");
        Local d = addLocal("d");
        Local e = addLocal("e");
        Local cst = addLocal("cst");

        addStmt(nAssign(b, nString("123")));
        addStmt(nAssign(c, Exprs.nInvokeNew(new Value[0], new String[0], sbType)));
        addStmt(nAssign(d, c));
        addStmt(nAssign(cst, nString("p1")));
        addStmt(nAssign(c,
                Exprs.nInvokeVirtual(new Value[]{d, cst}, sbType, "append", new String[]{sType}, sbType)));
        addStmt(nAssign(e, c));
        addStmt(nAssign(cst, nString("p2")));
        addStmt(nAssign(c,
                Exprs.nInvokeVirtual(new Value[]{e, cst}, sbType, "append", new String[]{sType}, sbType)));
        addStmt(nAssign(c, Exprs.nInvokeVirtual(new Value[]{c}, sbType, "toString", new String[0], sType)));

        addStmt(nReturn(c));
        new SSATransformer().transform(method);
        transform();
        assertEquals(1, stmts.getSize());
        assertEquals(0, locals.size());
    }

}
